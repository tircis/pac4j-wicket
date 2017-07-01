package org.gama.pac4j.wicket;

import java.util.List;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.AjaxRequestResolver;
import org.pac4j.core.profile.CommonProfile;

/**
 * @author Guillaume Mary
 */
public abstract class Pac4JApplication extends AuthenticatedWebApplication {
	
	private Url indirectClientCallbackUrl;
	
	@Override
	protected void init() {
		super.init();
		
		IndirectClientCallbackRequestHandler indirectClientCallbackHandler = new IndirectClientCallbackRequestHandler(getConfig());
		MountedRequestHandlerMapper authenticationCallback = new MountedRequestHandlerMapper("authenticationCallback", indirectClientCallbackHandler);
		mount(authenticationCallback);
		
		indirectClientCallbackUrl = authenticationCallback.mapHandler(new IndirectClientCallbackRequestHandler(getConfig()));
		
		mount(new MountedRequestHandlerMapper("logout", new LogoutRequestHandler(getConfig())));
	}
	
	public abstract Config getConfig();
	
	public void restartResponseAtSignInPage() {
		
		Client client = getConfig().getClients().getClients().get(0);
		IndirectClient currentClient = (IndirectClient) client;
		String callbackUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(indirectClientCallbackUrl);
		currentClient.setCallbackUrl(callbackUrl);
		J2EContext context = SecurityContext.getCurrentWebContext();
		DefaultSecurityLogic<Void, J2EContext> securityLogic = new DefaultSecurityLogic<Void, J2EContext>() {
			protected HttpAction redirectToIdentityProvider(J2EContext context, List<Client> currentClients) throws HttpAction {
				IndirectClient currentClient = (IndirectClient) currentClients.get(0);
				throw new RestartResponseAtInterceptPageException(new RedirectPage(currentClient.getRedirectAction(context).getLocation()));
			}
		};
		
		securityLogic.setAuthorizationChecker((context1, profiles, authorizerNames, authorizersMap) -> true);
		securityLogic.setMatchingChecker((context1, matcherNames, matchersMap) -> true);
		securityLogic.perform(
				context,
				getConfig(),
				(ctx, parameters) -> null,    // TODO: adapt Wicket Session role, for DirectClient use case
				(code, ctx) -> null,    // created to adapt perform(..) method return, nothing to do since we don't care about the return
				currentClient.getName(),    // of course our client
				null,    // anything, since AuthorizationChecker is fixed to always return true (accept all because Wicket manages Authorization)
				null,    // anything, since MatchingChecker is fixed to always return true (always executes the core code of 
				// DefaultSecurityLogic#perform(..) method
				true    // we do our best
		);
		
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
