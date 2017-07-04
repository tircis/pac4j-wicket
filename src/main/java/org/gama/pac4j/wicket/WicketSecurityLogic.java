package org.gama.pac4j.wicket;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;

/**
 * A {@link DefaultSecurityLogic} specialized for Wicket: no {@link org.pac4j.core.authorization.checker.AuthorizationChecker},
 * nor {@link org.pac4j.core.matching.MatchingChecker} since those checking will be done through 
 * {@link org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation} annotations on pages.
 * 
 * @author Guillaume Mary
 */
public class WicketSecurityLogic extends DefaultSecurityLogic<Void, J2EContext> {
	
	public WicketSecurityLogic() {
		// Authorization is made by Wicket, no need of the default AuthorizationChecker
		setAuthorizationChecker((context1, profiles, authorizerNames, authorizersMap) -> true);
		// URL matching is made by Wicket, no need of the default MatchingChecker
		setMatchingChecker((context1, matcherNames, matchersMap) -> true);
	}
	
	/**
	 * Shortcut for {@code perform(client.getName())}
	 * @param client the client to be used for authentication
	 */
	public void perform(Client client) {
		perform(client.getName());
	}
	
	/**
	 * A simple way to perform authentication for Wicket.
	 * Calls {@link #perform(WebContext, Config, SecurityGrantedAccessAdapter, HttpActionAdapter, String, String, String, Boolean, Object...)}
	 * with default values.
	 * 
	 * @param clientsNames a comma-separated list of client names to be used for authentication
	 */
	public void perform(String clientsNames) {
		perform(
				SecurityContext.getCurrentWebContext(),
				((Pac4jApplication) Application.get()).getConfig(),
				(ctx, parameters) -> null,	// TODO: adapt Wicket Session role, for DirectClient use case
				(code, ctx) -> null,	// created to adapt perform(..) method return, nothing to do since we don't care about the return
				clientsNames,	// of course our clients
				null,		// anything, since AuthorizationChecker is fixed to always return true (accept all because Wicket manages Authorization)
				null,		// anything, since MatchingChecker is fixed to always return true (always executes the core code of 
									// DefaultSecurityLogic#perform(..) method)
				true	// we do our best
		);
	}
	
	/**
	 * Overriden to throw {@link RestartResponseAtInterceptPageException} so Wicket will call
	 * {@link AuthenticatedWebApplication#restartResponseAtSignInPage()}
	 * 
	 * @param context the {@link J2EContext} of the request
	 * @param currentClients the list of implicated {@link Client}. Only the first one is used, expected to be an {@link IndirectClient}
	 * (else it's a misusage)
	 * @return nothing since its always throws {@link RestartResponseAtInterceptPageException}
	 * @throws RestartResponseAtInterceptPageException with the URL of the {@link IndirectClient} where to be redirected
	 */
	@Override
	protected HttpAction redirectToIdentityProvider(J2EContext context, List<Client> currentClients) throws HttpAction {
		IndirectClient currentClient = (IndirectClient) currentClients.get(0);
		throw new RestartResponseAtInterceptPageException(new RedirectPage(currentClient.getRedirectAction(context).getLocation()));
	}
}
