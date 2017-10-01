package org.gama.pac4j.wicket.signin;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.gama.pac4j.wicket.Pac4jApplication;
import org.gama.pac4j.wicket.SecurityContext;
import org.gama.pac4j.wicket.WicketClient;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 * An {@link AuthenticatedWebSession} that delegates authentication to {@link WicketClient}.
 * Expects that a {@link WicketClient} is defined in the current {@link Pac4jApplication} configuration.
 * 
 * Must be used only in coordination with Wicket {@link org.apache.wicket.authroles.authentication.panel.SignInPanel} because its hard codes a calls
 * to {@link AuthenticatedWebSession#authenticate(String, String)}.
 * If any other sign in panel is used, {@link org.gama.pac4j.wicket.Pac4jApplication.Pac4jSession} should be prefered.
 * 
 * @author Guillaume Mary
 */
public class Pac4jAuthenticatedWebSession extends AuthenticatedWebSession {
		
		/**
		 * Construct.
		 *
		 * @param request The current request object
		 */
		public Pac4jAuthenticatedWebSession(Request request) {
			super(request);
		}
		
		@Override
		protected boolean authenticate(String username, String password) {
			ThreadLocalCredentialsExtractor.set(new UsernamePasswordCredentials(username, password, null));
			try {
				WicketClient<CommonProfile> wicketClient = Pac4jApplication.get().getStrategy().getConfig().getClients().findClient(WicketClient.class);
				
				// yes, getCredentials() calls authentication of WicketClient validate's method
				UsernamePasswordCredentials credentials = wicketClient.getCredentials(SecurityContext.get().getWebContext());
				// no credentials means not authenticated (because BaseClient#retrieveCredentials() does on CredentialException)
				if (credentials == null) {
					return false;
				} else {
					// this forces call to WicketClient#create(), hence leaving developper to load user from database there, for instance
					CommonProfile userProfile = wicketClient.getUserProfile(credentials, SecurityContext.get().getWebContext());
					// eventually keep the profile onto Session, hence it will be available on next request through SecurityContext
					saveProfile(userProfile);
					return true;
				}
			} catch (HttpAction httpAction) {
				// should never happens :
				// - never thrown by any implementation of validate() !
				// - not expected by getUserProfile(..) because in our design WicketClient#create(..) is endly used, which is expected to be simple
				throw new RuntimeException(httpAction);
			} finally {
				ThreadLocalCredentialsExtractor.remove();
			}
			/*
			try {
//				((IndirectClient) ((Pac4jApplication) JokerApplication.get()).getStrategy().getConfig().getClients().getClients().get(0)).getAuthenticator()
//						.validate();
				
				Pac4jApplication authenticatedWebApplication = (Pac4jApplication) AuthenticatedWebApplication.get();
				Class<? extends Page> signInPageClass = authenticatedWebApplication.getHomePage();
				// we redirect with a Url relative to the current one
				IRequestHandler handler = new BookmarkablePageRequestHandler(new PageProvider(signInPageClass));
				Url url = RequestCycle.get().mapUrlFor(handler);
				CharSequence redirectUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(url);
				
				DefaultCallbackLogic callbackLogic = new WicketClientCallbackLogic();
//				DefaultCallbackLogic callbackLogic = new DefaultCallbackLogic();
//				String clientNameParameter = authenticatedWebApplication.getStrategy().getConfig().getClients().getClientNameParameter();
//				SecurityContext.get().getWebContext().getRequestParameters().put(clientNameParameter, new String[] { WicketClient.class.getSimpleName() });
				callbackLogic.perform(SecurityContext.get().getWebContext(), ((Pac4jApplication) JokerApplication.get()).getStrategy().getConfig(), new HttpActionAdapter() {
					
					@Override
					public Object adapt(int code, WebContext context) {
						return null;
					}
				}, redirectUrl.toString(), true, false);
				
//				WicketSecurityLogic securityLogic = new WicketSecurityLogic();
//				securityLogic.perform(((Pac4jApplication) JokerApplication.get()).getStrategy().getConfig().getClients().getClients().get(0));
				return true;
//				return username.equals(password);
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			*/
		}
	
	protected void saveProfile(CommonProfile userProfile) {
		SecurityContext.get().getProfileManager().save(true, userProfile, false);
	}
	
	@Override
	public Roles getRoles() {
		CommonProfile currentProfile = SecurityContext.getCurrentProfile();
		if (currentProfile == null) {
			return new Roles();
		} else {
			return new Roles(currentProfile.getRoles().toArray(new String[0]));
		}
	}
}