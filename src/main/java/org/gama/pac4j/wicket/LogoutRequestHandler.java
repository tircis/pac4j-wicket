package org.gama.pac4j.wicket;

import org.apache.wicket.Application;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.gama.pac4j.wicket.SecurityContext;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultLogoutLogic;

/**
 * A handler for logout user action
 * 
 * @author Guillaume Mary
 */
public class LogoutRequestHandler implements IRequestHandler {
	
	private final Config config;
	private final DefaultLogoutLogic<Void, J2EContext> logoutLogic;
	
	public LogoutRequestHandler(Config config) {
		this.config = config;
		logoutLogic = new DefaultLogoutLogic<>();
	}
	
	@Override
	public void respond(IRequestCycle requestCycle) {
		logout(SecurityContext.getCurrentWebContext());
	}
	
	/**
	 * Clean authenticated session
	 * @param context the Pac4J current context, not null
	 */
	protected void logout(J2EContext context) {
		// we call Pac4J default callback logic to clear current profiles, redirect, and so on
		logoutLogic.perform(
				context,
				config,
				(code, ctx) -> null,    // created to adapt perform(..) method return, nothing to do since we don't care about the return
				getRedirectUrl(),
				null,    // no pattern is necessary since it shouldn't get called
				true,    // of course, what else ?
				true,    // to be sure that nothing stays in the Wicket application
				true    // we want to be deconnected from the application only, not the central SSO system
		);
	}
	
	protected String getRedirectUrl() {
		// Where to go ? warn : home page may be redirected to login page if it is role-protected hence we may loop ...
		return RequestCycle.get().urlFor(new RenderPageRequestHandler(Application.get().getHomePage())).toString();
	}
}
