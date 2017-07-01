package org.gama.pac4j.wicket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import static org.pac4j.core.util.CommonHelper.isEmpty;

/**
 * The Pac4J-Wicket security context.
 * Gives access to current {@link J2EContext} and {@link CommonProfile}.
 * 
 * Its design mimics Wicket {@link RequestCycle}, {@link org.apache.wicket.Session} and {@link org.apache.wicket.Application}, hence is available
 * through a Thread-local (request bounded) instance via {@link #get()}.
 * 
 * Should not be instanciated outside of {@link Pac4jRequestCycleListener}.
 * 
 * @author Guillaume Mary
 * @see Pac4jRequestCycleListener
 */
public class SecurityContext {
	
	/** Holder of the Thread-attached {@link SecurityContext} */
	private static final ThreadLocal<SecurityContext> THREAD_LOCAL = new ThreadLocal<>();
	
	/**
	 * @return the Thread-attached {@link SecurityContext}
	 */
	public static SecurityContext get() {
		return THREAD_LOCAL.get();
	}
	
	/**
	 * Shortcut to {@link #get()} + {@link #getWebContext()}
	 * @return the current {@link J2EContext}
	 */
	public static J2EContext getCurrentWebContext() {
		return get().getWebContext();
	}
	
	/**
	 * Shortcut to {@link #get()} + {@link #getProfile()}
	 * @return the current connected {@link CommonProfile}, maybe null
	 */
	public static CommonProfile getCurrentProfile() {
		return get().getProfile();
	}
	
	/**
	 * Shortcut to constructor and Thread-local put.
	 * 
	 * @param requestCycle the current {@link RequestCycle}, not null
	 * @param config the Pac4J {@link Config}
	 */
	static void initCurrent(RequestCycle requestCycle, Config config) {
		SecurityContext context = new SecurityContext(requestCycle, config);
		THREAD_LOCAL.set(context);
	}
	
	/**
	 * Detaches the {@link SecurityContext} from the current Thread.
	 */
	public static void cleanCurrent() {
		THREAD_LOCAL.remove();
	}
	
	/** The created {@link J2EContext} from the request cycle HTTP request */
	private final J2EContext webContext;
	
	/** The {@link ProfileManager} created from the {@link J2EContext} */
	private final ProfileManager profileManager;
	
	/** Connected {@link CommonProfile}s */
	private final List<CommonProfile> profiles;
	
	/**
	 * Creates a {@link SecurityContext} from the request and response of the given {@link RequestCycle}.
	 * May create an empty one (null attributes) if request is not an HTTP one.
	 * 
	 * @param requestCycle the current {@link RequestCycle}, not null
	 * @param config the Pac4J {@link Config}
	 */
	private SecurityContext(RequestCycle requestCycle, Config config) {
		Object containerRequest = requestCycle.getRequest().getContainerRequest();
		Object containerResponse = requestCycle.getResponse().getContainerResponse();
		if (containerRequest instanceof HttpServletRequest && containerResponse instanceof HttpServletResponse) {
			webContext = new J2EContext(
					(HttpServletRequest) containerRequest,
					(HttpServletResponse) containerResponse
			);
			// reading profiles (algorithm took from Pac4j DefaultSecurityLogic)
			profileManager = new ProfileManager(webContext);
			boolean loadProfilesFromSession = shouldLoadProfilesFromSession(config.getClients().getClients());
			profiles = profileManager.getAll(loadProfilesFromSession);
		} else {
			// Request & Response are not for us (for instance Response can be JavaxWebSocketConnection in case of protocol upgrade)
			webContext = null;
			profileManager = null;
			profiles = null;
		}
	}
	
	/**
	 * @return the servlet {@link J2EContext} created at instanciation time
	 */
	public J2EContext getWebContext() {
		return webContext;
	}
	
	/**
	 * @return the {@link ProfileManager} created at instanciation time
	 */
	public ProfileManager getProfileManager() {
		return profileManager;
	}
	
	/**
	 * @return the {@link CommonProfile}s looked up through the {@link ProfileManager} at instanciation time
	 */
	public List<CommonProfile> getProfiles() {
		return profiles;
	}
	
	/**
	 * @return the first {@link CommonProfile} of this context, maybe null
	 */
	public CommonProfile getProfile() {
		if (profiles.isEmpty()) {
			return null;
		} else {
			return profiles.get(0);
		}
	}
	
	/**
	 * Indicates of {@link CommonProfile} should be loaded from session
	 * 
	 * 
	 * @param currentClients {@link Client} from Pac4J configuration
	 * @return true is currentClients is empty or first is an {@link IndirectClient} or {@link AnonymousClient}
	 */
	protected boolean shouldLoadProfilesFromSession(List<Client> currentClients) {
		return isEmpty(currentClients) || currentClients.get(0) instanceof IndirectClient || currentClients.get(0) instanceof AnonymousClient;
	}
}
