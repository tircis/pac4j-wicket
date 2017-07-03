package org.gama.pac4j.wicket;

import java.util.List;

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.AjaxRequestResolver;
import org.pac4j.core.profile.CommonProfile;

/**
 * @author Guillaume Mary
 */
public abstract class Pac4JApplication extends AuthenticatedWebApplication {
	
	private Url indirectClientCallbackUrl;
	
	private Config config;
	
	@Override
	protected void init() {
		super.init();
		
		config = new Config("authenticationCallback", getClients());
		
		IndirectClientCallbackRequestHandler indirectClientCallbackHandler = new IndirectClientCallbackRequestHandler(getConfig());
		MountedRequestHandlerMapper authenticationCallback = new MountedRequestHandlerMapper("authenticationCallback", indirectClientCallbackHandler);
		mount(authenticationCallback);
		
		indirectClientCallbackUrl = authenticationCallback.mapHandler(new IndirectClientCallbackRequestHandler(getConfig()));
		
		mount(new MountedRequestHandlerMapper("logout", new LogoutRequestHandler(getConfig())));
		getRequestCycleListeners().add(new Pac4jRequestCycleListener(getConfig()));
	}
	
	public Config getConfig() {
		return config;
	}
	
	public abstract List<Client> getClients();
	
	public void restartResponseAtSignInPage() {
		
		Client client = getConfig().getClients().getClients().get(0);
		IndirectClient currentClient = (IndirectClient) client;
		String callbackUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(indirectClientCallbackUrl);
		currentClient.setCallbackUrl(callbackUrl);
		WicketSecurityLogic securityLogic = new WicketSecurityLogic(callbackUrl);
		securityLogic.perform(client);
		
		currentClient.setAjaxRequestResolver(new AjaxRequestResolver() {
			/**
			 * Overriden only to escape from the {@link IndirectClient#getRedirectAction(WebContext)} Ajax test
			 * and so goes to the authentication or redirection case. Ugly.
			 * TODO: revert the {@link AjaxRequestResolver} of the client ?
			 * TODO: ensure that it works !
			 */
			@Override
			public boolean isAjax(WebContext context) {
				return false;
			}
		});
	}
	
	/**
	 * @return the login page class only in case of direct client authentication, else (indirect client) null
	 */
	@Override
	protected abstract Class<? extends WebPage> getSignInPageClass();
	
	public static class Pac4JSession extends AbstractAuthenticatedWebSession {
		
		/**
		 * Construct.
		 *
		 * @param request The current request object
		 */
		public Pac4JSession(Request request) {
			super(request);
		}
		
		@Override
		public Roles getRoles() {
			CommonProfile currentProfile = SecurityContext.getCurrentProfile();
			if (currentProfile == null) {
				return new Roles();
			} else {
				return new Roles("admin");
//				return new Roles(currentProfile.getRoles().toArray(new String[0]));
			}
		}
		
		@Override
		public boolean isSignedIn() {
			return SecurityContext.getCurrentProfile() != null;
		}
	}
}
