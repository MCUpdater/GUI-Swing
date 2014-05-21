package org.mcupdater.gui;

import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class ConsoleForm {
	private static ConsoleForm INSTANCE;
	private final CALogHandler consoleHandler;
	private final ConsoleArea console;

	public ConsoleForm() {
		INSTANCE = this;
		JFrame window = new JFrame();
		window.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		window.setTitle("MCU Console");
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

	public static ConsoleArea getConsole() {
		return INSTANCE.console;
	}

	public static CALogHandler getHandler() {
		return INSTANCE.consoleHandler;
	}
}
