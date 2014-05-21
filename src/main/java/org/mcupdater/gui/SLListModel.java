package org.mcupdater.gui;

import org.mcupdater.model.ServerList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SLListModel extends AbstractListModel<ServerList> {
	private final ArrayList<ServerList> model;

	public SLListModel() {
		model = new ArrayList<>();
	}

	public int getEntryIdByTag(String tag) {
		int foundId = 0;
		int searchId = 0;
		for (ServerList entry : model) {
			if (tag.equals(entry.getServerId())) {
				foundId = searchId;
				break;
			}
			searchId++;
		}
		return foundId;
	}

	@Override
	public int getSize() {
		return model.size();
	}

	@Override
	public ServerList getElementAt(int index) {
		return model.get(index);
	}

	public void add(ServerList element) {
		model.add(element);
		fireContentsChanged(this, 0, getSize());
	}

	public Iterator<ServerList> iterator() {
		return model.iterator();
	}

	public boolean removeElement(ServerList element) {
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

	public void clearAndSet(List<ServerList> newList) {
		model.clear();
		model.addAll(newList);
		fireContentsChanged(this, 0, getSize());
	}
}
