package org.pentaho.platform.web.http;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.web.http.messages.Messages;

public class WebTemplateHelperExperimental extends WebTemplateHelper {

	@Override
	  public String processTemplate(String template, final String title, final IPentahoSession session) {

	    template = template.replaceAll("\\{menu\\}", getMenuHtml( session ) ); //$NON-NLS-1$
		template = super.processTemplate(template, title, session);
	    return template;
	  }
		    
	
	  public String getMenuHtml( final IPentahoSession session ) {
		  
		  IMenuProvider menuProvider = (IMenuProvider) PentahoSystem.getObject(session, "IMenuProvider" ); //$NON-NLS-1$
		  if( menuProvider != null ) {
			  return menuProvider.getMenuBar("menu", "system/ui/menubar.xul", session).toString(); //$NON-NLS-1$ //$NON-NLS-2$
		  }
		  Logger.error( WebTemplateHelperExperimental.class.getName(), Messages.getString("WebTemplateHelperExperimental.ERROR_0001_COULD_NOT_CREATE_MENUBAR") ); //$NON-NLS-1$
		  return ""; //$NON-NLS-1$
	  }
}
