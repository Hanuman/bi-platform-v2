<%@ page language="java" 
	import="org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.test.component.TestManagerComponent,
			org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.util.VersionHelper,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.dom4j.*,
			java.io.*,
			java.util.*,
      org.pentaho.platform.web.http.PentahoHttpSessionHelper" %><%/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 23, 2005 
 * @author Gretchen Moran
 * 
 */
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding()); 
 	response.setHeader("expires", "0"); //$NON-NLS-1$	

	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();
	String thisUrl = "TestSuite?"; //$NON-NLS-1$

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();

	String content = ""; //$NON-NLS-1$
	org.pentaho.test.platform.web.ui.component.TestManagerComponent test = new org.pentaho.test.platform.web.ui.component.TestManagerComponent( urlFactory, messages );	
	test.validate( userSession, null );

	String action = request.getParameter( "action" ); //$NON-NLS-1$	
	String suiteName = request.getParameter( "suite" ); //$NON-NLS-1$	
	String testName = request.getParameter( "test" ); //$NON-NLS-1$	
	String auto = request.getParameter( "auto" ); //$NON-NLS-1$	

	test.setAuto( "true".equals( auto ) ); //$NON-NLS-1$

	if( "run".equals( action ) ) { //$NON-NLS-1$
		if( suiteName != null && testName == null ) {
			test.runSuite( suiteName );
		}
		else if ( suiteName != null && testName != null ) {
			test.runTest( suiteName, testName );
		}
	}

	content = test.getContent( "text/html" ); //$NON-NLS-1$

	if( "true".equals( auto ) ) { //$NON-NLS-1$
		content += "<script> setTimeout( 'if( document.getElementById(\"auto-check\").checked) document.location.href=autoUrl', 2000) </script>"; //$NON-NLS-1$
	}

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.getUITemplater( userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template-document.html", Messages.getString("UI.USER_TEST_SUITE_TITLE"), userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}%><%= intro %>
<div style="height:570px">
<%= content %>
</div>
<%= footer %>