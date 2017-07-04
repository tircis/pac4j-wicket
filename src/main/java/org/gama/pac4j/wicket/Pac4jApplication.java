package org.gama.pac4j.wicket;

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.pac4j.core.profile.CommonProfile;

/**
 * @author Guillaume Mary
 */
public abstract class Pac4jApplication extends AuthenticatedWebApplication {
	
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
	
	/**
	 * @return the login page class only in case of direct client authentication, else will throw a {@link ClassCastException}
	 */
	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return ((DirectAuthenticationStrategy) getStrategy()).getSignInPageClass();
	}
	
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
