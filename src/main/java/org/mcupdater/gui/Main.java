package org.mcupdater.gui;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Main {
	public static List<String> passthroughArgs;
	private static String defaultPackURL;
	public static ConsoleForm mcuConsole;

	public static void main(String[] args) {
		OptionParser optParser = new OptionParser();
		optParser.allowsUnrecognizedOptions();
		ArgumentAcceptingOptionSpec<String> packSpec = optParser.accepts("ServerPack").withRequiredArg().ofType(String.class);
		ArgumentAcceptingOptionSpec<File> rootSpec = optParser.accepts("MCURoot").withRequiredArg().ofType(File.class);
		NonOptionArgumentSpec<String> nonOpts = optParser.nonOptions();
		OptionSet options = optParser.parse(args);
		passthroughArgs = options.valuesOf(nonOpts);
		MCUpdater.getInstance(options.valueOf(rootSpec));
		setDefaultPackURL(options.valueOf(packSpec));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SettingsManager.getInstance().loadSettings();
				MCUpdater.getInstance().setInstanceRoot(new File(SettingsManager.getInstance().getSettings().getInstanceRoot()).toPath());
				try {
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
					if (UIManager.getLookAndFeel().getName().equals("Metal")) {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					mcuConsole = new ConsoleForm("MCU Console");
					new MainForm();
				} catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void setDefaultPackURL(String defaultPackURL) {
		Main.defaultPackURL = defaultPackURL;
	}

	public static String getDefaultPackURL() {
		return defaultPackURL;
	}
}
