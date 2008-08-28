package org.pentaho.platform.api.engine;

import java.util.Map;

public interface IAcceptsRuntimeInputs {

	public void setInputs( Map<String,Object> inputs );
	
}
