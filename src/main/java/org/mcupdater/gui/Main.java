package org.mcupdater.gui;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.mcupdater.util.MCUpdater;

import javax.swing.*;
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
		ArgumentAcceptingOptionSpec<String> memSpec = optParser.accepts("defaultMem").withRequiredArg().ofType(String.class);
		ArgumentAcceptingOptionSpec<String> permSpec = optParser.accepts("defaultPerm").withRequiredArg().ofType(String.class);
		//optParser.accepts("syslf","Use OS-specific look and feel");
		NonOptionArgumentSpec<String> nonOpts = optParser.nonOptions();
		final OptionSet options = optParser.parse(args);
		passthroughArgs = options.valuesOf(nonOpts);
		MCUpdater.getInstance(options.valueOf(rootSpec));
		if (options.has(memSpec)) {
			MCUpdater.defaultMemory = options.valueOf(memSpec);
		}
		if (options.has(permSpec)) {
			MCUpdater.defaultPermGen = options.valueOf(permSpec);
		}
		setDefaultPackURL(options.valueOf(packSpec));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					/*
					if (options.has("syslf")) {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}   else {
						for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
							System.out.println("Installed L&F: " + info.getName());
							if ("Nimbus".equals(info.getName())) {
								UIManager.setLookAndFeel(info.getClassName());
								break;
							}
						}
						if (UIManager.getLookAndFeel().getName().equals("Metal")) {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						}
					}
					*/
					UIManager.addAuxiliaryLookAndFeel(new LookAndFeel() {
						private final UIDefaults defaults = new UIDefaults() {
							@Override
							public javax.swing.plaf.ComponentUI getUI(JComponent c) {
								if (c instanceof javax.swing.text.JTextComponent) {
									if (c.getClientProperty(this) == null) {
										c.setComponentPopupMenu(TextContextMenu.INSTANCE);
										c.putClientProperty(this, Boolean.TRUE);
									}
								}
								return null;
							}
						};
						@Override public UIDefaults getDefaults() { return defaults; }
						@Override public String getID() { return "TextContextMenu"; }
						@Override public String getName() { return getID(); }
						@Override public String getDescription() { return getID(); }
						@Override public boolean isNativeLookAndFeel() { return false; }
						@Override public boolean isSupportedLookAndFeel() { return true; }
					});
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
