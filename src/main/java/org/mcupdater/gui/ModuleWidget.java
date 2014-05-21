package org.mcupdater.gui;

import org.mcupdater.model.GenericModule;
import org.mcupdater.model.Module;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.StringTokenizer;

public class ModuleWidget extends JPanel {
	private Module entry;
	private Boolean isSelected;

	public ModuleWidget(Module module, Boolean overrideDefault, Boolean overrideValue) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.entry = module;
		if (!entry.getRequired()) {
			final JCheckBox chkModule = new JCheckBox(this.entry.getName());
			chkModule.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					isSelected = chkModule.isSelected();
				}
			});
			if (this.entry.getMeta().containsKey("description")) {
				chkModule.setToolTipText(splitMulti(this.entry.getMeta().get("description")));
			}
			if (entry.getRequired() || entry.getIsDefault()) {
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
				reqMod.setToolTipText(splitMulti(this.entry.getMeta().get("description")));
			}
			this.isSelected = true;
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
	}

	public Boolean getIsSelected() {
		return isSelected;
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
			output.append(word + " ");
			lineLen += word.length();
		}
		output.append("</html>");
		return output.toString();
	}
}
