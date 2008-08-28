package org.pentaho.platform.plugin.action.builtin;

import java.util.Map;
import java.util.Set;

public interface IConfiguredPojo {
	
	public Set<String> getConfigSettingsPaths();

	public boolean configure( Map<String,String> props );

}
