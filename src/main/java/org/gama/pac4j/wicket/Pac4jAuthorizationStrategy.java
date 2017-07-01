package org.gama.pac4j.wicket;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiations;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 * @author Guillaume Mary
 */
public class Pac4jAuthorizationStrategy implements IAuthorizationStrategy {
	
	private final Config config;
	
	public Pac4jAuthorizationStrategy(Config config) {
		this.config = config;
	}

//	private SecurityLogic<Object, J2EContext> securityLogic = new DefaultSecurityLogic<>();
	
//	//	@Override
//	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, 
//			ServletException {
//		
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
//		HttpServletResponse response = (HttpServletResponse) servletResponse;
//		SessionStore<J2EContext> sessionStore = config.getSessionStore();
//		J2EContext context = new J2EContext(request, response, sessionStore != null ? sessionStore : ShiroSessionStore.INSTANCE);
//		
//		securityLogic.perform(context, config, (ctx, parameters) -> true, (code, webCtx) -> false, "", "", 
//				"", false);
////		}, (code, ctx) -> null, clients, authorizers, matchers, multiProfile);
//	}
	
	@Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
		
		WebContext webContext = SecurityContext.getCurrentWebContext();
		
		List<CommonProfile> profiles = SecurityContext.get().getProfiles();
		
		CommonProfile currentProfile = SecurityContext.getCurrentProfile();
		
		if (currentProfile == null && Page.class.isAssignableFrom(componentClass)) {
			//saveRequestedUrl(context, currentClients);
			final String requestedUrl = webContext.getFullRequestURL();
//			webContext.debug("requestedUrl: {}", requestedUrl);
			webContext.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
			
			//action = redirectToIdentityProvider(context, currentClients);
			IndirectClient currentClient = (IndirectClient) config.getClients().getClients().get(0);
			try {
				currentClient.setCallbackUrl("http://localhost:8070/joker-web/");
				HttpAction redirect = currentClient.redirect(webContext);
				return false;
			} catch (HttpAction httpAction) {
				httpAction.printStackTrace();
				return false;
			}
		} else {
//		try {
			return assertAuthorized(componentClass, profiles, webContext);
//		} catch (AuthorizationException ae) {
//			// Store exception for use later in the request by onUnauthorizedInstantiation()
//			RequestCycle.get().setMetaData(EXCEPTION_KEY, ae);
//			return false;
//		}
		}
	}
	
	@Override
	public boolean isActionAuthorized(Component component, Action action) {
		return true;
	}
	
	@Override
	public boolean isResourceAuthorized(IResource resource, PageParameters parameters) {
		return true;
	}
	
	/**
	 * @throws AuthorizationException if the given class, or any of its
	 * superclasses, has a Shiro annotation that fails its
	 * authorization check.
	 */
	private boolean assertAuthorized(Class<?> componentClass, List<CommonProfile> profiles, WebContext context) throws AuthorizationException {
		// We are authorized unless we are found not to be
		boolean authorized = true;
		
		// Check class annotation first because it is more specific than package annotation
		AuthorizeInstantiation classAnnotation = componentClass.getAnnotation(AuthorizeInstantiation.class);
		if (classAnnotation != null) {
			authorized = check(classAnnotation, profiles, context);
		} else {
			// Check package annotation if there is no one on the the class
			Package componentPackage = componentClass.getPackage();
			if (componentPackage != null) {
				AuthorizeInstantiation packageAnnotation = componentPackage.getAnnotation(AuthorizeInstantiation.class);
				if (packageAnnotation != null) {
					authorized = check(packageAnnotation, profiles, context);
				}
			}
		}
		
		// Check for multiple instantiations
		AuthorizeInstantiations authorizeInstantiationsAnnotation = componentClass.getAnnotation(AuthorizeInstantiations.class);
		if (authorizeInstantiationsAnnotation != null) {
			for (AuthorizeInstantiation authorizeInstantiationAnnotation : authorizeInstantiationsAnnotation.ruleset()) {
				if (!check(authorizeInstantiationAnnotation, profiles, context)) {
					authorized = false;
				}
			}
		}
		
		return authorized;
	}
	
	/**
	 * Check if annotated instantiation is allowed.
	 *
	 * @param authorizeInstantiationAnnotation The annotations information
	 * @param profiles
	 * @param context
	 * @return False if the instantiation is not authorized
	 */
	private boolean check(AuthorizeInstantiation authorizeInstantiationAnnotation, List<CommonProfile> profiles, WebContext context) {
		// We are authorized unless we are found not to be
		boolean authorized = true;
		
		// Check class annotation first because it is more specific than package annotation
		if (authorizeInstantiationAnnotation != null) {
			RequireAnyRoleAuthorizer<CommonProfile> roleAuthorizer = new RequireAnyRoleAuthorizer<>(authorizeInstantiationAnnotation.value());
			try {
				authorized = roleAuthorizer.isAnyAuthorized(context, profiles);
			} catch (HttpAction httpAction) {
				// Should not happen since no exotic action is done during role authorization phase
				throw new WicketRuntimeException(httpAction);
			}
		}
		
		return authorized;
	}
}
