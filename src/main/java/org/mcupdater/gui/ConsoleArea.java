package org.mcupdater.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class ConsoleArea extends JTextPane {
	private StyledDocument doc = this.getStyledDocument();
	public Style infoStyle = doc.addStyle("Info", null);
	public Style warnStyle = doc.addStyle("Warning", null);
	public Style errorStyle = doc.addStyle("Error", null);

	public ConsoleArea() {
		this.setBackground(Color.white);
		StyleConstants.setForeground(infoStyle, new Color(0x007700));
		StyleConstants.setForeground(warnStyle, new Color(0xaaaa00));
		StyleConstants.setForeground(errorStyle, Color.red);
	}

	public void log(String msg) {
		try {
			doc.insertString(doc.getLength(), msg, null);
			setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void log(String msg, Style a) {
		try {
			doc.insertString(doc.getLength(), msg, a);
			setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
