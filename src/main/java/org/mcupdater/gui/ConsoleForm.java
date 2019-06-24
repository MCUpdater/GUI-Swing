package org.mcupdater.gui;

import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class ConsoleForm extends ConsoleComponent {
	private final JDialog window;

	public ConsoleForm(String title) {
		super(title);
		window = new JDialog();
		window.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		window.setTitle(getTitle());
		window.setBounds(25, 25, 500, 500);
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.getContentPane().setLayout(new BorderLayout());
		window.getContentPane().add(scroller, BorderLayout.CENTER);
		window.setVisible(true);
	}

	public void allowClose(boolean newValue) {
		this.setCloseable(newValue);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void closeConsole() {
		window.dispose();
	}
}
