package org.gama.pac4j.wicket;

import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.pac4j.core.config.Config;

/**
 * Wicket {@link IRequestCycleListener} that will create a new {@link SecurityContext} for each request.
 * 
 * @author Guillaume Mary
 * @see SecurityContext
 */
public class Pac4jRequestCycleListener implements IRequestCycleListener {
	
	/** Pac4J {@link Config} for {@link SecurityContext} */
	private final Config config;
	
	/**
	 * Unique constructor that needs a {@link Config} to be passed to each created {@link SecurityContext}
	 * @param config a (non null) {@link Config}
	 */
	public Pac4jRequestCycleListener(Config config) {
		this.config = config;
	}
	
	/**
	 * This implementation creates a new {@link SecurityContext} available through {@link SecurityContext#get()}
	 * @param cycle the current {@link RequestCycle}
	 */
	public void onBeginRequest(RequestCycle cycle) {
		SecurityContext.initCurrent(cycle, config);
	}
	
	/**
	 * Detaches current {@link SecurityContext} from its Thread
	 * @param cycle the current {@link RequestCycle}
	 */
	public void onEndRequest(RequestCycle cycle) {
		SecurityContext.cleanCurrent();
	}
	
}
