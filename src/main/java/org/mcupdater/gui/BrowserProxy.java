package org.mcupdater.gui;

import javax.swing.*;

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

    public abstract void navigate(String navigateTo);

    public JComponent getBaseComponent() {
        return baseComponent;
    }
}
