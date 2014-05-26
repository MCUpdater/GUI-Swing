package org.mcupdater.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ProgressView extends JPanel {

	private final Map<MultiKey, ProgressItem> items = new HashMap<>();

	public ProgressView() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(new LineBorder(Color.BLACK, 2));
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

		public ProgressItem(String jobName, String parentId) {
			active = true;
			this.setLayout(new BorderLayout());
			lblName = new JLabel(parentId + " - " + jobName);
			pbProgress = new JProgressBar(0,1000);
			lblStatus = new JLabel("Inactive");
			btnDismiss = new JButton(new ImageIcon(this.getClass().getResource("remove.png")));
			btnDismiss.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {

				}
			});
			btnDismiss.setEnabled(false);
			JPanel pnlStatus = new JPanel(new FlowLayout());
			pnlStatus.add(lblStatus);
			pnlStatus.add(btnDismiss);
			this.add(lblName, BorderLayout.WEST);
			this.add(pbProgress, BorderLayout.CENTER);
			this.add(pnlStatus, BorderLayout.EAST);
			this.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
		}
	}
}
