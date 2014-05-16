package org.mcupdater.gui;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mcupdater.api.Version;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    }

    @Override
    public void navigate(String navigateTo) {
        try {
            ((JTextPane) baseComponent).setPage(navigateTo);
        } catch (IOException e) {
            MainForm.getInstance().baseLogger.severe(ExceptionUtils.getStackTrace(e));
        }
    }
}
