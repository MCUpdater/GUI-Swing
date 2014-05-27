package org.mcupdater.gui;

import layout.SpringUtilities;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog implements SettingsListener {

	private final JButton btnOK;
	private final JButton btnCancel;
	private final JButton btnReload;
	private final JButton btnSave;
	private final JLabel lblStatus;
	private final JTextField txtMinMemory;
	private final JTextField txtMaxMemory;

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
			btnOK = new JButton("OK");
			btnCancel = new JButton("Cancel");
			pnlActions.add(new JLabel("Save status: "), gbcNormal);
			pnlActions.add(lblStatus, gbcFillWidth);
			pnlActions.add(btnSave, gbcNormal);
			pnlActions.add(btnReload, gbcNormal);
			JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
			sep.setPreferredSize(new Dimension(5, 1));
			pnlActions.add(sep, gbcFillHeight);
			pnlActions.add(btnOK, gbcNormal);
			pnlActions.add(btnCancel, gbcNormal);
			pnlActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
		}
		JTabbedPane pnlTabs = new JTabbedPane();
		{
			JPanel pnlJava = new JPanel();
			{
				pnlJava.setLayout(new SpringLayout());
				int rows = 0;
				{
					rows++;
					JLabel lblMinMemory = new JLabel("Minimum Memory: ");
					txtMinMemory = new JTextField();
					Dimension sizeGuide = new Dimension(Integer.MAX_VALUE, txtMinMemory.getMinimumSize().height);
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

				}
				pnlJava.setMaximumSize(new Dimension(Integer.MAX_VALUE, (txtMinMemory.getMinimumSize().height + 3) * rows));
				SpringUtilities.makeCompactGrid(pnlJava, rows, 2, 6, 6, 6, 6);
			}
			pnlTabs.add("Java", new JScrollPane(pnlJava));
			pnlTabs.add("Minecraft", new JPanel());
			pnlTabs.add("MCUpdater", new JPanel());
			pnlTabs.add("Profiles", new JPanel());
			pnlTabs.add("Pack URLs", new JPanel());
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
