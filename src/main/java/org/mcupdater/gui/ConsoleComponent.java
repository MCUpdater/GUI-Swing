package org.mcupdater.gui;

import javax.swing.*;

public class ConsoleComponent {
	protected final ConsoleArea console;
	protected final JScrollPane scroller;
	protected String title;
	protected boolean closeable;

	public ConsoleComponent(String title) {
		this.console = new ConsoleArea();
		this.scroller = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.title = title;
		this.closeable = false;
	}

	public ConsoleArea getConsole() {
		return this.console;
	}

	public JScrollPane getScroller() { return this.scroller; }

	public void setTitle(String title) { this.title = title; }

	public String getTitle() { return this.title;}

	public void setCloseable(boolean newValue) { this.closeable = newValue; }

	public boolean isCloseable() { return this.closeable; }
}
