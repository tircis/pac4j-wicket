package org.gama.pac4j.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.AjaxRequestResolver;

/**
 * @author Guillaume Mary
 */
public class IndirectAuthenticationStrategy extends Pac4jWicketAuthenticationStrategy {
	
	private Url indirectClientCallbackUrl;
	
	public IndirectAuthenticationStrategy(Config config) {
		super(config);
	}
	
	@Override
	public void init() {
		IndirectClientCallbackRequestHandler indirectClientCallbackHandler = new IndirectClientCallbackRequestHandler(getConfig());
		MountedRequestHandlerMapper authenticationCallback = new MountedRequestHandlerMapper(getConfig().getClients().getCallbackUrl(), indirectClientCallbackHandler);
		WebApplication.get().mount(authenticationCallback);
		indirectClientCallbackUrl = authenticationCallback.mapHandler(new IndirectClientCallbackRequestHandler(getConfig()));
		
		WebApplication.get().mount(new MountedRequestHandlerMapper("logout", new LogoutRequestHandler(getConfig())));
	}
	
	@Override
	public void restartResponseAtSignPage() {
		Client client = getConfig().getClients().getClients().get(0);
		IndirectClient currentClient = (IndirectClient) client;
		String callbackUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(indirectClientCallbackUrl);
		currentClient.setCallbackUrl(callbackUrl);
		WicketSecurityLogic securityLogic = new WicketSecurityLogic();
		securityLogic.perform(client);
		
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
}
