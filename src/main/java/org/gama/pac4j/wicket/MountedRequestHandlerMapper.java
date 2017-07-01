package org.gama.pac4j.wicket;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.AbstractMapper;

/**
 * A very simple {@link org.apache.wicket.request.IRequestMapper} that can be used to mount a {@link IRequestHandler} on a path.
 * Helps to mount callback URL for indirect authentication for instance.
 * 
 * @author Guillaume Mary
 */
public class MountedRequestHandlerMapper extends AbstractMapper {
	
	private final String mountPath;
	
	private final IRequestHandler targetRequestHandler;
	
	public MountedRequestHandlerMapper(String mountPath, IRequestHandler targetRequestHandler) {
		this.mountPath = mountPath;
		this.targetRequestHandler = targetRequestHandler;
	}
	
	@Override
	public IRequestHandler mapRequest(Request request) {
		// our handler if we match
		return matches(request) ? targetRequestHandler : null;
	}
	
	
	@Override
	public int getCompatibilityScore(Request request) {
		// full score if we match
		return matches(request) ? Integer.MAX_VALUE : 0;
	}
	
	@Override
	public Url mapHandler(IRequestHandler requestHandler) {
		Url result = null;
		// if target handler is (a subclass of) ours, then we map
		if (targetRequestHandler.getClass().isAssignableFrom(requestHandler.getClass())) {
			result = new Url();
			result.getSegments().add(mountPath);
		}
		return result;
	}
	
	private boolean matches(Request request) {
		// we match if request starts with us
		return urlStartsWith(request.getUrl(), mountPath);
	}
	
}
