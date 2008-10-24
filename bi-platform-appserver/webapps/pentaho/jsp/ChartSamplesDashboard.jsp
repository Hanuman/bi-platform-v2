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
  org.pentaho.platform.web.http.PentahoHttpSessionHelper"%>
<%

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
 */


	// set the character encoding e.g. UFT-8
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding()); 

	// get the current Pentaho session or create a new one if needed
	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
%>
<html>
<head>
<title>Pentaho Chart Samples Dashboard</title>
</head>

<body>

<table width="90%"  border="1" cellspacing="5" cellpadding="5">
  <tr> 
    <td> <!-- -------------------------- BAR CHART ------------------------- -->
		<%

    		SimpleParameterProvider parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			StringBuffer content = new StringBuffer(); 
      		ArrayList messages = new ArrayList();
       		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "barchart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$

       	%>
		<br/>
		<%= content.toString() %>    
    
    </td>
    
    <td> <!-- -------------------------- LINE CHART ------------------------- -->
    
    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "linechart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>
    
    </td>
  </tr>
  <tr>      
    <td>  <!-- -------------------------- AREA CHART ------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "areachart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>
	</td>
    <td> <!-- -------------------------- DIAL CHART ------------------------- -->
    
    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "dialchart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>
    </td>
  </tr>
  <tr>  
    <td> <!-- -------------------------- PIE CHART ------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "piechart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
    <td> <!-- -------------------------- TIME SERIES CHART ------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "timeserieschart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
  </tr>
  <tr>
    <td> <!-- -------------------------- BAR LINE COMBO  CHART ------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "barlinecombochart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
    <td> <!-- -------------------------- XY CHART -------------------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "xychart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
  </tr>
  <tr>
    <td> <!-- -------------------------- BUBBLE CHART ------------------------- -->
    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "bubblechart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
    <td> <!-- ------------------- MULTIPLE PIE CHART (PIE GRID)  ------------------------- -->

    	<%
	    	parameters = new SimpleParameterProvider();
      		parameters.setParameter( "image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$
      		parameters.setParameter( "image-height", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			content = new StringBuffer(); 
      		messages = new ArrayList();
      		ChartHelper.doChart( "bi-developers", "charts/samplesDashboard", "piegridchart.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<br/>
		<%= content.toString() %>

	</td>
  </tr>	
</table>


</body>
</html>
