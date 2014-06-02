package org.mcupdater.auth;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.auth.LoginService;
import org.mcupdater.Yggdrasil.AuthManager;
import org.mcupdater.Yggdrasil.SessionResponse;
import org.mcupdater.gui.MainForm;

public class MinecraftLoginService extends LoginService {

	private final JXLoginPane parent;
	private final String clientToken;
	private AuthManager auth = new AuthManager();
	private SessionResponse response;

	public MinecraftLoginService(JXLoginPane parent, String clientToken) {
		this.setSynchronous(true);
		this.parent = parent;
		this.clientToken = clientToken;
	}

	@Override
	public boolean authenticate(String name, char[] password, String server) throws Exception {
		response = auth.authenticate(name, new String(password), clientToken);
		if (response.getError().isEmpty()) {
			return true;
		} else {
			MainForm.getInstance().baseLogger.warning(response.getErrorMessage());
			parent.setErrorMessage(response.getErrorMessage());
			return false;
		}
	}

	public SessionResponse getResponse() {
		return response;
	}
}
