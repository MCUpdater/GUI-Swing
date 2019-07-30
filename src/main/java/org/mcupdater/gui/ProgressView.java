package org.mcupdater.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ProgressView extends JPanel {

	private final Map<MultiKey, ProgressItem> items = new HashMap<>();
	protected ProgressView container;

	public ProgressView() {
		container = this;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	public synchronized void addProgressBar(String jobName, String parentId) {
		MultiKey key = new MultiKey(parentId, jobName);
		if (items.containsKey(key)) {
			this.remove(items.get(key)); // Remove old row if one exists
		}
		ProgressItem newItem = new ProgressItem(jobName, parentId);
		this.add(newItem);
		items.put(key, newItem);
		this.invalidate();
		this.validate();
		this.repaint();
	}

	public synchronized void updateProgress(final String jobName, final String parentId, float newProgress, int totalFiles, int successfulFiles) {
		ProgressItem bar = items.get(new MultiKey(parentId, jobName));
		if (bar == null) { return; }
		bar.setProgress(newProgress, totalFiles, successfulFiles );
	}

	public int getActiveCount() {
		int activeCount = 0;
		synchronized (this) {
			for (Map.Entry<MultiKey, ProgressItem> item : items.entrySet()) {
				if (item.getValue().isActive()) {
					activeCount++;
				}
			}
		}
		return activeCount;
	}

	public int getActiveById(String serverId) {
		int activeCount = 0;
		synchronized (this) {
			for (Map.Entry<MultiKey, ProgressItem> item : items.entrySet()) {
				if (item.getKey().getParent().equals(serverId)) {
					if (item.getValue().isActive()) {
						activeCount++;
					}
				}
			}
		}
		return activeCount;
	}

	private class MultiKey
	{
		private final String parent;
		private final String job;

		public MultiKey(String parent, String job){
			this.parent = parent;
			this.job = job;
		}

		public String getParent(){
			return parent;
		}

		public String getJob(){
			return job;
		}

		@Override
		public String toString() { return parent + "/" + job; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof MultiKey)) return false;
			MultiKey key = (MultiKey) o;
			return parent.equals(key.parent) && job.equals(key.job);
		}

		@Override
		public int hashCode() {
			int result = parent.hashCode();
			result = 31 * result + job.hashCode();
			return result;
		}
	}

	private class ProgressItem extends JPanel {
        private final JLabel lblName;
		private final JProgressBar pbProgress;
		private final JLabel lblStatus;
		private final JButton btnDismiss;
		private boolean active;

		public ProgressItem(final String jobName, final String parentId) {
			active = true;
			this.setLayout(new BorderLayout());
			lblName = new JLabel(parentId + " - " + jobName);
			lblName.setVerticalTextPosition(JLabel.CENTER);
			lblName.setBorder(new EmptyBorder(5, 5, 5, 5));
			pbProgress = new JProgressBar(0,10000);
			lblStatus = new JLabel("Inactive");
			lblStatus.setVerticalTextPosition(JLabel.CENTER);
			lblStatus.setBorder(new EmptyBorder(5, 5, 5, 5));
			btnDismiss = new JButton(new ImageIcon(this.getClass().getResource("remove.png")));
			btnDismiss.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					MultiKey key = new MultiKey(parentId, jobName);
					ProgressItem self = items.get(key);
					items.remove(key);
					container.remove(self);
					container.invalidate();
					container.validate();
					container.repaint();
				}
			});
			btnDismiss.setEnabled(false);
			btnDismiss.setBorder(new EmptyBorder(5, 5, 5, 5));
			JPanel pnlStatus = new JPanel();
			pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.LINE_AXIS));
			pnlStatus.add(lblStatus);
			pnlStatus.add(btnDismiss);
			this.add(lblName, BorderLayout.WEST);
			this.add(pbProgress, BorderLayout.CENTER);
			this.add(pnlStatus, BorderLayout.EAST);
			this.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
		}

		public void setProgress(final float progress, final int totalFiles, final int successfulFiles) {
			EventQueue.invokeLater(new Runnable(){
				@Override
				public void run() {
					pbProgress.setValue((int) (progress * 10000.0F));
					lblStatus.setText(String.format("%d/%d downloaded",successfulFiles,totalFiles)); //TODO: i18n
					if (progress >= 1) {
						if (successfulFiles == totalFiles) {
							lblStatus.setText("Finished"); //TODO: i18n
						} else {
							lblStatus.setText((totalFiles - successfulFiles) + " failed!"); //TODO: i18n
						}
						btnDismiss.setEnabled(true);
						active = false;
					}
				}
			});
		}

		public boolean isActive() {
			return active;
		}
	}
}
