package org.mcupdater.gui;

import org.mcupdater.model.Module;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModulePanel extends JPanel {
	private List<ModuleWidget> modules = new ArrayList<>();

	public void reload(List<Module> modList, Map<String, Boolean> optionalMods) {
		this.setVisible(false);
		this.removeAll();
		modules = new ArrayList<>();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for (Module entry : modList) {
			ModuleWidget newEntry;
			if (optionalMods.containsKey(entry.getId())) {
				newEntry = new ModuleWidget(entry, true, optionalMods.get(entry.getId()));
			} else {
				newEntry = new ModuleWidget(entry, false, false);
			}
			this.add(newEntry);
			modules.add(newEntry);
		}
		this.setVisible(true);
	}

	public List<ModuleWidget> getModules() {
		return modules;
	}
}
