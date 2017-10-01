package org.gama.pac4j.wicket.signin;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;

/**
 * A {@link CredentialsExtractor} that reads {@link org.pac4j.core.credentials.Credentials} from a {@link ThreadLocal}.
 * Only way found to compose with Wicket {@link org.apache.wicket.authroles.authentication.AuthenticatedWebSession#authenticate(String, String)}
 * and Pac4j {@link org.pac4j.core.client.IndirectClient} way of passing {@link org.pac4j.core.credentials.Credentials}
 * 
 * @author Guillaume Mary
 * @see Pac4jAuthenticatedWebSession#authenticate(String, String)
 */
public class ThreadLocalCredentialsExtractor implements CredentialsExtractor<UsernamePasswordCredentials> {
	
	private static final ThreadLocal<UsernamePasswordCredentials> THREAD_LOCAL = new ThreadLocal<>();
	
	@Override
	public UsernamePasswordCredentials extract(WebContext context) throws HttpAction, CredentialsException {
		return get();
	}
	
	private static UsernamePasswordCredentials get() {
		return THREAD_LOCAL.get();
	}
	
	public static void set(UsernamePasswordCredentials credentials) {
		THREAD_LOCAL.set(credentials);
	}
	
	public static void remove() {
		THREAD_LOCAL.remove();
	}
} 