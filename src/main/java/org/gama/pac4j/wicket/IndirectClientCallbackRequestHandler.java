package org.gama.pac4j.wicket;

import org.apache.wicket.Application;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;

/**
 * A callback handler for {@link org.pac4j.core.client.IndirectClient} after successfull authentication.
 * Implements {@link IRequestHandler} to match Wicket API, hence must be declared at {@link Application#getRootRequestMapperAsCompound()} level.
 * 
 * @author Guillaume Mary
 */
public class IndirectClientCallbackRequestHandler implements IRequestHandler {
	
	private final Config config;
	private final CallbackLogic<Void, J2EContext> defaultCallbackLogic;
	
	public IndirectClientCallbackRequestHandler(Config config) {
		this.config = config;
		this.defaultCallbackLogic = new DefaultCallbackLogic<>();
	}
	
	@Override
	public void respond(IRequestCycle requestCycle) {
		fixUserProfile(SecurityContext.getCurrentWebContext());
	}
	
	protected void fixUserProfile(J2EContext context) {
		// we call Pac4J default callback logic to fullfill current profiles, redirect, and so on
		defaultCallbackLogic.perform(context, config,
				(code, ctx) -> null,    // created to adapt perform(..) method return, nothing to do since we don't care about the return
				getDefaultRedirectUrl(),
				true,	// we do our best
				false	// we don't renew the session because I'm not sure of the consequences on request cycle (request loop ?)
		);
	}
	
	/**
	 * @return the very default URL to where the successfull authentication must be redirected. Default is Application's home page.
	 */
	protected String getDefaultRedirectUrl() {
		return RequestCycle.get().urlFor(new RenderPageRequestHandler(Application.get().getHomePage())).toString();
	}
}
