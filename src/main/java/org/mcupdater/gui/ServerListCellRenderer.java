package org.mcupdater.gui;

import org.mcupdater.model.ServerList;
import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

final class ServerListCellRenderer extends JPanel implements ListCellRenderer<ServerList> {
	private static final int LIST_CELL_ICON_SIZE = 32;

	public final ImageIcon STATUS_ERROR = new ImageIcon(this.getClass().getResource("cross.png"));
	public final ImageIcon STATUS_UPDATE = new ImageIcon(this.getClass().getResource("asterisk_yellow.png"));
	public final ImageIcon STATUS_READY = new ImageIcon(this.getClass().getResource("tick.png"));

	private JLayeredPane paneIcon;
	private JLabel lblStatus;

	private JLabel lblIcon;
	private JLabel lblServerName;
	private JLabel lblMCVersion;
	private JLabel lblPackVersion;

	public ServerListCellRenderer() {
		lblServerName = new JLabel(" ");
		lblMCVersion = new JLabel(" ");
		lblPackVersion = new JLabel(" ");

		int imageSize = LIST_CELL_ICON_SIZE + 4;

		lblIcon = new JLabel();
		lblIcon.setOpaque(true);
		lblIcon.setHorizontalAlignment(JLabel.CENTER);
		lblIcon.setVerticalAlignment(JLabel.CENTER);
		lblIcon.setBackground(Color.WHITE);
		lblIcon.setBounds(0,0,imageSize+10, imageSize+10);

		lblStatus = new JLabel();
		lblStatus.setOpaque(false);
		lblStatus.setBounds(0,0,16,16);

		paneIcon = new JLayeredPane();
		paneIcon.add(lblIcon, JLayeredPane.DEFAULT_LAYER);
		paneIcon.add(lblStatus, JLayeredPane.POPUP_LAYER);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
		layout.setHorizontalGroup(hg);
		hg.
				addComponent(paneIcon, imageSize, imageSize + 10, imageSize + 10).
				addGroup(layout.createParallelGroup().
						addComponent(lblServerName, 50, 125, Integer.MAX_VALUE).
						addComponent(lblMCVersion, 50, 125, Integer.MAX_VALUE).
						addComponent(lblPackVersion, 50, 125, Integer.MAX_VALUE));

		GroupLayout.ParallelGroup vg = layout.createParallelGroup();
		layout.setVerticalGroup(vg);
		vg.
				addComponent(paneIcon, GroupLayout.Alignment.CENTER, imageSize, imageSize + 10, imageSize + 10).
				addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup().
						addComponent(lblServerName).
						addComponent(lblMCVersion).
						addComponent(lblPackVersion));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ServerList> list, ServerList entry, int index, boolean isSelected, boolean cellHasFocus) {
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

		switch( entry.getState() ) {
			case READY: lblStatus.setIcon(STATUS_READY); break;
			case UPDATE: lblStatus.setIcon(STATUS_UPDATE); break;
			case ERROR: lblStatus.setIcon(STATUS_UPDATE); break;
			case UNKNOWN:
			default:
				lblStatus.setIcon(null);
		}

		if (isSelected) {
			System.out.println("Selected: " + list.getSelectionBackground().toString() + " " + list.getSelectionForeground());
			adjustColors(list.getSelectionBackground(), list.getSelectionForeground(), this, lblIcon, lblServerName, lblMCVersion, lblPackVersion);
		} else {
			System.out.println(list.getBackground().toString() + " " + list.getForeground());
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
