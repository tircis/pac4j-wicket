package org.gama.pac4j.wicket;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.pac4j.core.config.Config;

/**
 * @author Guillaume Mary
 */
public class DirectAuthenticationStrategy extends Pac4jWicketAuthenticationStrategy {
	
	private final Class<? extends WebPage> signInPageClass;
	
	public DirectAuthenticationStrategy(Config config, Class<? extends WebPage> signInPageClass) {
		super(config);
		this.signInPageClass = signInPageClass;
	}
	
	@Override
	public void init() {
		WebApplication.get().mount(new MountedRequestHandlerMapper("logout", new LogoutRequestHandler(getConfig())));
	}
	
	@Override
	public void restartResponseAtSignPage() {
		throw new RestartResponseAtInterceptPageException(getSignInPageClass());
	}
	
	public Class<? extends WebPage> getSignInPageClass() {
		return signInPageClass;
	}
}
