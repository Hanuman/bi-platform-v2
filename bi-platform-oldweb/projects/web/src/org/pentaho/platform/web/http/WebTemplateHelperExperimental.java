package org.pentaho.platform.web.http;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class WebTemplateHelperExperimental extends WebTemplateHelper {

	@Override
	  public String processTemplate(String template, final String title, final IPentahoSession session) {

	    template = template.replaceAll("\\{menu\\}", getMenuHtml( session ) ); //$NON-NLS-1$ //$NON-NLS-2$
		template = super.processTemplate(template, title, session);
	    return template;
	  }
		    
	
	  public String getMenuHtml( final IPentahoSession session ) {
		  
		  IMenuProvider menuProvider = (IMenuProvider) PentahoSystem.getObject(session, "IMenuProvider" );
		  return menuProvider.getMenuBar("menu", session).toString();
	  }
}
