package org.pentaho.platform.engine.core.solution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;

public class CustomSettingsParameterProvider extends BaseParameterProvider {

	private IPentahoSession session;
	
	public void setSession(IPentahoSession session) {
		this.session = session;
	}

	@Override
	public Object getParameter(String name) {
		return getValue( name );
	}

	@Override
	protected String getValue(String path) {
		// apply templates to the part
		if( session != null ) {
			path = path.replace( "{$user}" , session.getName() );
		}
		return SystemSettingsParameterProvider.getSystemSetting( "components/"+path );
	}

	public Iterator getParameterNames() {
		// this will return hundreds of things so we're just going to return an empty list
		List<String> list = new ArrayList<String>();
		return list.iterator();
	}

}
