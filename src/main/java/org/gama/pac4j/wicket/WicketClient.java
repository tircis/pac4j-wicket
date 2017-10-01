package org.gama.pac4j.wicket;

import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.gama.pac4j.wicket.signin.ThreadLocalCredentialsExtractor;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.redirect.RedirectAction;

/**
 * An implementation of {@link IndirectClient} for Wicket {@link AuthenticatedWebApplication}, or almast (!) because
 * {@link AuthenticatedWebApplication#getSignInPageClass()}() is not accessible from outside, then {@link Pac4jApplication} must be used,
 * or {@link #getSignInPageClass()} must be overriden.
 * 
 * Implements {@link ProfileCreator} and {@link Authenticator} so authentication and profile construction can be done through simple override
 * of this class.
 * 
 * WARNING: it expects credentials to be put into {@link ThreadLocalCredentialsExtractor}. Which {@link org.gama.pac4j.wicket.signin.Pac4jAuthenticatedWebSession}
 * does. If you don't use it, then you'll have to mimic it. Don't forget to release the ThreadLocal !
 * 
 * @author Guillaume Mary
 */
public abstract class WicketClient<U extends CommonProfile> extends IndirectClient<UsernamePasswordCredentials, U>
	implements ProfileCreator<UsernamePasswordCredentials, U>, Authenticator<UsernamePasswordCredentials> {
	
	@Override
	protected void clientInit(WebContext context) {
		defaultRedirectActionBuilder(ctx -> {
			Class<? extends WebPage> signInPageClass = getSignInPageClass();
			// we redirect with a Url relative to the current one
			IRequestHandler handler = new BookmarkablePageRequestHandler(new PageProvider(signInPageClass));
			Url url = RequestCycle.get().mapUrlFor(handler);
			CharSequence redirectUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(url);
			return RedirectAction.redirect(redirectUrl.toString());
		});
		
		defaultCredentialsExtractor(new ThreadLocalCredentialsExtractor());
		defaultAuthenticator(this);
		defaultProfileCreator(this);
	}
	
	/**
	 * Implementation that calls {@link Pac4jApplication#getSignInPageClass()}
	 * @return {@link Pac4jApplication#getSignInPageClass()}
	 */
	protected Class<? extends WebPage> getSignInPageClass() {
		Pac4jApplication authenticatedWebApplication = Pac4jApplication.get();
		return authenticatedWebApplication.getSignInPageClass();
	}
	
}
