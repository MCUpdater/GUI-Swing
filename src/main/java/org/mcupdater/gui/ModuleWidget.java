package org.mcupdater.gui;

import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Module;
import org.mcupdater.settings.SettingsManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ModuleWidget extends JPanel {
	private Module entry;
	private boolean selected;
	private List<ModuleWidget> dependents = new ArrayList<>();
	private String description;
	private boolean init;

	public ModuleWidget(Module module, Boolean overrideDefault, Boolean overrideValue) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.entry = module;
		init = true;
		if (!entry.getRequired() || SettingsManager.getInstance().getSettings().isProfessionalMode()) {
			final JCheckBox chkModule = new JCheckBox(this.entry.getName());
			chkModule.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (selected != chkModule.isSelected()) {
						selected = chkModule.isSelected();
						if (!init) MainForm.getInstance().setDirty();
					}
					for (String modid : entry.getDepends().split(" ")) {
						for (ModuleWidget entry : MainForm.getInstance().modPanel.getModules()) {
							if (entry.entry.getId().equals(modid)) {
								if (selected) {
									entry.setSelected(true);
								} else {
									entry.checkDependents();
								}
							}
						}
					}
				}
			});
			if (this.entry.getMeta().containsKey("description")) {
				description = this.entry.getMeta().get("description");
				updateToolTip(chkModule);
			}
			if (entry.getRequired() || (entry.getIsDefault() && !overrideDefault)) {
				chkModule.setSelected(true);
			}
			if (overrideDefault) {
				chkModule.setSelected(overrideValue);
			}
			this.add(chkModule);
		} else {
			JLabel reqMod = new JLabel(this.entry.getName());
			this.add(reqMod);
			if (this.entry.getMeta().containsKey("description")) {
				description = this.entry.getMeta().get("description");
				updateToolTip(reqMod);
			}
			this.selected = true;
		}
		if (entry.hasSubmodules()) {
			for (GenericModule sm : entry.getSubmodules()) {
				JLabel smLabel = new JLabel("  -- " + sm.getName());
				this.add(smLabel);
				if (sm.getMeta().containsKey("description")) {
					smLabel.setToolTipText(splitMulti(sm.getMeta().get("description")));
				}
			}
		}
		init = false;
	}

	private void updateToolTip(JComponent target) {
		target.setToolTipText(splitMulti(description));
	}

	public void checkDependents() {
		for (Component component : this.getComponents()) {
			if (component instanceof JCheckBox) {
				JCheckBox chkModule = (JCheckBox) component;
				boolean shouldDisable = false;
				for (ModuleWidget entry : dependents) {
					if (entry.isSelected()) {
						shouldDisable = true;
					}
				}
				chkModule.setEnabled(!shouldDisable);
			}
		}
	}

	public boolean isSelected() {
		return selected;
	}

	private String splitMulti(String input) {
		if (input == null) return null;
		StringTokenizer tok = new StringTokenizer(input, " ");
		StringBuilder output = new StringBuilder();
		int lineLen = 0;
		output.append("<html>");
		while (tok.hasMoreTokens()) {
			String word = tok.nextToken();

			if (lineLen + word.length() > 40) {
				output.append("<br>");
				lineLen = 0;
			}
			output.append(word).append(" ");
			lineLen += word.length();
		}
		output.append("</html>");
		return output.toString();
	}

	public void setSelected(boolean selected) {
		for (Component component : this.getComponents()) {
			if (component instanceof JCheckBox) {
				((JCheckBox) component).setSelected(selected);
			}
		}
		checkDependents();
	}

	public void addDependent(ModuleWidget entry) {
		this.dependents.add(entry);
		if (entry.isSelected()) {
			setSelected(true);
		}
	}

	public Module getModule() {
		return this.entry;
	}
}
