package org.mcupdater.gui;

import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Loader;
import org.mcupdater.model.Module;
import org.mcupdater.settings.SettingsManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class LoaderWidget extends JPanel {
	private Loader entry;
	private boolean selected;
	private List<LoaderWidget> dependents = new ArrayList<>();
	private String description;
	private boolean init;

	public LoaderWidget(Loader loader) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.entry = loader;
		final JLabel lblLoader = new JLabel(this.entry.getFriendlyName());
		this.add(lblLoader);
	}

	public Loader getLoader() {
		return this.entry;
	}
}
