package org.mcupdater.gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class JFXBrowser extends BrowserProxy {

    public static WebEngine webEngine;

    public JFXBrowser() {
        baseComponent = new JFXPanel();

        Platform.runLater(new Runnable() { // this will run initFX as JavaFX-Thread
            @Override
            public void run() {
                initFX((JFXPanel) baseComponent);
            }
        });
    }

    private static void initFX(final JFXPanel fxPanel) {
        ExtensibleRegion group = new ExtensibleRegion();
        Scene scene = new Scene(group);
        fxPanel.setScene(scene);
        WebView webView = new WebView();
        group.add(webView);
        webEngine = webView.getEngine();
    }

    @Override
    public void navigate(final String navigateTo) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                webEngine.load(navigateTo);
            }
        });
    }

    private static class ExtensibleRegion extends Region {
        @Override
        protected void layoutChildren() {
            for (Node node : getChildren()) {
                layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
            }
        }

        public void add(Node newComponent) {
            getChildren().add(newComponent);
        }
    }
}
