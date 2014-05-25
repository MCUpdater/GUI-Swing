package org.mcupdater.gui;

import org.mcupdater.settings.Profile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileModel extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {
	private final List<Profile> model;
	private Profile selected;

	public ProfileModel() {
		model = new ArrayList<>();
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public Profile getElementAt(int index) {
		return model.get(index);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.selected = (Profile) anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

	public void add(Profile entry) {
		model.add(entry);
		fireContentsChanged(this, 0, model.size());
	}

	public boolean removeElement(Profile element) {
		boolean removed = model.remove(element);
		if (removed) {
			fireContentsChanged(this, 0, getSize());
		}
		return removed;
	}

	public void clear() {
		model.clear();
		fireContentsChanged(this, 0, getSize());
	}

	public void clearAndSet(List<Profile> newList) {
		model.clear();
		model.addAll(newList);
		fireContentsChanged(this, 0, getSize());
	}

	public List<Profile> getData() {
		return model;
	}
}
