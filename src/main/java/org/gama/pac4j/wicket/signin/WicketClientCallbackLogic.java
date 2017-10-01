package org.gama.pac4j.wicket.signin;

import javax.servlet.http.HttpServletRequest;

import org.gama.pac4j.wicket.WicketClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.pac4j.core.util.CommonHelper.assertNotBlank;
import static org.pac4j.core.util.CommonHelper.assertNotNull;
import static org.pac4j.core.util.CommonHelper.assertTrue;

/**
 * A simplified version of {@link DefaultCallbackLogic}, created to mimic Pac4j callback principle.
 * {@link org.pac4j.core.engine.DefaultCallbackLogic} can't be used because, for it, client name must be defined as a parameter of the current HTTP
 * request ("TechnicalException: name cannot be blank" in {@link org.pac4j.core.client.Clients#findClient(WebContext)}) and those can't be modified
 * ({@link HttpServletRequest#getParameterMap()}.put() throws "IllegalStateException : No modifications are allowed to a locked ParameterMap").
 * So it requires Wicket sign in form ({@link org.apache.wicket.authroles.authentication.panel.SignInPanel.SignInForm} for instance) to put it in
 * its URL, quite invasive.
 * Moreover {@link org.pac4j.core.engine.DefaultCallbackLogic} has too much code that is not necessary for our use case.
 * 
 * ABANDONNED because it finally calls {@link #redirectToOriginallyRequestedUrl(WebContext, String)} which 
 * 
 * @author Guillaume Mary
 */
public class WicketClientCallbackLogic<R, C extends WebContext> extends DefaultCallbackLogic<R, C> {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public WicketClientCallbackLogic() {
	}
	
	@Override
	public R perform(C context, Config config, HttpActionAdapter<R, C> httpActionAdapter, String inputDefaultUrl, Boolean inputMultiProfile, Boolean
			inputRenewSession) {
		final String defaultUrl;
		if (inputDefaultUrl == null) {
			defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
		} else {
			defaultUrl = inputDefaultUrl;
		}
		final boolean multiProfile;
		if (inputMultiProfile == null) {
			multiProfile = false;
		} else {
			multiProfile = inputMultiProfile;
		}
		final boolean renewSession;
		if (inputRenewSession == null) {
			renewSession = true;
		} else {
			renewSession = inputRenewSession;
		}
		
		// checks
		assertNotNull("context", context);
		assertNotNull("config", config);
		assertNotNull("httpActionAdapter", httpActionAdapter);
		assertNotBlank(Pac4jConstants.DEFAULT_URL, defaultUrl);
		final Clients clients = config.getClients();
		assertNotNull("clients", clients);

		// logic
		final Client client = findClient(context, clients);	// override is here
		logger.debug("client: {}", client);
		assertNotNull("client", client);
		assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");
		
		HttpAction action;
		try {
			final Credentials credentials = client.getCredentials(context);
			logger.debug("credentials: {}", credentials);
			
			final CommonProfile profile = client.getUserProfile(credentials, context);
			logger.debug("profile: {}", profile);
			saveUserProfile(context, config, profile, multiProfile, renewSession);
			action = redirectToOriginallyRequestedUrl(context, defaultUrl);
			
		} catch (final HttpAction e) {
			logger.debug("extra HTTP action required in callback: {}", e.getCode());
			action = e;
		}
		
		return httpActionAdapter.adapt(action.getCode(), context);
	}
	
	public Client findClient(C context, Clients clients) {
		WicketClient client = clients.findClient(WicketClient.class);
		assertTrue(client != null,
				WicketClientCallbackLogic.class.getSimpleName() + " should be used with a " + WicketClient.class.getSimpleName());
		return client;
	}
}
