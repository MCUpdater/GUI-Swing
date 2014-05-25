package org.mcupdater.gui;

import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.MojangStatus;
import org.mcupdater.util.ServerPackParser;
import org.mcupdater.util.ServerStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends MCUApp implements SettingsListener {
	private static MainForm instance;
	private JFrame frameMain;
	private SLListModel slModel;
	private ProfileModel profileModel;
	private JList<ServerList> serverList;
	private JComboBox<Profile> cboProfiles;
	private BrowserProxy newsBrowser = BrowserProxy.createProxy();
	private JButton btnRefresh;
	private ServerList selected;
	private Gson gson = new Gson();
	protected ModulePanel modPanel = new ModulePanel();
	private ImageIcon GREEN_FLAG = new ImageIcon(this.getClass().getResource("flag_green.png"));
	private ImageIcon RED_FLAG = new ImageIcon(this.getClass().getResource("flag_red.png"));
	private JLabel lblServerStatus;

	public MainForm() {
		SettingsManager.getInstance().addListener(this);
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		FileHandler mcuHandler;
		try {
			mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCUpdater.log").toString(), 0, 3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			baseLogger.addHandler(mcuHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		baseLogger.addHandler(ConsoleForm.getHandler());
		Version.setApp(this);
		MCUpdater.getInstance().setParent(this);
		instance = this;
		baseLogger.info("Activate interlock!");
		initGui();
		baseLogger.info("Dynatherms connected!");
		bindLogic();
		baseLogger.info("Infracells up!");
		refreshInstanceList();
		refreshProfileList();
		String lastProfile = SettingsManager.getInstance().getSettings().getLastProfile();
		setSelectedInstance(SettingsManager.getInstance().getSettings().findProfile(lastProfile).getLastInstance());
		frameMain.setVisible(true);
	}

	// Section - GUI elements

	public void initGui() {
		frameMain = new JFrame();
		frameMain.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		frameMain.setTitle("MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
		frameMain.setBounds(100, 100, 1175, 592);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Left Pane
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		panelLeft.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelButtons = new JPanel();
			panelButtons.setLayout(new GridLayout(0, 3));
			{
				JButton btnAddURL = new JButton();
				btnAddURL.setIcon(new ImageIcon(this.getClass().getResource("add.png")));
				btnAddURL.setToolTipText("Add URL");
				btnAddURL.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnAddURL.setHorizontalTextPosition(SwingConstants.CENTER);
				btnRefresh = new JButton();
				btnRefresh.setIcon(new ImageIcon(this.getClass().getResource("arrow_refresh.png")));
				btnRefresh.setToolTipText("Refresh");
				btnRefresh.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnRefresh.setHorizontalTextPosition(SwingConstants.CENTER);
				JButton btnSettings = new JButton();
				btnSettings.setIcon(new ImageIcon(this.getClass().getResource("cog.png")));
				btnSettings.setToolTipText("Settings");
				btnSettings.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnSettings.setHorizontalTextPosition(SwingConstants.CENTER);
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
			JScrollPane modScroller = new JScrollPane(modPanel);
			modScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			JTabbedPane instanceTabs = new JTabbedPane();
			{
				instanceTabs.addTab("News", newsBrowser.getBaseComponent());
				instanceTabs.addTab("Mods", modScroller);
				instanceTabs.addTab("Progress", new JPanel());
				instanceTabs.addTab("Changes", new JPanel());
				instanceTabs.addTab("Maintenance", new JPanel());
			}
			contentPanel.add(instanceTabs, BorderLayout.CENTER);

			JPanel panelBottom = new JPanel();
			panelBottom.setLayout(new BorderLayout());
			contentPanel.add(panelBottom, BorderLayout.SOUTH);

			JPanel panelStatus = new JPanel();
			panelStatus.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0,5,0,0);
				JLabel lblMojang = new JLabel("Mojang status -");
				JLabel lblAuth = new JLabel("Auth:");
				lblAuth.setIconTextGap(3);
				lblAuth.setHorizontalTextPosition(JLabel.LEFT);
				JLabel lblSession = new JLabel("Session:");
				lblSession.setIconTextGap(3);
				lblSession.setHorizontalTextPosition(JLabel.LEFT);
				MojangStatus current = MojangStatus.getMojangStatus();
				lblAuth.setIcon(current.getAuth() ? GREEN_FLAG : RED_FLAG);
				lblSession.setIcon(current.getSession() ? GREEN_FLAG : RED_FLAG);
				panelStatus.add(lblMojang, gbc);
				panelStatus.add(lblAuth, gbc);
				panelStatus.add(lblSession, gbc);
				JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
				sep.setPreferredSize(new Dimension(5,1));
				gbc.fill = GridBagConstraints.VERTICAL;
				panelStatus.add(sep, gbc);
				gbc = new GridBagConstraints();
				lblServerStatus = new JLabel("");
				panelStatus.add(lblServerStatus, gbc);
			}
			panelBottom.add(panelStatus, BorderLayout.WEST);

			JPanel panelActions = new JPanel();
			panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			{
				profileModel = new ProfileModel();
				cboProfiles = new JComboBox<>(profileModel);
				JButton btnUpdate = new JButton("Update");
				JButton btnLaunch = new JButton("Launch Minecraft");

				panelActions.add(cboProfiles);
				panelActions.add(btnUpdate);
				panelActions.add(btnLaunch);
			}
			panelBottom.add(panelActions, BorderLayout.EAST);

			/*
			JPanel panelDebug = new JPanel();
			panelDebug.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			{
				JButton button4 = new JButton();
				button4.setText("Console Test");
				button4.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ConsoleForm.getConsole().log("Out of band message.");
					}
				});
				JButton button5 = new JButton();
				button5.setText("Undefined");
				JButton button6 = new JButton();
				button6.setText("System L&F");
				button6.addActionListener(new ActionListener() {

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
				panelDebug.add(button4);
				panelDebug.add(button5);
				panelDebug.add(button6);
			}
			panelBottom.add(panelDebug, BorderLayout.CENTER);
			*/
		}
		frameMain.getContentPane().add(contentPanel, BorderLayout.CENTER);

	}

	private void bindLogic() {
		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshInstanceList();
			}
		});
	}

	// Section - Logic elements

	private void setSelectedInstance(String instance) {
		for (ServerList entry : ((SLListModel)serverList.getModel()).getData()) {
			if (entry.getServerId().equals(instance)) {
				serverList.setSelectedValue(entry, true);
				return;
			}
		}
	}

	public static MainForm getInstance() {
		return instance;
	}

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
							docEle = (Element) servers.item(i);
							ServerList sl = ServerList.fromElement(mcuVersion, serverUrl, docEle);
							if (!sl.isFakeServer()) {
								slList.add(sl);
							}
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
			((SLListModel) serverList.getModel()).clearAndSet(slList);
		}
	}

	private void changeSelectedServer(ServerList entry) {
		frameMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.selected = entry;
		newsBrowser.navigate(entry.getNewsUrl());
		List<Module> modList = ServerPackParser.loadFromURL(entry.getPackUrl(), entry.getServerId());
		try {
			Collections.sort(modList, new ModuleComparator(ModuleComparator.Mode.OPTIONAL_FIRST));
		} catch (Exception e) {
			baseLogger.warning("Unable to sort mod list!");
		}
		Set<String> digests = new HashSet<>();
		for (Module mod : modList) {
			if (!mod.getMD5().isEmpty()) {
				digests.add(mod.getMD5());
			}
			for (ConfigFile cf : mod.getConfigs()) {
				if (!cf.getMD5().isEmpty()) {
					digests.add(cf.getMD5());
				}
			}
			for (GenericModule sm : mod.getSubmodules()) {
				if (!sm.getMD5().isEmpty()) {
					digests.add(sm.getMD5());
				}
			}
		}
		String remoteHash = MCUpdater.calculateGroupHash(digests);
		Instance instData = new Instance();
		final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.json");
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			reader.close();
		} catch (IOException e) {
			baseLogger.log(Level.WARNING, "instance.json file not found.  This is not an error if the instance has not been installed.");
		}
		modPanel.reload(modList, instData.getOptionalMods());
		boolean needUpdate = (instData.getHash().isEmpty() || !instData.getHash().equals(remoteHash));
		boolean needNewMCU = Version.isVersionOld(entry.getMCUVersion());

		if (needUpdate) {
			JOptionPane.showMessageDialog(null, "Your configuration is out of sync with the server. Updating is necessary.", "MCUpdater", JOptionPane.WARNING_MESSAGE);
		}
		if (needNewMCU) {
			JOptionPane.showMessageDialog(null, "The server pack indicates that it is for a newer version of MCUpdater than you are currently using.\nThis version of MCUpdater may not properly handle this server.");
		}
		frameMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		baseLogger.info("Selection changed to: " + entry.getServerId());
	}

	private void refreshProfileList() {
		profileModel.clearAndSet(SettingsManager.getInstance().getSettings().getProfiles());
	}

	@Override
	public void setStatus(String string) {
		lblServerStatus.setText(string);
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
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

	private final class InstanceListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				changeSelectedServer(serverList.getSelectedValue());
				try {
					if (!serverList.getSelectedValue().getAddress().isEmpty()) {
						ServerStatus serverStatus = ServerStatus.getStatus(serverList.getSelectedValue().getAddress());
						setStatus(serverStatus.getMOTD() + " - " + serverStatus.getPlayers() + "/" + serverStatus.getMaxPlayers());
					} else {
						setStatus("Server status N/A");
					}
				} catch (Exception e1) {
					baseLogger.log(Level.SEVERE, "Error getting server status", e1);
					setStatus("Server info not available");
				}
			}
		}
	}

}
