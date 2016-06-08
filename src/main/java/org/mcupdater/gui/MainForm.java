package org.mcupdater.gui;

import com.google.gson.Gson;
import com.mojang.authlib.UserType;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jdesktop.swingx.JXLoginPane;
import org.mcupdater.FMLStyleFormatter;
import org.mcupdater.MCUApp;
import org.mcupdater.api.Version;
import org.mcupdater.auth.MinecraftLoginService;
import org.mcupdater.auth.YggdrasilAuthManager;
import org.mcupdater.downloadlib.DownloadQueue;
import org.mcupdater.downloadlib.Downloadable;
import org.mcupdater.downloadlib.TrackerListener;
import org.mcupdater.instance.Instance;
import org.mcupdater.model.*;
import org.mcupdater.mojang.AssetIndex;
import org.mcupdater.mojang.AssetManager;
import org.mcupdater.mojang.Library;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsListener;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.MojangStatus;
import org.mcupdater.util.ServerPackParser;
import org.mcupdater.util.ServerStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Style;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainForm extends MCUApp implements SettingsListener, TrackerListener, ClipboardOwner {
	private static MainForm instance;
	private final MainForm self;
	private JFrame frameMain;
	private SLListModel slModel;
	private ProfileModel profileModel;
	private JList<ServerList> serverList;
	private JComboBox<Profile> cboProfiles;
	private final BrowserProxy newsBrowser;
	private JButton btnRefresh;
	private ServerList selected;
	private final Gson gson = new Gson();
	protected final ModulePanel modPanel = new ModulePanel();
	private final ImageIcon GREEN_FLAG = new ImageIcon(this.getClass().getResource("flag_green.png"));
	private final ImageIcon RED_FLAG = new ImageIcon(this.getClass().getResource("flag_red.png"));
	private JLabel lblServerStatus;
	private final ProgressView progressView = new ProgressView();
	private JButton btnUpdate;
	private JButton btnLaunch;
	private JButton btnAddURL;
	private JButton btnSettings;
	private int updateCounter = 0;
	private boolean playing;
	private JTabbedPane instanceTabs;
	private JScrollPane progressScroller;

	public MainForm() {
		self = this;
		this.baseLogger = Logger.getLogger("MCUpdater");
		baseLogger.setLevel(Level.ALL);
		FileHandler mcuHandler;
		try {
			mcuHandler = new FileHandler(MCUpdater.getInstance().getArchiveFolder().resolve("MCUpdater.log").toString(), 0, 3);
			mcuHandler.setFormatter(new FMLStyleFormatter());
			baseLogger.addHandler(mcuHandler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		baseLogger.addHandler(Main.mcuConsole.getHandler());
		Version.setApp(this);
		MCUpdater.getInstance().setParent(this);
		instance = this;
		SettingsManager.getInstance().addListener(this);
		baseLogger.info("Activate interlocks!");
		//SettingsManager.getInstance().loadSettings();
		MCUpdater.getInstance().setInstanceRoot(new File(SettingsManager.getInstance().getSettings().getInstanceRoot()).toPath());
		newsBrowser = BrowserProxy.createProxy();
		initGui();
		baseLogger.info("Dynatherms connected!");
		this.setAuthManager(new YggdrasilAuthManager());
		bindLogic();
		baseLogger.info("Infracells up!");
		if (!SettingsManager.getInstance().getSettings().getPackURLs().contains(Main.getDefaultPackURL())) {
			SettingsManager.getInstance().getSettings().addPackURL(Main.getDefaultPackURL());
			SettingsManager.getInstance().saveSettings();
		}
		if (SettingsManager.getInstance().getSettings().getProfiles().size() == 0) {
			Profile newProfile = requestLogin("");
			if (newProfile != null) {
				SettingsManager.getInstance().getSettings().addOrReplaceProfile(newProfile);
				SettingsManager.getInstance().getSettings().setLastProfile(newProfile.getName());
				SettingsManager.getInstance().fireSettingsUpdate();
				SettingsManager.getInstance().saveSettings();
			}
		}
		settingsChanged(SettingsManager.getInstance().getSettings());
		frameMain.setVisible(true);
		baseLogger.info("Megathrusters are go!");
		if (serverList.getModel().getSize() > 0 && serverList.getSelectedIndex() < 0) {
			serverList.setSelectedIndex(0);
		}
		Thread daemonMonitor = new Thread() {
			private ServerList currentSelection;
			private int activeJobs = 0;
			private boolean playState;
			private boolean needsRefresh;

			@SuppressWarnings("InfiniteLoopStatement")
			@Override
			public void run() {
				while (true) {
					try {
						if (activeJobs != progressView.getActiveCount() || currentSelection != serverList.getSelectedValue() || playState != isPlaying()) {
							currentSelection = serverList.getSelectedValue();
							activeJobs = progressView.getActiveCount();
							playState = isPlaying();
							SwingUtilities.invokeAndWait( new Runnable() {
								@Override
								public void run() {
									instanceTabs.setTitleAt(instanceTabs.indexOfComponent(progressScroller),"Progress - " + activeJobs + " active");
									if (activeJobs > 0) {
										btnLaunch.setEnabled(false);
										needsRefresh = true;
									} else {
										if (needsRefresh) {
											refreshInstanceList();
											needsRefresh = false;
										}
										if (!(currentSelection == null) && !playState && currentSelection.getState().equals(ServerList.State.READY)) {
											btnLaunch.setEnabled(true);
										} else {
											btnLaunch.setEnabled(false);
										}
									}
									if (!(currentSelection == null)) {
										if (progressView.getActiveById(currentSelection.getServerId()) > 0 || playState) {
											btnUpdate.setEnabled(false);
										} else {
											btnUpdate.setEnabled(true);
										}
									} else {
										btnUpdate.setEnabled(false);
									}

								}
							});
						}
						sleep(500);
					} catch (Exception e) {
						baseLogger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		};
		daemonMonitor.setDaemon(true);
		daemonMonitor.start();
	}

	// Section - GUI elements

	public void initGui() {
		frameMain = new JFrame();
		frameMain.setIconImage(new ImageIcon(this.getClass().getResource("mcu-icon.png")).getImage());
		frameMain.setTitle("MCUpdater " + Version.VERSION + Version.BUILD_LABEL);
		frameMain.setBounds(100, 100, 1175, 592);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Left Pane
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
		panelLeft.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelButtons = new JPanel();
			panelButtons.setLayout(new GridLayout(0, 3));
			{
				btnAddURL = new JButton();
				btnAddURL.setIcon(new ImageIcon(this.getClass().getResource("add.png")));
				btnAddURL.setToolTipText("Add URL");
				btnAddURL.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnAddURL.setHorizontalTextPosition(SwingConstants.CENTER);
				btnRefresh = new JButton();
				btnRefresh.setIcon(new ImageIcon(this.getClass().getResource("arrow_refresh.png")));
				btnRefresh.setToolTipText("Refresh");
				btnRefresh.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnRefresh.setHorizontalTextPosition(SwingConstants.CENTER);
				btnSettings = new JButton();
				btnSettings.setIcon(new ImageIcon(this.getClass().getResource("cog.png")));
				btnSettings.setToolTipText("Settings");
				btnSettings.setVerticalTextPosition(SwingConstants.BOTTOM);
				btnSettings.setHorizontalTextPosition(SwingConstants.CENTER);
				panelButtons.add(btnAddURL);
				panelButtons.add(btnRefresh);
				panelButtons.add(btnSettings);
			}
			panelLeft.add(panelButtons, BorderLayout.NORTH);

			slModel = new SLListModel();
			serverList = new JList<>();
			serverList.setModel(slModel);
			serverList.setCellRenderer(new ServerListCellRenderer());
			serverList.addListSelectionListener(new InstanceListener());
			serverList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						showInstanceMenu(serverList.getModel().getElementAt(serverList.locationToIndex(e.getPoint())), e.getX(), e.getY());
					}
				}
			});

			JScrollPane instanceScroller = new JScrollPane(serverList);
			panelLeft.add(instanceScroller, BorderLayout.CENTER);
		}
		frameMain.getContentPane().add(panelLeft, BorderLayout.WEST);

		// Center Pane
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane modScroller = new JScrollPane(modPanel);
			modScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			modScroller.getVerticalScrollBar().setUnitIncrement(16);
			progressScroller = new JScrollPane(progressView);
			progressScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			instanceTabs = new JTabbedPane();
			{
				instanceTabs.addTab("News", newsBrowser.getBaseComponent());
				instanceTabs.addTab("Mods", modScroller);
				instanceTabs.addTab("Progress", progressScroller);
				/*
				TODO: Implement new features
				instanceTabs.addTab("Changes", new JPanel());
				instanceTabs.addTab("Maintenance", new JPanel());
				*/
			}
			contentPanel.add(instanceTabs, BorderLayout.CENTER);

			JPanel panelBottom = new JPanel();
			panelBottom.setLayout(new BorderLayout());
			contentPanel.add(panelBottom, BorderLayout.SOUTH);

			JPanel panelStatus = new JPanel();
			panelStatus.setLayout(new GridBagLayout());
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 5, 0, 0);
				JLabel lblMojang = new JLabel("Mojang status -");
				JLabel lblAuth = new JLabel("Auth:");
				lblAuth.setIconTextGap(3);
				lblAuth.setHorizontalTextPosition(JLabel.LEFT);
				JLabel lblSession = new JLabel("Session:");
				lblSession.setIconTextGap(3);
				lblSession.setHorizontalTextPosition(JLabel.LEFT);
				MojangStatus current = MojangStatus.getMojangStatus();
				lblAuth.setIcon(current.getAuth() ? GREEN_FLAG : RED_FLAG);
				lblSession.setIcon(current.getSession() ? GREEN_FLAG : RED_FLAG);
				panelStatus.add(lblMojang, gbc);
				panelStatus.add(lblAuth, gbc);
				panelStatus.add(lblSession, gbc);
				JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
				sep.setPreferredSize(new Dimension(5, 1));
				gbc.fill = GridBagConstraints.VERTICAL;
				panelStatus.add(sep, gbc);
				gbc = new GridBagConstraints();
				lblServerStatus = new JLabel("");
				panelStatus.add(lblServerStatus, gbc);
			}
			panelBottom.add(panelStatus, BorderLayout.WEST);

			JPanel panelActions = new JPanel();
			panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			{
				profileModel = new ProfileModel();
				JLabel lblProfiles = new JLabel("Profile:");
				cboProfiles = new JComboBox<>(profileModel);
				btnUpdate = new JButton("Update");
				btnUpdate.setIcon(new ImageIcon(this.getClass().getResource("update.png")));
				btnLaunch = new JButton("Launch Minecraft");
				btnLaunch.setIcon(new ImageIcon(this.getClass().getResource("play.png")));

				panelActions.add(lblProfiles);
				panelActions.add(cboProfiles);
				panelActions.add(btnUpdate);
				panelActions.add(btnLaunch);
			}
			panelBottom.add(panelActions, BorderLayout.EAST);
		}
		frameMain.getContentPane().add(contentPanel, BorderLayout.CENTER);

	}

	private void showInstanceMenu(final ServerList context, int x, int y) {
		JPopupMenu mnuInstance = new JPopupMenu();
		JMenuItem openFolder = new JMenuItem("Open instance folder");
		openFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(MCUpdater.getInstance().getInstanceRoot().resolve(context.getServerId()).toFile());
				} catch (IOException ex) {
					baseLogger.log(Level.SEVERE, "An error occurred:", ex);
				}
			}
		});
		JMenuItem copyLink = new JMenuItem("Copy pack URL");
		copyLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection clipboardContents = new StringSelection(context.getPackUrl());
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(clipboardContents, instance);
			}
		});
		mnuInstance.add(openFolder);
		mnuInstance.add(copyLink);
		mnuInstance.show(serverList, x, y);
	}

	private void bindLogic() {
		btnAddURL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newUrl = JOptionPane.showInputDialog(frameMain, "URL for ServerPack XML", "MCUpdater", JOptionPane.PLAIN_MESSAGE);
				try {
					Document doc = ServerPackParser.readXmlFromUrl(newUrl);
					if (!(doc == null)) {
						Element parent = doc.getDocumentElement();
						if (parent.getNodeName().equals("ServerPack")) {
							log("ServerPack definition found for MCU version " + parent.getAttribute("version"));
							SettingsManager.getInstance().getSettings().addPackURL(newUrl);
							SettingsManager.getInstance().fireSettingsUpdate();
						} else {
							int response = JOptionPane.showConfirmDialog(frameMain, "File is either invalid or complies with MCU 1.0 format.\n\nDo you want to add this URL anyway?", "MCUpdater", JOptionPane.YES_NO_OPTION);
							if (response == JOptionPane.YES_OPTION) {
								SettingsManager.getInstance().getSettings().addPackURL(newUrl);
								SettingsManager.getInstance().fireSettingsUpdate();
							}
						}
						if (!SettingsManager.getInstance().isDirty()) {
							SettingsManager.getInstance().saveSettings();
						}
					} else {
						log("Unable to get server information from " + newUrl);
					}
				} catch (Exception e1) {
					baseLogger.warning("Problem reading from " + newUrl + ":\n" + ExceptionUtils.getStackTrace(e1));
				}
			}
		});
		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshInstanceList();
			}
		});
		btnSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog settings = new SettingsDialog(self);
				settings.setLocationRelativeTo(frameMain);
				settings.setVisible(true);
			}
		});
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnUpdate.setEnabled(false);
				try {
					Path instPath = Files.createDirectories(MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId()));
					Instance instData;
					final Path instanceFile = instPath.resolve("instance.json");
					try {
						BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
						instData = gson.fromJson(reader, Instance.class);
						reader.close();
					} catch (IOException ioe) {
						instData = new Instance();
					}
					Set<String> digests = new HashSet<>();
					List<Module> fullModList = new ArrayList<>(selected.getModules().values());
					for (Module mod : fullModList) {
						if (!mod.getMD5().isEmpty()) {
							digests.add(mod.getMD5());
						}
						for (ConfigFile cf : mod.getConfigs()) {
							if (!cf.getMD5().isEmpty()) {
								digests.add(cf.getMD5());
							}
						}
						for (GenericModule sm : mod.getSubmodules()) {
							if (!sm.getMD5().isEmpty()) {
								digests.add(sm.getMD5());
							}
						}
					}
					instData.setHash(MCUpdater.calculateGroupHash(digests));

					final List<GenericModule> selectedMods = new ArrayList<>();
					final List<ConfigFile> selectedConfigs = new ArrayList<>();
					for (ModuleWidget entry : modPanel.getModules()) {
						System.out.println(entry.getModule().getName() + " - " + entry.getModule().getModType().toString());
						if (entry.isSelected()) {
							selectedMods.add(entry.getModule());
							if (entry.getModule().hasConfigs()) {
								selectedConfigs.addAll(entry.getModule().getConfigs());
							}
							if (entry.getModule().hasSubmodules()) {
								selectedMods.addAll(entry.getModule().getSubmodules());
							}
						}
						if (!entry.getModule().getRequired()) {
							instData.setModStatus(entry.getModule().getId(), entry.isSelected());
						}
					}
					//TODO: Add hard update option
