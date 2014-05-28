package org.mcupdater.gui;

import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog implements SettingsListener {

	private final JButton btnApply;
	private final JButton btnCancel;
	private final JButton btnReload;
	private final JButton btnSave;
	private final JLabel lblStatus;
	private final JTextField txtMinMemory;
	private final JTextField txtMaxMemory;
	private final JTextField txtPermGen;
	private final JTextField txtJavaHome;
	private final JButton btnJavaHomeBrowse;
	private final JTextField txtJVMOpts;
	private final JTextField txtWrapper;
	private final JButton btnWrapperBrowse;
	private final JCheckBox chkFullscreen;
	private final JTextField txtWindowWidth;
	private final JTextField txtWindowHeight;
	private final JCheckBox chkAutoconnect;
	private final JCheckBox chkMinimize;
	private final JCheckBox chkConsoleOutput;
	private final JList<Profile> lstProfiles;
	private final JButton btnProfileAdd;
	private final JButton btnProfileRemove;
	private final JList<String> lstPacks;
	private final JButton btnPackAdd;
	private final JButton btnPackRemove;
	private final JButton btnClearCache;
	private final JButton btnResetSettings;
	private ProfileModel profileModel;

	public SettingsDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setTitle("Settings");
		setResizable(true);
		getContentPane().setLayout(new BorderLayout());
		setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		setSize(700, 500);
		JPanel pnlActions = new JPanel(new GridBagLayout());
		{
			GridBagConstraints gbcFillWidth = new GridBagConstraints();
			gbcFillWidth.fill = GridBagConstraints.HORIZONTAL;
			gbcFillWidth.weightx = 1;
			GridBagConstraints gbcFillHeight = new GridBagConstraints();
			gbcFillHeight.fill = GridBagConstraints.VERTICAL;
			GridBagConstraints gbcNormal = new GridBagConstraints();
			lblStatus = new JLabel();
			btnSave = new JButton("Save");
			btnReload = new JButton("Reload");
			btnApply = new JButton("Apply");
			btnCancel = new JButton("Cancel");
			pnlActions.add(new JLabel("Save status: "), gbcNormal);
			pnlActions.add(lblStatus, gbcFillWidth);
			pnlActions.add(btnSave, gbcNormal);
			pnlActions.add(btnReload, gbcNormal);
			JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
			sep.setPreferredSize(new Dimension(5, 1));
			pnlActions.add(sep, gbcFillHeight);
			pnlActions.add(btnApply, gbcNormal);
			pnlActions.add(btnCancel, gbcNormal);
			pnlActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
		}
		JTabbedPane pnlTabs = new JTabbedPane();
		{
			Dimension sizeGuide;
			JPanel pnlJava = new JPanel();
			{
				GroupLayout layout = new GroupLayout(pnlJava);
				pnlJava.setLayout(layout);
				{
					// Create column groups
					GroupLayout.Group colLabel = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					GroupLayout.Group colContent = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

					GroupLayout.Group rowMinMemory = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblMinMemory = new JLabel("Minimum Memory: ");
					txtMinMemory = new JTextField();
					sizeGuide = new Dimension(Integer.MAX_VALUE, txtMinMemory.getMinimumSize().height);
					txtMinMemory.setMaximumSize(sizeGuide);
					lblMinMemory.setLabelFor(txtMinMemory);
					colLabel.addComponent(lblMinMemory);
					colContent.addComponent(txtMinMemory);
					rowMinMemory.addComponent(lblMinMemory).addComponent(txtMinMemory);

					GroupLayout.Group rowMaxMemory = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblMaxMemory = new JLabel("Maximum Memory: ");
					txtMaxMemory = new JTextField();
					txtMaxMemory.setMaximumSize(sizeGuide);
					lblMaxMemory.setLabelFor(txtMaxMemory);
					colLabel.addComponent(lblMaxMemory);
					colContent.addComponent(txtMaxMemory);
					rowMaxMemory.addComponent(lblMaxMemory).addComponent(txtMaxMemory);

					GroupLayout.Group rowPermGen = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblPermGen = new JLabel("PermGen Space: ");
					txtPermGen = new JTextField();
					txtPermGen.setMaximumSize(sizeGuide);
					lblPermGen.setLabelFor(txtPermGen);
					colLabel.addComponent(lblPermGen);
					colContent.addComponent(txtPermGen);
					rowPermGen.addComponent(lblPermGen).addComponent(txtPermGen);

					GroupLayout.Group rowJavaHome = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblJavaHome = new JLabel("Java Home Path: ");
					JPanel pnlJavaHome = new JPanel(new BorderLayout());
					txtJavaHome = new JTextField();
					pnlJavaHome.setMaximumSize(sizeGuide);
					lblPermGen.setLabelFor(txtJavaHome);
					btnJavaHomeBrowse = new JButton(new ImageIcon(this.getClass().getResource("folder_explore.png")));
					pnlJavaHome.add(txtJavaHome, BorderLayout.CENTER);
					pnlJavaHome.add(btnJavaHomeBrowse, BorderLayout.EAST);
					colLabel.addComponent(lblJavaHome);
					colContent.addComponent(pnlJavaHome);
					rowJavaHome.addComponent(lblJavaHome).addComponent(pnlJavaHome);

					GroupLayout.Group rowJVMOpts = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblJVMOpts = new JLabel("JVMOpts: ");
					txtJVMOpts = new JTextField();
					txtJVMOpts.setMaximumSize(sizeGuide);
					lblJVMOpts.setLabelFor(txtJVMOpts);
					colLabel.addComponent(lblJVMOpts);
					colContent.addComponent(txtJVMOpts);
					rowJVMOpts.addComponent(lblJVMOpts).addComponent(txtJVMOpts);

					GroupLayout.Group rowWrapper = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblWrapper = new JLabel("Program Wrapper: ");
					JPanel pnlWrapper = new JPanel(new BorderLayout());
					txtWrapper = new JTextField();
					pnlWrapper.setMaximumSize(sizeGuide);
					lblWrapper.setLabelFor(txtWrapper);
					btnWrapperBrowse = new JButton(new ImageIcon(this.getClass().getResource("folder_explore.png")));
					pnlWrapper.add(txtWrapper, BorderLayout.CENTER);
					pnlWrapper.add(btnWrapperBrowse, BorderLayout.EAST);
					colLabel.addComponent(lblWrapper);
					colContent.addComponent(pnlWrapper);
					rowWrapper.addComponent(lblWrapper).addComponent(pnlWrapper);

					layout.setAutoCreateGaps(true);
					layout.setAutoCreateContainerGaps(true);
					layout.setHorizontalGroup(
							layout.createSequentialGroup()
									.addGroup(colLabel)
									.addGroup(colContent)
					);
					layout.setVerticalGroup(
							layout.createSequentialGroup()
									.addGroup(rowMinMemory)
									.addGroup(rowMaxMemory)
									.addGroup(rowPermGen)
									.addGroup(rowJavaHome)
									.addGroup(rowJVMOpts)
									.addGroup(rowWrapper)
					);
				}
			}
			pnlTabs.add("Java", new JScrollPane(pnlJava));

			JPanel pnlMinecraft = new JPanel();
			{
				GroupLayout layout = new GroupLayout(pnlMinecraft);
				pnlMinecraft.setLayout(layout);
				{
					// Create column groups
					GroupLayout.Group colLabel = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					GroupLayout.Group colContent = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

					GroupLayout.Group rowFullscreen = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblFullscreen = new JLabel("Fullscreen: ");
					chkFullscreen = new JCheckBox();
					chkFullscreen.setMaximumSize(sizeGuide);
					lblFullscreen.setLabelFor(chkFullscreen);
					colLabel.addComponent(lblFullscreen);
					colContent.addComponent(chkFullscreen);
					rowFullscreen.addComponent(lblFullscreen).addComponent(chkFullscreen);

					GroupLayout.Group rowWindowWidth = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblWindowWidth = new JLabel("Window Width: ");
					txtWindowWidth = new JTextField();
					txtWindowWidth.setMaximumSize(sizeGuide);
					lblWindowWidth.setLabelFor(txtWindowWidth);
					colLabel.addComponent(lblWindowWidth);
					colContent.addComponent(txtWindowWidth);
					rowWindowWidth.addComponent(lblWindowWidth).addComponent(txtWindowWidth);

					GroupLayout.Group rowWindowHeight = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
					JLabel lblWindowHeight = new JLabel("Window Height: ");
					txtWindowHeight = new JTextField();
					txtWindowHeight.setMaximumSize(sizeGuide);
					lblWindowHeight.setLabelFor(txtWindowHeight);
					colLabel.addComponent(lblWindowHeight);
					colContent.addComponent(txtWindowHeight);
					rowWindowHeight.addComponent(lblWindowHeight).addComponent(txtWindowHeight);

					GroupLayout.Group rowAutoconnect = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblAutoconnect = new JLabel("Automatically Connect: ");
					chkAutoconnect = new JCheckBox();
					chkAutoconnect.setMaximumSize(sizeGuide);
					lblAutoconnect.setLabelFor(chkAutoconnect);
					colLabel.addComponent(lblAutoconnect);
					colContent.addComponent(chkAutoconnect);
					rowAutoconnect.addComponent(lblAutoconnect).addComponent(chkAutoconnect);

					GroupLayout.Group rowMinimize = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblMinimize = new JLabel("Minimize on Launch: ");
					chkMinimize = new JCheckBox();
					chkMinimize.setMaximumSize(sizeGuide);
					lblMinimize.setLabelFor(chkMinimize);
					colLabel.addComponent(lblMinimize);
					colContent.addComponent(chkMinimize);
					rowMinimize.addComponent(lblMinimize).addComponent(chkMinimize);

					GroupLayout.Group rowConsoleOutput = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
					JLabel lblConsoleOutput = new JLabel("Minecraft Output to Console: ");
					chkConsoleOutput = new JCheckBox();
					chkConsoleOutput.setMaximumSize(sizeGuide);
					lblConsoleOutput.setLabelFor(chkConsoleOutput);
					colLabel.addComponent(lblConsoleOutput);
					colContent.addComponent(chkConsoleOutput);
					rowConsoleOutput.addComponent(lblConsoleOutput).addComponent(chkConsoleOutput);

					layout.setAutoCreateGaps(true);
					layout.setAutoCreateContainerGaps(true);
					layout.setHorizontalGroup(
							layout.createSequentialGroup()
							.addGroup(colLabel)
							.addGroup(colContent)
					);
					layout.setVerticalGroup(
							layout.createSequentialGroup()
							.addGroup(rowFullscreen)
							.addGroup(rowWindowWidth)
							.addGroup(rowWindowHeight)
							.addGroup(rowAutoconnect)
							.addGroup(rowMinimize)
							.addGroup(rowConsoleOutput)
					);
				}
			}
			pnlTabs.add("Minecraft", new JScrollPane(pnlMinecraft));

			JPanel pnlMCU = new JPanel();
			{
				GroupLayout layout = new GroupLayout(pnlMCU);
				pnlMCU.setLayout(layout);
				{
					// Create column groups
					GroupLayout.Group colLabel = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					GroupLayout.Group colContent = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

					GroupLayout.Group rowProfiles = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					JLabel lblProfiles = new JLabel("Profiles: ");
					JPanel pnlProfiles = new JPanel(new BorderLayout());
					lstProfiles = new JList<>();
					profileModel = new ProfileModel();
					lstProfiles.setModel(profileModel);
					pnlProfiles.add(lstProfiles, BorderLayout.CENTER);
					JPanel pnlProfileActions = new JPanel();
					pnlProfileActions.setLayout(new GridLayout(6, 1));
					btnProfileAdd = new JButton("Add");
					btnProfileRemove = new JButton("Remove");
					pnlProfileActions.add(btnProfileAdd);
					pnlProfileActions.add(btnProfileRemove);
					pnlProfiles.add(pnlProfileActions, BorderLayout.EAST);
					colLabel.addComponent(lblProfiles);
					colContent.addComponent(pnlProfiles);
					rowProfiles.addComponent(lblProfiles).addComponent(pnlProfiles);

					GroupLayout.Group rowPacks = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
					JLabel lblPacks = new JLabel("Pack URLs: ");
					JPanel pnlPacks = new JPanel(new BorderLayout());
					lstPacks = new JList<>();
					pnlPacks.add(lstPacks, BorderLayout.CENTER);
					JPanel pnlPackActions = new JPanel();
					pnlPackActions.setLayout(new GridLayout(6, 1));
					btnPackAdd = new JButton("Add");
					btnPackRemove = new JButton("Remove");
					pnlPackActions.add(btnPackAdd);
					pnlPackActions.add(btnPackRemove);
					pnlPacks.add(pnlPackActions, BorderLayout.EAST);
					colLabel.addComponent(lblPacks);
					colContent.addComponent(pnlPacks);
					rowPacks.addComponent(lblPacks).addComponent(pnlPacks);

					btnClearCache = new JButton("Clear cache");
					btnResetSettings = new JButton("Reset settings");

					layout.setAutoCreateGaps(true);
					layout.setAutoCreateContainerGaps(true);
					layout.setHorizontalGroup(
							layout.createParallelGroup()
									.addGroup(layout.createSequentialGroup()
											.addGroup(colLabel)
											.addGroup(colContent))
							.addComponent(btnClearCache)
							.addComponent(btnResetSettings)
					);
					layout.setVerticalGroup(
							layout.createSequentialGroup()
									.addGroup(rowProfiles)
									.addGroup(rowPacks)
							.addComponent(btnClearCache)
							.addComponent(btnResetSettings)
					);
				}
			}
			pnlTabs.add("MCUpdater", new JScrollPane(pnlMCU));
		}
		getContentPane().add(pnlTabs, BorderLayout.CENTER);
		getContentPane().add(pnlActions, BorderLayout.SOUTH);

		lblStatus.setText(SettingsManager.getInstance().isDirty() ? "Not Saved" : "Saved");
		updateValues();
	}

	private void updateValues() {
		Settings current = SettingsManager.getInstance().getSettings();
		txtMinMemory.setText(current.getMinMemory());
		txtMaxMemory.setText(current.getMaxMemory());
		txtPermGen.setText(current.getPermGen());
		txtJavaHome.setText(current.getJrePath());
		txtJVMOpts.setText(current.getJvmOpts());
		txtWrapper.setText(current.getProgramWrapper());
		chkFullscreen.setSelected(current.isFullScreen());
		txtWindowWidth.setText(String.valueOf(current.getResWidth()));
		txtWindowHeight.setText(String.valueOf(current.getResHeight()));
		chkAutoconnect.setSelected(current.isAutoConnect());
		chkMinimize.setSelected(current.isMinimizeOnLaunch());
		chkConsoleOutput.setSelected(current.isMinecraftToConsole());
		profileModel.clearAndSet(current.getProfiles());
		lstPacks.setListData(current.getPackURLs().toArray(new String[0]));
	}

	@Override
	public void stateChanged(boolean newState) {
		lblStatus.setText(newState ? "Not Saved" : "Saved");
	}

	@Override
	public void settingsChanged(Settings newSettings) {
		updateValues();
	}

}
