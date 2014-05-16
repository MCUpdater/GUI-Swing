package org.mcupdater.gui;

import javax.swing.*;
import java.net.URL;

public abstract class BrowserProxy {

    protected JComponent baseComponent;

    public static BrowserProxy createProxy() {
        try {
            Class.forName("javafx.scene.web.WebView");
            return new JFXBrowser();
        } catch (ClassNotFoundException e) {
            return new SwingBrowser();
        }
    }

    public abstract void navigate(URL navigateTo);

    public JComponent getBaseComponent() {
        return baseComponent;
    }
}
