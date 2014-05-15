package org.mcupdater.gui;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends MCUApp implements SettingsListener
{
	private static MainForm window;
	private JFrame frameMain;
	private SLListModel slModel;
	private JList<ServerList> serverList;

	public MainForm() {
		SettingsManager.getInstance().addListener(this);
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		Version.setApp(this);
		window = this;
		initGui();
		refreshInstanceList();
		frameMain.setVisible(true);
	}


	// Section - GUI elements

	public void initGui() {
		frameMain = new JFrame();
		frameMain.setTitle("MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
		frameMain.setBounds(100, 100, 1175, 592);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Left Pane
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.BLACK));
		panelLeft.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelButtons = new JPanel();
			panelButtons.setLayout(new GridLayout(0,3));
			{
				JButton btnAddURL = new JButton();
				btnAddURL.setIcon(new ImageIcon(this.getClass().getResource("add.png")));
				btnAddURL.setToolTipText("Add URL");
				btnAddURL.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnAddURL.setHorizontalTextPosition(SwingConstants.CENTER);
				//btnAddURL.setFont(btnAddURL.getFont().deriveFont(10f));
				JButton btnRefresh = new JButton();
				btnRefresh.setIcon(new ImageIcon(this.getClass().getResource("arrow_refresh.png")));
				btnRefresh.setToolTipText("Refresh");
				btnRefresh.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnRefresh.setHorizontalTextPosition(SwingConstants.CENTER);
				//btnRefresh.setFont(btnRefresh.getFont().deriveFont(10f));
				JButton btnSettings = new JButton();
				btnSettings.setIcon(new ImageIcon(this.getClass().getResource("cog.png")));
				btnSettings.setToolTipText("Settings");
				btnSettings.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnSettings.setHorizontalTextPosition(SwingConstants.CENTER);
				//btnSettings.setFont(btnSettings.getFont().deriveFont(10f));
				panelButtons.add(btnAddURL);
				panelButtons.add(btnRefresh);
				panelButtons.add(btnSettings);
			}
			panelLeft.add(panelButtons, BorderLayout.NORTH);

			slModel = new SLListModel();
			serverList = new JList<>();
			serverList.setModel(slModel);
			serverList.setCellRenderer(new ServerListCellRenderer());
			serverList.addListSelectionListener(new InstanceListener());

			JScrollPane instanceScroller = new JScrollPane(serverList);
			panelLeft.add(instanceScroller, BorderLayout.CENTER);
		}
		frameMain.getContentPane().add(panelLeft, BorderLayout.WEST);

		// Center Pane
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane instanceTabs = new JTabbedPane();
			{
				instanceTabs.addTab("News", new JPanel());
				instanceTabs.addTab("Progress", new JPanel());
				instanceTabs.addTab("Changes", new JPanel());
				instanceTabs.addTab("Maintenance", new JPanel());
			}
			contentPanel.add(instanceTabs, BorderLayout.CENTER);

			JPanel panelRightButtons = new JPanel();
			panelRightButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			{
				JButton button4 = new JButton();
				button4.setText("1");
				JButton button5 = new JButton();
				button5.setText("2");
				JButton button6 = new JButton();
				button6.setText("3");
				button6.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
							SwingUtilities.updateComponentTreeUI(frameMain);
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
							e1.printStackTrace();
						}

					}
				});
				panelRightButtons.add(button4);
				panelRightButtons.add(button5);
				panelRightButtons.add(button6);
			}
			contentPanel.add(panelRightButtons, BorderLayout.SOUTH);
		}
		frameMain.getContentPane().add(contentPanel, BorderLayout.CENTER);

	}

	// Section - Logic elements

	private void refreshInstanceList() {
		Settings current = SettingsManager.getInstance().getSettings();
		List<ServerList> slList = new ArrayList<>();

		Set<String> urls = new HashSet<>();
		urls.addAll(current.getPackURLs());

		for (String serverUrl : urls) {
			try {
				Element docEle;
				Document serverHeader = ServerPackParser.readXmlFromUrl(serverUrl);
				if (!(serverHeader == null)) {
					Element parent = serverHeader.getDocumentElement();
					if (parent.getNodeName().equals("ServerPack")) {
						String mcuVersion = parent.getAttribute("version");
						NodeList servers = parent.getElementsByTagName("Server");
						for (int i = 0; i < servers.getLength(); i++) {
							docEle = (Element)servers.item(i);
							ServerList sl = ServerList.fromElement(mcuVersion, serverUrl, docEle);
							if (!sl.isFakeServer()) { slList.add(sl); }
						}
					} else {
						ServerList sl = ServerList.fromElement("1.0", serverUrl, parent);
						slList.add(sl);
					}
				} else {
					log("Unable to get server information from " + serverUrl);
				}
			} catch (Exception e) {
				log(ExceptionUtils.getStackTrace(e));
			}
		}
		if (serverList != null) {
			((SLListModel)serverList.getModel()).clearAndSet(slList);
		}
	}

	private void changeSelectedServer(ServerList selectedValue) {
		//TODO
	}

	@Override
	public void setStatus(String string) {

	}

	@Override
	public void log(String msg) {

	}

	@Override
	public Profile requestLogin(String username) {
		return null;
	}

	@Override
	public void addServer(ServerList entry) {

	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		return null;
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		return null;
	}

	@Override
	public void stateChanged(boolean newState) {

	}

	@Override
	public void settingsChanged(Settings newSettings) {

	}

	private final class InstanceListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				changeSelectedServer(serverList.getSelectedValue());
				// TODO: Check for update
			}
		}
	}

}
