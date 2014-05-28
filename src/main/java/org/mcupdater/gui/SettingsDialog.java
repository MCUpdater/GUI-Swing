package org.mcupdater.gui;

import layout.SpringUtilities;
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
				pnlJava.setLayout(new SpringLayout());
				int rows = 0;
				{
					rows++;
					JLabel lblMinMemory = new JLabel("Minimum Memory: ");
					txtMinMemory = new JTextField();
					sizeGuide = new Dimension(Integer.MAX_VALUE, txtMinMemory.getMinimumSize().height);
					txtMinMemory.setMaximumSize(sizeGuide);
					lblMinMemory.setLabelFor(txtMinMemory);
					pnlJava.add(lblMinMemory);
					pnlJava.add(txtMinMemory);

					rows++;
					JLabel lblMaxMemory = new JLabel("Maximum Memory: ");
					txtMaxMemory = new JTextField();
					txtMaxMemory.setMaximumSize(sizeGuide);
					lblMaxMemory.setLabelFor(txtMaxMemory);
					pnlJava.add(lblMaxMemory);
					pnlJava.add(txtMaxMemory);

					rows++;
					JLabel lblPermGen = new JLabel("PermGen Space: ");
					txtPermGen = new JTextField();
					txtPermGen.setMaximumSize(sizeGuide);
					lblPermGen.setLabelFor(txtPermGen);
					pnlJava.add(lblPermGen);
					pnlJava.add(txtPermGen);

					rows++;
					JLabel lblJavaHome = new JLabel("Java Home Path: ");
					JPanel pnlJavaHome = new JPanel(new BorderLayout());
					txtJavaHome = new JTextField();
					pnlJavaHome.setMaximumSize(sizeGuide);
					lblPermGen.setLabelFor(txtJavaHome);
					btnJavaHomeBrowse = new JButton(new ImageIcon(this.getClass().getResource("folder_explore.png")));
					pnlJava.add(lblJavaHome);
					pnlJavaHome.add(txtJavaHome, BorderLayout.CENTER);
					pnlJavaHome.add(btnJavaHomeBrowse, BorderLayout.EAST);
					pnlJava.add(pnlJavaHome);

					rows++;
					JLabel lblJVMOpts = new JLabel("JVMOpts: ");
					txtJVMOpts = new JTextField();
					txtJVMOpts.setMaximumSize(sizeGuide);
					lblJVMOpts.setLabelFor(txtJVMOpts);
					pnlJava.add(lblJVMOpts);
					pnlJava.add(txtJVMOpts);

					rows++;
					JLabel lblWrapper = new JLabel("Program Wrapper: ");
					JPanel pnlWrapper = new JPanel(new BorderLayout());
					txtWrapper = new JTextField();
					pnlWrapper.setMaximumSize(sizeGuide);
					lblWrapper.setLabelFor(txtWrapper);
					btnWrapperBrowse = new JButton(new ImageIcon(this.getClass().getResource("folder_explore.png")));
					pnlJava.add(lblWrapper);
					pnlWrapper.add(txtWrapper, BorderLayout.CENTER);
					pnlWrapper.add(btnWrapperBrowse, BorderLayout.EAST);
					pnlJava.add(pnlWrapper);
				}
				SpringUtilities.makeCompactGrid(pnlJava, rows, 2, 6, 6, 6, 6);
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
					rowAutoconnect.addComponent(lblMinimize).addComponent(chkMinimize);

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
					);
				}
			}
			pnlTabs.add("Minecraft", new JScrollPane(pnlMinecraft));
			pnlTabs.add("MCUpdater", new JPanel());
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
