package org.pentaho.platform.uifoundation.contentgen;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.util.xml.XmlHelper;

public class AdminContentGenerator extends BaseXmlContentGenerator {

	private static final long serialVersionUID = 2272261269875005948L;

	@Override
	public Log getLogger() {
		return LogFactory.getLog(AdminContentGenerator.class);
	}

	@Override
	public String getContent() throws Exception {

		String header = Messages.getString( "UI.USER_ADMIN_INTRO" ); //$NON-NLS-1$
		String admin = getAdminLinks( userSession );
		String publish = getPublisherContent( userSession );
		
		// worth putting this table into a template?
		
		StringBuilder sb = new StringBuilder();
		sb.append( "<table class='content_table' border='0' cellpadding='0' cellspacing='0' height='100%''>\n" );//$NON-NLS-1$
		sb.append( 		"<tr>\n" );//$NON-NLS-1$
		sb.append( 			"<td colspan='2' class='content_pagehead'>\n" );//$NON-NLS-1$
		sb.append( header );
		sb.append( 			"</td>\n" );//$NON-NLS-1$
		sb.append( 		"</tr>\n" );//$NON-NLS-1$
		sb.append( 		"<tr>\n" );//$NON-NLS-1$
		sb.append( 			"<td class='contentcell_half_right' width='50%'>\n" );//$NON-NLS-1$
		sb.append( admin );
		sb.append( publish );
		sb.append( 			"</td>\n" );//$NON-NLS-1$
		sb.append( 		"</tr>\n" );//$NON-NLS-1$
		sb.append( "</table>\n" );//$NON-NLS-1$
		
		return sb.toString(); //$NON-NLS-1$
	}

	private final String getAdminLinks( IPentahoSession userSession ) {
        SimpleParameterProvider parameters = new SimpleParameterProvider();
    	parameters.setParameter( "solution", "admin" );
	String navigateUrl = PentahoSystem.getApplicationContext().getBaseUrl() + "/Navigate?";
	SimpleUrlFactory urlFactory = new SimpleUrlFactory( navigateUrl );
	ArrayList messages = new ArrayList();
	INavigationComponent navigate = PentahoSystem.getNavigationComponent(userSession);
	navigate.setHrefUrl(PentahoSystem.getApplicationContext().getBaseUrl());
	navigate.setOnClick("");
	navigate.setSolutionParamName("solution");
	navigate.setPathParamName("path");
	navigate.setAllowNavigation( new Boolean(false) );
	navigate.setOptions("");
	navigate.setUrlFactory(urlFactory);
	navigate.setMessages(messages);
	// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
	navigate.validate( userSession, null );
	navigate.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameters ); //$NON-NLS-1$
	navigate.setXsl( "text/html", "admin-mini.xsl" );
	String content = navigate.getContent( "text/html" ); //$NON-NLS-1$
	return content;
}

private final String getPublisherContent( IPentahoSession userSession ) {
	Document publishersDocument = PentahoSystem.getPublishersDocument();
	if( publishersDocument != null ) {
		HashMap parameters = new HashMap();
		try
		{
			StringBuffer sb = XmlHelper.transformXml( "publishers-mini.xsl", null, publishersDocument.asXML(), parameters, new SolutionURIResolver(userSession) ); //$NON-NLS-1$
			return sb.toString();
		} catch (TransformerException e )
		{
			return Messages.getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$
		}
	}
	return Messages.getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$

}

}
