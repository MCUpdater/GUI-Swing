package org.mcupdater.gui;

import org.mcupdater.model.ServerList;
import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

final class ServerListCellRenderer extends JPanel implements ListCellRenderer<ServerList>
{
	private static final int LIST_CELL_ICON_SIZE = 32;

	private JLabel lblIcon;
	private JLabel lblServerName;
	private JLabel lblMCVersion;
	private JLabel lblPackVersion;

	public ServerListCellRenderer() {
		lblServerName = new JLabel(" ");
		lblMCVersion = new JLabel(" ");
		lblPackVersion = new JLabel(" ");
		lblIcon = new JLabel();
		lblIcon.setOpaque(true);
		lblIcon.setHorizontalAlignment(JLabel.CENTER);
		lblIcon.setVerticalAlignment(JLabel.CENTER);
		lblIcon.setBackground(Color.WHITE);
		int imageSize = LIST_CELL_ICON_SIZE + 4;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
		layout.setHorizontalGroup(hg);
		hg.
				addComponent(lblIcon, imageSize, imageSize + 10, imageSize + 10).
				addGroup(layout.createParallelGroup().
						addComponent(lblServerName, 50, 125, Integer.MAX_VALUE).
						addComponent(lblMCVersion, 50, 125, Integer.MAX_VALUE).
						addComponent(lblPackVersion, 50, 125, Integer.MAX_VALUE));

		GroupLayout.ParallelGroup vg = layout.createParallelGroup();
		layout.setVerticalGroup(vg);
		vg.
				addComponent(lblIcon, GroupLayout.Alignment.CENTER, imageSize, imageSize + 10, imageSize + 10).
				addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup().
						addComponent(lblServerName).
						addComponent(lblMCVersion).
						addComponent(lblPackVersion));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ServerList> list, ServerList value, int index, boolean isSelected, boolean cellHasFocus) {
		ServerList entry = value;
		String serverName = entry.getName();
		Font fontInfo = new Font("SansSerif", Font.PLAIN, 10);
		lblServerName.setText(serverName);
		lblMCVersion.setText("MC Version: " + entry.getVersion());
		lblPackVersion.setText("Pack revision: " + entry.getRevision());
		lblServerName.setFont(fontInfo);
		lblMCVersion.setFont(fontInfo);
		lblPackVersion.setFont(fontInfo);
		ImageIcon iconPack;
		try {
			iconPack = new ImageIcon(new URL(entry.getIconUrl()));
		} catch (MalformedURLException e) {
			iconPack = MCUpdater.getInstance().defaultIcon;
		}
		Image img = iconPack.getImage(); //.getScaledInstance(32,32,Image.SCALE_SMOOTH);
		BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		g.drawImage(img, 0, 0, 32, 32, lblIcon);
		lblIcon.setIcon(new ImageIcon(bi));

		if (isSelected) {
			adjustColors(list.getSelectionBackground(), list.getSelectionForeground(), this, lblIcon, lblServerName, lblMCVersion, lblPackVersion);
		} else {
			adjustColors(list.getBackground(), list.getForeground(), this, lblIcon, lblServerName, lblMCVersion, lblPackVersion);
		}
		return this;
	}

	private void adjustColors(Color bg, Color fg, Component... components) {
		for (Component c : components) {
			c.setForeground(fg);
			c.setBackground(bg);
		}
	}

}
