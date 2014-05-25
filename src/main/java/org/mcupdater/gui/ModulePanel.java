package org.mcupdater.gui;

import org.mcupdater.model.Module;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;

public class ModulePanel extends JPanel {
	private Map<String,ModuleWidget> modules = new HashMap<>();

	public void reload(List<Module> modList, Map<String, Boolean> optionalMods) {
		this.setVisible(false);
		this.removeAll();
		modules = new HashMap<>();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for (Module entry : modList) {
			ModuleWidget newEntry;
			if (optionalMods.containsKey(entry.getId())) {
				newEntry = new ModuleWidget(entry, true, optionalMods.get(entry.getId()));
			} else {
				newEntry = new ModuleWidget(entry, false, false);
			}
			this.add(newEntry);
			modules.put(entry.getId(), newEntry);
		}
		for (Entry<String,ModuleWidget> entry : modules.entrySet()) {
			if (!entry.getValue().getModule().getDepends().isEmpty()){
				for (String modid : entry.getValue().getModule().getDepends().split(" ")) {
					modules.get(modid).addDependent(entry.getValue());
				}
			}
		}
		this.setVisible(true);
	}

	public Collection<ModuleWidget> getModules() {
		return modules.values();
	}
}
