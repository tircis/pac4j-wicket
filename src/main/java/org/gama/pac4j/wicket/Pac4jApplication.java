package org.gama.pac4j.wicket;

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.pac4j.core.profile.CommonProfile;

/**
 * @author Guillaume Mary
 */
public abstract class Pac4jApplication extends AuthenticatedWebApplication {
	
	public static Pac4jApplication get() {
		return (Pac4jApplication) WebApplication.get();
	}
	
	@Override
	protected void init() {
		super.init();
		
		getStrategy().init();
		
		getRequestCycleListeners().add(new Pac4jRequestCycleListener(getStrategy().getConfig()));
	}
	
	public abstract Pac4jWicketAuthenticationStrategy getStrategy();
	
	@Override
	public void restartResponseAtSignInPage() {
		getStrategy().restartResponseAtSignPage();
	}
	
	/** Overriden to make it public, then accessible from WicketClient */
	@Override
	public abstract Class<? extends WebPage> getSignInPageClass();
	
	public static class Pac4jSession extends AbstractAuthenticatedWebSession {
		
		/**
		 * Default, lonely, constructor
		 *
		 * @param request the current request object
		 */
		public Pac4jSession(Request request) {
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
