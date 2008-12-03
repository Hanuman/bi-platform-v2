package org.pentaho.platform.engine.services.solution;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.system.PentahoBase;

public abstract class BaseContentGenerator extends PentahoBase implements IContentGenerator {

	protected String instanceId;
	
	protected Map<String, IParameterProvider> parameterProviders;
	
	protected IPentahoSession userSession;
	
	protected List<Object> callbacks;
	
	protected IPentahoUrlFactory urlFactory;
	
	protected List<String> messages;

	protected IOutputHandler outputHandler;
	
	public abstract Log getLogger();
	
	public abstract void createContent() throws Exception;
	
	public void setCallbacks(List<Object> callbacks) {
		this.callbacks = callbacks;
	}

	protected Object getCallback( Class<?> clazz ) {
		if( callbacks == null || callbacks.size() == 0 ) {
			// there are no callbacks
			return null;
		}
		
		// see if we have a callback of the appropriate type
		for( Object obj : callbacks ) {
			Class<?> interfaces[] = obj.getClass().getInterfaces();
			if( interfaces != null && interfaces.length > 0 ) {
				for( Class<? extends Object> interfaze : interfaces ) {
					if( interfaze.equals( clazz ) ) {
						// we found it
						return obj;
					}
				}
			}
		}
		// we did not find a callback of the requested type
		return null;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setParameterProviders( Map<String, IParameterProvider> parameterProviders) {
		this.parameterProviders = parameterProviders;
	}

	public void setSession(IPentahoSession userSession) {
		this.userSession = userSession;
	}

	public void setUrlFactory(IPentahoUrlFactory urlFactory) {
		this.urlFactory = urlFactory;
	}

	public void setMessagesList( List<String> messages ) {
		this.messages = messages;
	}

	public void setOutputHandler(IOutputHandler outputHandler) {
		this.outputHandler = outputHandler;
	}
	
	 public void setInput( IPentahoStreamSource item ) {
	   // most content generators won't use this so we ignore it
	   // override this method if a content generator needs the input stream
	 }


}
