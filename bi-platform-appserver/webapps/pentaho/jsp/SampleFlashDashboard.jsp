<%@ page language="java" 
	import="java.util.ArrayList,
	java.util.Date,
	java.io.ByteArrayOutputStream,
	org.pentaho.platform.util.web.SimpleUrlFactory,
	org.pentaho.platform.web.jsp.messages.Messages,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.uifoundation.chart.DashboardWidgetComponent,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.VersionHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
	org.pentaho.platform.uifoundation.chart.ChartHelper,
	org.pentaho.platform.uifoundation.chart.FlashChartHelper,
	org.pentaho.platform.engine.services.solution.SolutionHelper,
  org.pentaho.platform.web.http.PentahoHttpSessionHelper
	"
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
		<title>Pentaho Regional Report - JSP Sample</title>
	</head>
	<body>
<%


// Take a look to see if we have a region parameter
String department = request.getParameter("department");
String title = "Select a region";
String region = request.getParameter("region");
String categoryName = request.getParameter("categoryName");
if( "region".equals( categoryName ) ) {
	region = request.getParameter("category");
}
if( "department".equals( categoryName ) ) {
	department = request.getParameter("category");
}
if( department != null ) {
	title = "This is headcount spending for " + region + ", " + department;
} 
else if ( region != null ) {
	title = "This is headcount spending for " + region;
}


%>

<h1 style='font-family:Arial'><%= title %></h1>

<table>
	<tr>
		<td valign="top" style="font-family:Arial;font-weight:bold">Select a Region By Clicking on the Pie Chart</br>
<%
        ArrayList messages = new ArrayList();
        SimpleParameterProvider parameters = new SimpleParameterProvider();
        parameters.setParameter( "drill-url", "SampleFlashDashboard?prochart=true&amp;categoryName=region" );
        parameters.setParameter( "inner-param", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
				StringBuffer content = new StringBuffer(); 
        FlashChartHelper.doFlashChart( "bi-developers", "dashboard", "regions.flashwidget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
%>
<%= content.toString() %>
		</td>	
			<td valign="top" style="font-family:Arial;font-weight:bold">
<%
	if( region != null ) {
%>
		Select a Department By Clicking on the Bar Chart</br>
<%
        	messages = new ArrayList();
        	parameters = new SimpleParameterProvider();
        	parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
        	parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
		parameters.setParameter( "connection", "SampleData" );
		parameters.setParameter( "query", "select department, variance from quadrant_actuals where region='{REGION}'" );
		parameters.setParameter( "REGION", region );
		parameters.setParameter( "outer-params", "REGION" );
        	parameters.setParameter( "drill-url", "SampleFlashDashboard?prochart=true&amp;region="+region+"&amp;categoryName=department" );
		content = new StringBuffer(); 
        	FlashChartHelper.doFlashChart( "bi-developers", "dashboard", "departments.flashwidget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
%>
<%= content.toString() %>
<%
	}
%>
	</tr>
	<tr>
		<td colspan="2" valign="top" style="font-family:Arial;font-weight:bold"><hr size="1"/>
	</tr>
	<tr>
		<td valign="top" style="font-family:Arial;font-weight:bold">
<%
	if( department != null ) {

		Date now = new Date();
		int seconds = now.getSeconds();
		long dialValue = Math.round((seconds/60.0)*100.0);
		
        messages = new ArrayList();
        parameters = new SimpleParameterProvider();
        parameters.setParameter( "image-width", "250"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-height", "250"); //$NON-NLS-1$ //$NON-NLS-2$
		parameters.setParameter( "value", Long.toString( dialValue ) );
		content = new StringBuffer();
        FlashChartHelper.doFlashDial( "bi-developers", "dashboard", "flashdial.widget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
%>
<%= content.toString() %>
<%
	}
%>

		</td>
		<td valign="top" style="font-family:Arial;font-weight:bold">

<%
	if( department != null ) {

        	messages = new ArrayList();
        	parameters = new SimpleParameterProvider();
		parameters.setParameter( "region", region );
		parameters.setParameter( "department", department );
		content = new StringBuffer(); 
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	SolutionHelper.doAction( "bi-developers", "dashboard/jsp", "embedded_report.xaction", "SampleFlashDashboard", parameters, outputStream , userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.write( outputStream.toString() );
	}
%>
		</td>
	</tr>
</table>

</body>
</html>
