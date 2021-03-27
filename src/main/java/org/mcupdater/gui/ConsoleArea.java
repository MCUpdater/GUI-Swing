package org.mcupdater.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsoleArea extends JTextPane {
	private final StyledDocument doc = this.getStyledDocument();
	public final Style infoStyle = doc.addStyle("Info", null);
	public final Style warnStyle = doc.addStyle("Warning", null);
	public final Style errorStyle = doc.addStyle("Error", null);
	public final Style genericStyle = doc.addStyle("Generic", null);
	private final ConcurrentLinkedQueue<Entry> logQueue = new ConcurrentLinkedQueue<>();

	public ConsoleArea() {
		this.setBackground(Color.darkGray);
		StyleConstants.setForeground(infoStyle, new Color(0x00aa00));
		StyleConstants.setForeground(warnStyle, new Color(0xaaaa00));
		StyleConstants.setForeground(errorStyle, Color.red);
		StyleConstants.setForeground(genericStyle, Color.BLACK);
		this.setEditable(false);
		//this.getDocument().addDocumentListener(new LimitLinesDocumentListener(200));
		this.startQueue();
	}

	private void startQueue() {
		Thread queueDaemon = new Thread() {
			@SuppressWarnings("InfiniteLoopStatement")
			@Override
			public void run() {
				while (true) {
					Entry current = logQueue.poll();
					if (current != null) {
						try {
							doc.insertString(doc.getLength(), current.msg, current.style);
							setCaretPosition(doc.getLength());
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} else {
						while (doc.getDefaultRootElement().getElementCount() > 200) {
							Element line = doc.getDefaultRootElement().getElement(0);
							try {
								doc.remove(0,line.getEndOffset());
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		queueDaemon.setDaemon(true);
		queueDaemon.start();
	}

	public void log(String msg) {
		log(msg, null);
	}

	public void log(String msg, Style a) {
		logQueue.add(new Entry(msg, a));
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return getUI().getPreferredSize(this).width <= getParent().getSize().width;
	}

	private class Entry {
		public String msg;
		public Style style;

		Entry(String msg, Style style) {
			this.msg = msg;
			this.style = style;
		}
	}
}
