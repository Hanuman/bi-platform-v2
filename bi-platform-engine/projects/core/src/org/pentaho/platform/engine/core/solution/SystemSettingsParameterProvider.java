package org.pentaho.platform.engine.core.solution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SystemSettingsParameterProvider extends BaseParameterProvider {

	@Override
	public Object getParameter(String name) {
		return getValue( name );
	}

	public static String getSystemSetting(String path) {
		// parse out the path
		int pos1 = path.indexOf( '{' );
		int pos2 = path.indexOf( '}' );
		if( pos1 > 0 && pos2 > 0 ) {
			String file = path.substring( 0, pos1 );
			String setting = path.substring( pos1+1, pos2 );
			String value = PentahoSystem.getSystemSetting( file, setting, null );
			if( value != null ) {
				return value;
			}
		}
		return null;
	}
	
	@Override
	protected String getValue(String path) {
		// parse out the path
		return getSystemSetting( path );
	}

	public Iterator getParameterNames() {
		// this will return hundreds of things so we're just going to return an empty list
		List<String> list = new ArrayList<String>();
		return list.iterator();
	}

}
