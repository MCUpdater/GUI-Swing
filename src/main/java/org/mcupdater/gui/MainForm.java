package org.mcupdater.gui;

import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends MCUApp
{
	private static MainForm window;
	private JFrame frameMain;
	private JPanel panelTop;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JPanel panelLeftButtons;
	private JPanel panelRightButtons;
	private JButton button4;
	private JButton button5;
	private JButton button6;

	public MainForm() {
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		Version.setApp(this);
		window = this;
		initGui();
		frameMain.setVisible(true);
	}

	// Section - GUI elements

	public void initGui() {
		frameMain = new JFrame();
		frameMain.setTitle("MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
		frameMain.setBounds(100, 100, 1175, 592);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panelTop = new JPanel();
		panelTop.setLayout(new BorderLayout(0,0));
		panelLeftButtons = new JPanel();
		panelLeftButtons.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		panelTop.add(panelLeftButtons, BorderLayout.WEST);
		button1 = new JButton();
		button1.setText("A");
		button2 = new JButton();
		button2.setText("B");
		button3 = new JButton();
		button3.setText("C");
		panelLeftButtons.add(button1);
		panelLeftButtons.add(button2);
		panelLeftButtons.add(button3);

		panelRightButtons = new JPanel();
		panelRightButtons.setLayout(new FlowLayout(FlowLayout.RIGHT,0,0));
		panelTop.add(panelRightButtons, BorderLayout.EAST);
		button4 = new JButton();
		button4.setText("1");
		button5 = new JButton();
		button5.setText("2");
		button6 = new JButton();
		button6.setText("3");
		panelRightButtons.add(button4);
		panelRightButtons.add(button5);
		panelRightButtons.add(button6);
		frameMain.getContentPane().add(panelTop, BorderLayout.NORTH);
	}

	// Section - Logic elements

	@Override
	public void setStatus(String string) {

	}

	@Override
	public void log(String msg) {

	}

	@Override
	public Profile requestLogin(String username) {
		return null;
	}

	@Override
	public void addServer(ServerList entry) {

	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		return null;
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		return null;
	}

}
