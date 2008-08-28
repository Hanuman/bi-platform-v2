package org.pentaho.platform.uifoundation.contentgen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class NavigationContentGenerator extends BaseXmlContentGenerator {

	private static final long serialVersionUID = 2272261269875005948L;

	@Override
	public Log getLogger() {
		return LogFactory.getLog(NavigationContentGenerator.class);
	}

	@Override
	public String getContent() throws Exception {

		String solution = requestParameters.getStringParameter("solution", null); //$NON-NLS-1$
		if( "".equals( solution ) ) { //$NON-NLS-1$
			solution = null;
		}

		INavigationComponent navigate = PentahoSystem.getNavigationComponent(userSession);
		navigate.setHrefUrl(baseUrl);
		navigate.setOnClick("");
		navigate.setSolutionParamName("solution");
		navigate.setPathParamName("path");
		navigate.setAllowNavigation(new Boolean(solution != null));
		navigate.setOptions("");
		navigate.setUrlFactory(urlFactory);
		navigate.setMessages(messages);
		// This line will override the default setting of the navigate component
		// to allow debugging of the generated HTML.
		// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
		navigate.validate( userSession, null );
		navigate.setParameterProvider( IParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
		navigate.setParameterProvider( IParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
		
		String view = requestParameters.getStringParameter("view", null );//$NON-NLS-1$
		if( view != null ) {
			if( "default".equals( view ) ) { //$NON-NLS-1$
				userSession.removeAttribute( "pentaho-ui-folder-style" ); //$NON-NLS-1$
			} else {
				userSession.setAttribute( "pentaho-ui-folder-style", view );
				navigate.setXsl( "text/html", view ); //$NON-NLS-1$
			}
		} else {
			view = (String) userSession.getAttribute( "pentaho-ui-folder-style" );
			if( view != null ) {
				navigate.setXsl( "text/html", view ); //$NON-NLS-1$
			}
		}
		
		return navigate.getContent( "text/html" ); //$NON-NLS-1$
	}


}
