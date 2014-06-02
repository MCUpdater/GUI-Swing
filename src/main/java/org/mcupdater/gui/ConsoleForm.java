package org.mcupdater.gui;

import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class ConsoleForm {
	private final CALogHandler consoleHandler;
	private final ConsoleArea console;
	private final JFrame window;

	public ConsoleForm(String title) {
		window = new JFrame();
		window.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		window.setTitle(title);
		window.setBounds(25, 25, 500, 500);
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		console = new ConsoleArea();
		window.getContentPane().setLayout(new BorderLayout());
		JScrollPane scroller = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		window.getContentPane().add(scroller, BorderLayout.CENTER);
		consoleHandler = new CALogHandler(console);
		consoleHandler.setLevel(Level.INFO);
		window.setVisible(true);
		MCUpdater.apiLogger.addHandler(consoleHandler);
	}

	public ConsoleArea getConsole() {
		return console;
	}

	public CALogHandler getHandler() {
		return consoleHandler;
	}

	public void allowClose() {
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