/*
					Map<String,String> libOverrides = new HashMap<>();
					try {
						URL libOverridesUrl = new URI("http://files.mcupdater.com/liboverrides.dat").toURL();
						BufferedReader reader = new BufferedReader(new InputStreamReader(libOverridesUrl.openStream()));
						String entry;
						while((entry = reader.readLine()) != null) {
							String key = StringUtils.join(Arrays.copyOfRange(entry.split(":"),0,2),":");
							libOverrides.put(key, entry);
						}
					} catch (URISyntaxException e1) {
						baseLogger.log(Level.SEVERE, "Unable to get library overrides!", e1);
					}
*/
                    System.out.println("overrides: " + selected.getLibOverrides().size());
					MCUpdater.getInstance().installMods(selected, selectedMods, selectedConfigs, instPath, false, instData, ModSide.CLIENT);
				} catch (IOException e1) {
					baseLogger.log(Level.SEVERE, "Unable to create directory for instance!", e1);
				}
			}
		});
		btnLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Do new launching
				btnLaunch.setEnabled(false);
				btnUpdate.setEnabled(false);
				setPlaying(true);
				if (!changeSelectedServer(selected)) {
					setPlaying(false);
					return;
				}
				Profile launchProfile = (Profile) profileModel.getSelectedItem();
				if (!(launchProfile == null)) {
					SettingsManager.getInstance().getSettings().setLastProfile(launchProfile.getName());
					SettingsManager.getInstance().getSettings().findProfile(launchProfile.getName()).setLastInstance(selected.getServerId());
					if (!SettingsManager.getInstance().isDirty()) {
						SettingsManager.getInstance().saveSettings();
					}
					if (selected.getLauncherType().equals("Legacy")) {
						try {
							tryOldLaunch(selected, launchProfile);
						} catch (Exception ex) {
							baseLogger.log(Level.SEVERE, ex.getMessage(), ex);
							JOptionPane.showMessageDialog(frameMain, ex.getMessage() + "\n\nNote: An authentication error can occur if your profile is out of sync with Mojang's servers.\nTry re-adding your profile in the Settings window to resync with Mojang.", "MCUpdater", JOptionPane.ERROR_MESSAGE);
							setPlaying(false);
						}
					} else {
						try {
							tryNewLaunch(selected, modPanel.getModules(), launchProfile);
						} catch (Exception ex) {
							baseLogger.log(Level.SEVERE, ex.getMessage(), ex);
							JOptionPane.showMessageDialog(frameMain, ex.getMessage() + "\n\nNote: An authentication error can occur if your profile is out of sync with Mojang's servers.\nTry re-adding your profile in the Settings window to resync with Mojang.", "MCUpdater", JOptionPane.ERROR_MESSAGE);
							setPlaying(false);
						}
					}
				}
			}
		});
		cboProfiles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (cboProfiles.getSelectedIndex() > -1) {
					setSelectedInstance(((Profile) cboProfiles.getSelectedItem()).getLastInstance());
				}
			}
		});
	}

	private void tryNewLaunch(final ServerList selected, Collection<ModuleWidget> modules, Profile user) throws Exception {
		Path javaPath = getJava();
		String playerName = user.getName();
		String sessionKey = user.getSessionKey(this);
		MinecraftVersion mcVersion = MinecraftVersion.loadVersion(selected.getVersion());
		String indexName = mcVersion.getAssets();
		if (indexName == null) {
			indexName = "legacy";
		}
		String mainClass;
		List<String> args = new ArrayList<>();
		StringBuilder clArgs = new StringBuilder(mcVersion.getMinecraftArguments());
		List<String> libs = new ArrayList<>();
		MCUpdater mcu = MCUpdater.getInstance();
		File indexesPath = mcu.getArchiveFolder().resolve("assets").resolve("indexes").toFile();
		File indexFile = new File(indexesPath, indexName + ".json");
		String json;
		json = FileUtils.readFileToString(indexFile);
		AssetIndex index = gson.fromJson(json, AssetIndex.class);
		final Settings settings = SettingsManager.getInstance().getSettings();
		if (settings.isFullScreen()) {
			clArgs.append(" --fullscreen");
		} else {
			clArgs.append(" --width ").append(settings.getResWidth()).append(" --height ").append(settings.getResHeight());
		}
		if (settings.isAutoConnect() && selected.isAutoConnect()) {
			URI address;
			try {
				address = new URI("my://" + selected.getAddress());
				clArgs.append(" --server ").append(address.getHost());
				if (address.getPort() != -1) {
					clArgs.append(" --port ").append(address.getPort());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		clArgs.append(" --resourcePackDir ${resource_packs}");
		if (!settings.getProgramWrapper().isEmpty()) {
			args.add(settings.getProgramWrapper());
		}
		args.add(javaPath.toString());
		args.add("-Xms" + settings.getMinMemory());
		args.add("-Xmx" + settings.getMaxMemory());
		args.add("-XX:PermSize=" + settings.getPermGen());
		if (!settings.getJvmOpts().isEmpty()) {
			args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
		}
		if (System.getProperty("os.name").startsWith("Mac")) {
			args.add("-Xdock:icon=" + mcu.getArchiveFolder().resolve("assets").resolve("icons").resolve("minecraft.icns").toString());
			args.add("-Xdock:name=Minecraft(MCUpdater)");
		}
		args.add("-Djava.library.path=" + mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve("natives").toString());
		if (!selected.getMainClass().isEmpty()) {
			mainClass = selected.getMainClass();
		} else {
			mainClass = mcVersion.getMainClass();
		}
		for (ModuleWidget entry : modules) {
			if (entry.isSelected()) {
				if (entry.getModule().getModType().equals(ModType.Library)) {
					libs.add(entry.getModule().getId() + ".jar");
				}
				if (!entry.getModule().getLaunchArgs().isEmpty()) {
					clArgs.append(" ").append(entry.getModule().getLaunchArgs());
				}
				if (!entry.getModule().getJreArgs().isEmpty()) {
					args.addAll(Arrays.asList(entry.getModule().getJreArgs().split(" ")));
				}
				if (entry.getModule().hasSubmodules()) {
					for (GenericModule sm : entry.getModule().getSubmodules()) {
						if (sm.getModType().equals(ModType.Library)) {
							libs.add(sm.getId() + ".jar");
						}
						if (!sm.getLaunchArgs().isEmpty()) {
							clArgs.append(" ").append(sm.getLaunchArgs());
						}
						if (!sm.getJreArgs().isEmpty()) {
							args.addAll(Arrays.asList(sm.getJreArgs().split(" ")));
						}
					}
				}
			}
		}
		for (Library lib : mcVersion.getLibraries()) {
			String key = StringUtils.join(Arrays.copyOfRange(lib.getName().split(":"),0,2),":");
			if (selected.getLibOverrides().containsKey(key)) {
				lib.setName(selected.getLibOverrides().get(key));
			}
			if (lib.validForOS() && !lib.hasNatives()) {
				libs.add(lib.getFilename());
			}
		}
		args.add("-cp");
		StringBuilder classpath = new StringBuilder();
		for (String entry : libs) {
			classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve(entry).toString()).append(MCUpdater.cpDelimiter());
		}
		classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("bin").resolve("minecraft.jar").toString());
		args.add(classpath.toString());
		args.add(mainClass);
		String tmpclArgs = clArgs.toString();
		Map<String,String> fields = new HashMap<>();
		StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
		fields.put("auth_player_name", playerName);
		fields.put("auth_uuid", user.getUUID().replace("-",""));
		fields.put("auth_access_token", user.getAccessToken());
		fields.put("auth_session", sessionKey);
		fields.put("version_name", selected.getVersion());
		fields.put("game_directory", mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
		if (index.isVirtual()) {
			fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").resolve("virtual").toString());
			fields.put("assets_root", mcu.getArchiveFolder().resolve("assets").resolve("virtual").toString());
		} else {
			fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").toString());
			fields.put("assets_root", mcu.getArchiveFolder().resolve("assets").toString());
		}
		fields.put("assets_index_name", indexName);
		fields.put("resource_packs", mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("resourcepacks").toString());
		fields.put("user_properties", "{}"); //TODO: This will likely actually get used at some point.
		fields.put("user_type", (user.isLegacy() ? UserType.LEGACY.toString() : UserType.MOJANG.toString()));
		fields.put("version_type", mcVersion.getType());
		String[] fieldArr = tmpclArgs.split(" ");
		for (int i = 0; i < fieldArr.length; i++) {
			fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
		}
		args.addAll(Arrays.asList(fieldArr));
		args.addAll(Main.passthroughArgs);

		log("Launch args:");
		log("=======================");
		for (String entry : args) {
			log(entry);
		}
		log("=======================");
		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.environment().put("openeye.tags","MCUpdater," + selected.getName() + " (" + selected.getServerId() + ")");
		pb.directory(mcu.getInstanceRoot().resolve(selected.getServerId()).toFile());
		pb.redirectErrorStream(true);
		final Thread gameThread = new Thread(new Runnable(){
			@Override
			public void run() {
				ConsoleForm mcOutput = null;
				try{
					if (settings.isMinecraftToConsole()) {
						mcOutput = new ConsoleForm("Minecraft instance: " + selected.getName());
					}
					Process task = pb.start();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String line;
					while ((line = buffRead.readLine()) != null) {
						if (line.length() > 0) {
							if (settings.isMinecraftToConsole()) {
								if (mcOutput != null) {
									Style lineStyle = null;
									if (line.contains("WARNING")) { lineStyle = mcOutput.getConsole().warnStyle; }
									if (line.contains("SEVERE")) { lineStyle = mcOutput.getConsole().errorStyle; }
									mcOutput.getConsole().log(line + "\n", lineStyle);
								}
							}
						}
					}
				} catch (Exception e) {
					baseLogger.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					if (mcOutput != null) {
						mcOutput.allowClose();
					}
					baseLogger.info("Minecraft process terminated");
					setPlaying(false);
				}
			}
		});
		gameThread.start();
	}

	private Path getJava() throws Exception {
		Path javaFile;
		if (System.getProperty("os.name").startsWith("Win")) {
			javaFile = new File(SettingsManager.getInstance().getSettings().getJrePath()).toPath().resolve("bin").resolve("javaw.exe");
		} else {
			javaFile = new File(SettingsManager.getInstance().getSettings().getJrePath()).toPath().resolve("bin").resolve("java");
		}
		if (Files.exists(javaFile)) {
			return javaFile;
		} else {
			throw new Exception("Java executable not found at specified JRE path!");
		}
	}

	private void tryOldLaunch(final ServerList selected, Profile user) throws Exception {
		Path javaPath = getJava();
		Path mcuPath = MCUpdater.getInstance().getArchiveFolder();
		final Settings settings = SettingsManager.getInstance().getSettings();
		Path instancePath = MCUpdater.getInstance().getInstanceRoot().resolve(selected.getServerId());
		String playerName = user.getName();
		String sessionKey = user.getSessionKey(this);
		List<String> args = new ArrayList<>();
		if (!settings.getProgramWrapper().isEmpty()) {
			args.add(settings.getProgramWrapper());
		}
		args.add(javaPath.toString());
		if (System.getProperty("os.name").startsWith("Mac")) {
			args.add("-Xdock:icon=" + mcuPath.resolve("assets").resolve("icons").resolve("minecraft.icns").toString());
			args.add("-Xdock:name=Minecraft(MCUpdater)");
		}
		args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
		args.add("-Xms" + settings.getMinMemory());
		args.add("-Xmx" + settings.getMaxMemory());
		args.add("-XX:PermSize=" + settings.getPermGen());
		args.add("-classpath");
		args.add(mcuPath.resolve("lib").resolve("MCU-Launcher.jar").toString() + System.getProperty("path.separator") + instancePath.resolve("lib").resolve("*"));
		args.add("org.mcupdater.MinecraftFrame");
		args.add(playerName);
		args.add(sessionKey);
		args.add(selected.getName());
		args.add(instancePath.toString());
		args.add(instancePath.resolve("lib").toString());
		args.add(selected.getIconUrl().isEmpty() ? "https://minecraft.net/favicon.png" : selected.getIconUrl());
		args.add(String.valueOf(settings.getResWidth()));
		args.add(String.valueOf(settings.getResHeight()));
		args.add(selected.getAddress().isEmpty() ? "localhost" : selected.getAddress());
		args.add(Boolean.toString(selected.isAutoConnect() && settings.isAutoConnect()));

		log("Launch args:");
		log("=======================");
		for (String entry : args) {
			log(entry);
		}
		log("=======================");
		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(instancePath.toFile());
		pb.redirectErrorStream(true);
		final Thread gameThread = new Thread(new Runnable(){
			@Override
			public void run() {
				ConsoleForm mcOutput = null;
				try{
					if (settings.isMinecraftToConsole()) {
						mcOutput = new ConsoleForm("Minecraft instance: " + selected.getName());
					}
					Process task = pb.start();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String line;
					while ((line = buffRead.readLine()) != null) {
						if (line.length() > 0) {
							if (settings.isMinecraftToConsole()) {
								if (mcOutput != null) {
									Style lineStyle = null;
									if (line.contains("WARNING")) { lineStyle = mcOutput.getConsole().warnStyle; }
									if (line.contains("SEVERE")) { lineStyle = mcOutput.getConsole().errorStyle; }
									mcOutput.getConsole().log(line + "\n", lineStyle);
								}
							}
						}
					}
				} catch (Exception e) {
					baseLogger.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					if (mcOutput != null) {
						mcOutput.allowClose();
					}
					baseLogger.info("Minecraft process terminated");
					setPlaying(false);
				}
			}
		});
		gameThread.start();
	}

	// Section - Logic elements

	private void setSelectedInstance(String instance) {
		for (ServerList entry : slModel.getData()) {
			if (entry.getServerId().equals(instance)) {
				serverList.setSelectedValue(entry, true);
				return;
			}
		}
	}

	public static MainForm getInstance() {
		return instance;
	}

	private void refreshInstanceList() {
		Settings current = SettingsManager.getInstance().getSettings();
		List<ServerList> slList = new ArrayList<>();

		Set<String> urls = new HashSet<>();
		urls.addAll(current.getPackURLs());

		for (String serverUrl : urls) {
			try {
				Element docEle;
				Document serverHeader = ServerPackParser.readXmlFromUrl(serverUrl);
				if (!(serverHeader == null)) {
					Element parent = serverHeader.getDocumentElement();
					if (parent.getNodeName().equals("ServerPack")) {
						String mcuVersion = parent.getAttribute("version");
						NodeList servers = parent.getElementsByTagName("Server");
						for (int i = 0; i < servers.getLength(); i++) {
							docEle = (Element) servers.item(i);
							ServerList sl = ServerList.fromElement(mcuVersion, serverUrl, docEle);
							if (!sl.isFakeServer()) {
								ServerList newEntry = ServerPackParser.parseDocument(serverHeader,sl.getServerId(),new HashMap<String,Module>(), sl.getServerId());
								newEntry.setPackUrl(serverUrl);
								Instance instData = new Instance();
								AtomicReference<Instance> ref = new AtomicReference<>(instData);
								newEntry.setState(getPackState(newEntry, ref));
								slList.add(newEntry);
							}
						}
					} else {
						ServerList sl = ServerList.fromElement("1.0", serverUrl, parent);
						Instance instData = new Instance();
						AtomicReference<Instance> ref = new AtomicReference<>(instData);
						sl.setState(getPackState(sl, ref));
						slList.add(sl);
					}
				} else {
					log("Unable to get server information from " + serverUrl);
				}
			} catch (Exception e) {
				baseLogger.log(Level.SEVERE, "Failed to load from: " + serverUrl, e);
			}
		}
		if (serverList != null) {
			((SLListModel) serverList.getModel()).clearAndSet(slList);
			if (serverList.getSelectedIndex() > -1) {
				changeSelectedServer(serverList.getSelectedValue());
			}
		}
	}

	private boolean changeSelectedServer(ServerList entry) {
		frameMain.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.selected = entry;
		newsBrowser.navigate(entry.getNewsUrl());
		//entry = ServerPackParser.loadFromURL(entry.getPackUrl(), entry.getServerId());
		List<Module> modList = new ArrayList<>(entry.getModules().values());
		Instance instData = new Instance();
		AtomicReference<Instance> ref = new AtomicReference<>(instData);
		entry.setState(getPackState(entry, ref));
		instData = ref.get();
		// System.out.println(instData.toString());
		try {
			Collections.sort(modList, new ModuleComparator(ModuleComparator.Mode.OPTIONAL_FIRST));
		} catch (Exception e) {
			baseLogger.warning("Unable to sort mod list!");
		}
		Set<String> modIds = new HashSet<>();
		for (Module mod : modList) {
			modIds.add(mod.getId());
		}
		for (Module mod : new ArrayList<>(modList)) {
			if (!mod.getDepends().isEmpty()) {
				for (String modid : mod.getDepends().split(" ")) {
					if (!modIds.contains(modid)) {
						MainForm.getInstance().baseLogger.log(Level.WARNING, mod.getName() + ": " + modid + " does not exist in the mod list for dependency and will be removed from the pack.");
						modList.remove(mod);
					}
				}
			}
		}
		modPanel.reload(modList, instData.getOptionalMods());

		frameMain.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		baseLogger.info("Selection changed to: " + entry.getServerId());
		if (entry.getState().equals(ServerList.State.UPDATE)) {
			JOptionPane.showMessageDialog(null, "<html>Your configuration for <b>" + entry.getName() + "</b> is out of sync with the server.<br/>Updating is necessary.</html>", "MCUpdater", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (entry.getState().equals(ServerList.State.ERROR)) {
			JOptionPane.showMessageDialog(null, "The server pack indicates that it is for a newer version of MCUpdater than you are currently using.\nThis version of MCUpdater may not properly handle this server.");
			return false;
		}
		return true;
	}

	private ServerList.State getPackState(ServerList entry, AtomicReference<Instance> ref) {
		Set<String> digests = entry.getDigests();
		String remoteHash = MCUpdater.calculateGroupHash(digests);
		final Path instanceFile = MCUpdater.getInstance().getInstanceRoot().resolve(entry.getServerId()).resolve("instance.json");
		Instance instData = ref.get();
		try {
			BufferedReader reader = Files.newBufferedReader(instanceFile, StandardCharsets.UTF_8);
			instData = gson.fromJson(reader, Instance.class);
			ref.getAndSet(instData);
			reader.close();
		} catch (IOException e) {
			baseLogger.log(Level.WARNING, "instance.json file not found.  This is not an error if the instance has not been installed.");
		}
		boolean needUpdate = (instData.getHash().isEmpty() || !instData.getHash().equals(remoteHash));
		boolean needNewMCU = Version.isVersionOld(entry.getMCUVersion());
		if (needUpdate) { return ServerList.State.UPDATE; }
		if (needNewMCU) { return ServerList.State.ERROR; }
		return ServerList.State.READY;
	}

	private void refreshProfileList() {
		profileModel.clearAndSet(SettingsManager.getInstance().getSettings().getProfiles());
	}

	@Override
	public void setStatus(String string) {
		lblServerStatus.setText(string);
	}

	@Override
	public void log(String msg) {
		baseLogger.info(msg);
	}

	@Override
	public Profile requestLogin(String username) {
		JXLoginPane login = new JXLoginPane();
		Image img = null;
		try {
			img = ImageIO.read(getClass().getResource("banner.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		login.setBanner(img);
		MinecraftLoginService loginService = new MinecraftLoginService(login, UUID.randomUUID().toString());
		login.setLoginService(loginService);
		JXLoginPane.showLoginDialog(frameMain, login);
		Object response = loginService.getResponse();
		Profile newProfile = null;
		if (response instanceof YggdrasilUserAuthentication) {
			YggdrasilUserAuthentication user = (YggdrasilUserAuthentication) response;
			newProfile = new Profile();
			newProfile.setStyle("Yggdrasil");
			newProfile.setUsername(login.getUserName());
			newProfile.setAccessToken(user.getAuthenticatedToken());
			newProfile.setName(user.getSelectedProfile().getName());
			newProfile.setUUID(user.getSelectedProfile().getId().toString());
			newProfile.setUserId(user.getUserID());
			newProfile.setLegacy((UserType.LEGACY == user.getUserType()));
		}
		return newProfile;
	}

	@Override
	public DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath) {
		progressView.addProgressBar(queueName, parent);
		if (profileModel.getSelectedItem() != null) {
			return new DownloadQueue(queueName, parent, this, files, basePath, cachePath, ((Profile)profileModel.getSelectedItem()).getName());
		} else {
			return new DownloadQueue(queueName, parent, this, files, basePath, cachePath);
		}
	}

	@Override
	public DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version) {
		progressView.addProgressBar(queueName, parent);
		return AssetManager.downloadAssets(queueName, parent, MCUpdater.getInstance().getArchiveFolder().resolve("assets").toFile(), this, version);
	}

	@Override
	public void alert(String msg) {
		baseLogger.warning(msg);
		JOptionPane.showMessageDialog(null, msg, "MCUpdater", JOptionPane.WARNING_MESSAGE );
	}

	@Override
	public void stateChanged(boolean newState) {

	}

	@Override
	public void settingsChanged(Settings newSettings) {
		refreshInstanceList();
		refreshProfileList();
		MCUpdater.getInstance().setInstanceRoot(new File(newSettings.getInstanceRoot()).toPath());
		String lastProfile = newSettings.getLastProfile();
		cboProfiles.setSelectedIndex(-1);
		cboProfiles.setSelectedItem(newSettings.findProfile(lastProfile));
	}

	@Override
	public void onQueueFinished(DownloadQueue queue) {
		synchronized (progressView) {
			log(queue.getParent() + " - " + queue.getName() + ": Finished!");
			progressView.updateProgress(queue.getName(), queue.getParent(), 1f, queue.getTotalFileCount(), queue.getSuccessFileCount());
			for (Downloadable entry : queue.getFailures()) {
				baseLogger.severe("Failed: " + entry.getFilename());
			}
		}
	}

	@Override
	public void onQueueProgress(DownloadQueue queue) {
		updateCounter++;
		if (updateCounter == 10) {
			synchronized (progressView) {
				progressView.updateProgress(queue.getName(), queue.getParent(), queue.getProgress(), queue.getTotalFileCount(), queue.getSuccessFileCount());
			}
			updateCounter = 0;
		}
	}

	@Override
	public void printMessage(String msg) {
		log(msg);
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public boolean isPlaying() {
		return playing;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing
	}

	private final class InstanceListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (serverList.getSelectedIndex() > -1) {
				if (!e.getValueIsAdjusting()) {
					changeSelectedServer(serverList.getSelectedValue());
					setStatus("Getting server status...");
					Thread async = new Thread("Server status update") {
						public void run() {
							try {
								if (!serverList.getSelectedValue().getAddress().isEmpty()) {
									ServerStatus serverStatus = ServerStatus.getStatus(serverList.getSelectedValue().getAddress());
									setStatus(serverStatus.getMOTD() + " - " + serverStatus.getPlayers() + "/" + serverStatus.getMaxPlayers());
								} else {
									setStatus("Server status N/A");
								}
							} catch (Exception e1) {
								baseLogger.log(Level.SEVERE, "Error getting server status", e1);
								setStatus("Server info not available");
							}
						}
					};
					async.setDaemon(true);
					async.run();
				}
			}
		}
	}
}
