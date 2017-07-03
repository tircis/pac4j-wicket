package org.gama.pac4j.wicket;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;

/**
 * @author Guillaume Mary
 */
public class WicketSecurityLogic extends DefaultSecurityLogic<Void, J2EContext> {
	
	private final String callbackUrl;
	
	public WicketSecurityLogic(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		// Authorization is made by Wicket, no need of the default AuthorizationChecker
		setAuthorizationChecker((context1, profiles, authorizerNames, authorizersMap) -> true);
		// URL matching is made by Wicket, no need of the default MatchingChecker
		setMatchingChecker((context1, matcherNames, matchersMap) -> true);
	}
	
	public void perform(Client client) {
		String clientName = client.getName();
		perform(clientName);
	}
	
	public void perform(String clientName) {
		perform(
				SecurityContext.getCurrentWebContext(),
				((Pac4JApplication) Application.get()).getConfig(),
				(ctx, parameters) -> null,	// TODO: adapt Wicket Session role, for DirectClient use case
				(code, ctx) -> null,	// created to adapt perform(..) method return, nothing to do since we don't care about the return
				clientName,	// of course our client
				null,		// anything, since AuthorizationChecker is fixed to always return true (accept all because Wicket manages Authorization)
				null,		// anything, since MatchingChecker is fixed to always return true (always executes the core code of 
									// DefaultSecurityLogic#perform(..) method)
				true	// we do our best
		);
	}
	
	protected HttpAction redirectToIdentityProvider(J2EContext context, List<Client> currentClients) throws HttpAction {
		IndirectClient currentClient = (IndirectClient) currentClients.get(0);
		throw new RestartResponseAtInterceptPageException(new RedirectPage(currentClient.getRedirectAction(context).getLocation()));
	}
}
