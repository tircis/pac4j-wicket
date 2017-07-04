package org.gama.pac4j.wicket;

import org.pac4j.core.config.Config;

/**
 * @author Guillaume Mary
 */
public abstract class Pac4jWicketAuthenticationStrategy {
	
	private final Config config;
	
	public Pac4jWicketAuthenticationStrategy(Config config) {
		this.config = config;
	}
	
	public Config getConfig() {
		return config;
	}
	
	abstract void init();
	
	abstract void restartResponseAtSignPage();
}
