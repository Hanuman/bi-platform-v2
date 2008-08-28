<%@ page language="java" 
	import="java.util.ArrayList,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
	org.pentaho.platform.uifoundation.chart.FlashChartHelper,
  org.pentaho.platform.web.http.PentahoHttpSessionHelper"
	 %><%

/*
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
 * Created Feb 16, 2006 
 * @author James Dixon
 */

	response.setCharacterEncoding(LocaleHelper.getSystemEncoding()); 
	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
	HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );

%>
<html>
	<head>
		<title>Flash Gauge</title>
	</head>
	<body>

<h1 style='font-family:Arial'>Flash Dial Test</h1>

<table>
<%
        ArrayList messages = new ArrayList();
        SimpleParameterProvider parameters = new SimpleParameterProvider();
        StringBuffer content = new StringBuffer(); 
%>
	<tr>
		<td valign="top" style="font-family:Arial;font-weight:bold">
<%
		Double dialValue = null;
		String value = request.getParameter("value");
		if ( value == null ) {
		    content.append( "No value specified on the URL. Example: .../DialPct?value=75 - for 75%" );
		}
		else {
			try {		    
			    dialValue = new Double( value );
			}
			catch ( Throwable t ) {
			    content.append( value ).append( " is not a valid number." );
			}
		}
		
		if ( dialValue != null ) {
	        messages = new ArrayList();
	        parameters = new SimpleParameterProvider();
	        parameters.setParameter( "image-width", "250"); //$NON-NLS-1$ //$NON-NLS-2$
	        parameters.setParameter( "image-height", "250"); //$NON-NLS-1$ //$NON-NLS-2$
			parameters.setParameter( "value", dialValue.toString() );
			parameters.setParameter( "title", "My Dial" );
			content = new StringBuffer();
	        FlashChartHelper.doFlashDial( "samples", "dashboard", "flashdial.widget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
%>
<%= content.toString() %>

		</td>
	</tr>
</table>

</body>
</html>
