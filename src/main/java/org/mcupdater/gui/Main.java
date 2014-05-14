package org.mcupdater.gui;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;

public class Main
{
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable()
		{
			public void run() {
				try {
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
					if (UIManager.getLookAndFeel().getName().equals("Metal")) {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					new MainForm();
				} catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
