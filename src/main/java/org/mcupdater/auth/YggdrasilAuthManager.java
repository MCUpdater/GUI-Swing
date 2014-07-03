package org.mcupdater.auth;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import org.mcupdater.AuthManager;
import org.mcupdater.gui.MainForm;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.SettingsManager;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class YggdrasilAuthManager extends AuthManager {

	private final YggdrasilAuthenticationService authService;

	public YggdrasilAuthManager() {
		this.authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, SettingsManager.getInstance().getSettings().getClientToken().toString());
	}

	@Override
	public String getSessionKey(Profile profile) throws Exception {
		System.out.println("old-> " + profile.getAccessToken() + ": " + profile.getClientToken());
		UserAuthentication auth = new YggdrasilUserAuthentication(authService, Agent.MINECRAFT);
		Map<String, Object> credentials = new HashMap<>();
		credentials.put("accessToken", profile.getAccessToken());
		credentials.put("username", profile.getUsername());
		auth.loadFromStorage();
		SessionResponse response = auth.refresh(profile.getAccessToken(), profile.getClientToken());
		if (!response.getError().isEmpty()) {
			try {
				Profile newProfile = MainForm.getInstance().requestLogin(profile.getUsername());
				if (newProfile.getStyle().equals("Yggdrasil")) {
					SettingsManager.getInstance().getSettings().addOrReplaceProfile(newProfile);
					if (!SettingsManager.getInstance().isDirty()) {
						SettingsManager.getInstance().saveSettings();
					}
					return newProfile.getSessionKey(MainForm.getInstance());
				}
			} catch (Exception e) {
			}
		} else {
			profile.setAccessToken(response.getAccessToken());
			profile.setClientToken(response.getClientToken());
			//System.out.println("new-> " + accessToken + ": " + clientToken);

			SettingsManager.getInstance().getSettings().addOrReplaceProfile(profile);
			if (!SettingsManager.getInstance().isDirty()) {
				System.out.println("Saving settings");
				SettingsManager.getInstance().saveSettings();
			}
		}
		return response.getSessionId();
	}

	@Override
	public Object authenticate(String name, String s, String clientToken) {
		return null;
	}

	private Object refresh(String accessToken, String clientToken) {
		YggdrasilUserAuthentication auth = new YggdrasilUserAuthentication()
	}
}
