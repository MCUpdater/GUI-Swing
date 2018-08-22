package org.mcupdater.gui;

import org.mcupdater.api.Version;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

public class SwingBrowser extends BrowserProxy {

	public SwingBrowser() {
		baseComponent = new JTextPane() {
			@Override
			protected InputStream getStream(URL url) throws IOException {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent", "MCUpdater/" + Version.VERSION);
				return conn.getInputStream();
			}
		};
		((JTextPane) baseComponent).setEditable(false);
		((JTextPane) baseComponent).setContentType("text/html");
		((JTextPane) baseComponent).addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Object o = Class.forName("java.awt.Desktop").getMethod("getDesktop", new Class[0]).invoke(null);
						o.getClass().getMethod("browse", new Class[] { URI.class }).invoke(o, e.getURL().toURI());
					} catch (Exception ex) {
						MainForm.getInstance().baseLogger.log(Level.SEVERE, "Error while trying to handle hyperlink", ex);
					}
				}
			}
		});
	}

	@Override
	public void navigate(final String navigateTo) {
		Thread async = new Thread("News update") {
			public void run() {
				try
				{
					((JTextPane) baseComponent).setPage(navigateTo);
				} catch ( IOException e	)
				{
					MainForm.getInstance().baseLogger.log(Level.SEVERE, "Unable to load URL: " + navigateTo, e);
					((JTextPane) baseComponent).setDocument(((JTextPane) baseComponent).getEditorKit().createDefaultDocument());
					((JTextPane) baseComponent).setText("<HTML><BODY>Unable to load page</BODY></HTML>");
				}
			}
		};
		async.setDaemon(true);
		async.start();
	}

	@Override
	public boolean isModern() {
		return false;
	}
}
