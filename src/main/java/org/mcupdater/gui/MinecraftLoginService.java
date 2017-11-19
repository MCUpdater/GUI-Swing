package org.mcupdater.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.auth.LoginService;
import org.mcupdater.auth.AuthManager;

public class MinecraftLoginService extends LoginService {

	private final JXLoginPane parent;
	private final String clientToken;
	private AuthManager auth = MainForm.getInstance().getAuthManager();
	private Object response;

	public MinecraftLoginService(JXLoginPane parent, String clientToken) {
		this.setSynchronous(true);
		this.parent = parent;
		this.clientToken = clientToken;
	}

	@Override
	public boolean authenticate(String name, char[] password, String server) throws Exception {
		response = auth.authenticate(name, new String(password), clientToken);
		if (response instanceof YggdrasilUserAuthentication) {
			return true;
		} else if (response instanceof AuthenticationException) {
			Exception error = (AuthenticationException) response;
			MainForm.getInstance().baseLogger.warning(error.getMessage());
			parent.setErrorMessage(error.getMessage());
			return false;
		}
		MainForm.getInstance().baseLogger.severe("Authentication returned this object: " + response.toString());
		parent.setErrorMessage("An unexpected result has occurred!");
		return false;
	}

	public Object getResponse() {
		return response;
	}
}
