package org.mcupdater.auth;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import org.mcupdater.AuthManager;
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
		//System.out.println("old-> " + profile.getAccessToken() + ": " + profile.getClientToken());
		UserAuthentication auth = new YggdrasilUserAuthentication(authService, Agent.MINECRAFT);
		Map<String, Object> credentials = new HashMap<>();
		credentials.put("accessToken", profile.getAccessToken());
		credentials.put("username", profile.getUsername());
		credentials.put("userid", profile.getUserId());
		credentials.put("uuid", profile.getUUID());
		credentials.put("displayName", profile.getName());
		auth.loadFromStorage(credentials);
		auth.logIn();
		if (auth.isLoggedIn()) {
			profile.setAccessToken(auth.getAuthenticatedToken());
			profile.setUserId(auth.getUserID());
			profile.setLegacy(UserType.LEGACY == auth.getUserType());

			SettingsManager.getInstance().getSettings().addOrReplaceProfile(profile);
			if (!SettingsManager.getInstance().isDirty()) {
				System.out.println("Saving settings");
				SettingsManager.getInstance().saveSettings();
			}
		}
		return "token:" + auth.getAuthenticatedToken() + ":" + auth.getSelectedProfile().getId().toString();
	}

	@Override
	public Object authenticate(String name, String pass, String clientToken) {
		UserAuthentication auth = new YggdrasilUserAuthentication(authService, Agent.MINECRAFT);
		auth.setUsername(name);
		auth.setPassword(pass);
		try {
			auth.logIn();
			return auth;
		} catch (AuthenticationException e) {
			return e;
		}
	}
}
