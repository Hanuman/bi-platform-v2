package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

public interface IContentGenerator extends ILogger {

	public void createContent( ) throws Exception;

	public void setOutputHandler(IOutputHandler outputHandler);
	
	public void setParameterProviders( Map<String,IParameterProvider> parameterProviders );
	
	public void setCallbacks( List<Object> callbacks );
	
	public void setInstanceId( String instanceId );
	
	public void setSession( IPentahoSession userSession );

	public void setUrlFactory(IPentahoUrlFactory urlFactory);
	
	public void setMessagesList( List<String> messages );
}
