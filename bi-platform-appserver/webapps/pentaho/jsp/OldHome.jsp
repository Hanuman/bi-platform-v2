<%@ 
	page language="java" 
	import="
			java.util.ArrayList,
			org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.util.VersionHelper,
			org.pentaho.platform.api.ui.INavigationComponent,
			org.pentaho.platform.uifoundation.component.HtmlComponent,
			org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.uifoundation.chart.ChartHelper,
      org.pentaho.platform.web.http.PentahoHttpSessionHelper" %><%

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
 * @created Jul 23, 2005 
 * @author James Dixon
 * 
 */
 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();

	IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.getUITemplater( userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template-home.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}

%>

	<%= intro %>
	
<%
	// See if we have a 'territory' parameter
	String territory = request.getParameter("territory");
	// See if we have a 'productline' parameter
	String productline = request.getParameter("productline");

	// Create the title for the top of the page
	String title = "Top Ten Customers";
	if( territory == null && productline != null) {
		title = "Top Ten for " + productline;
	} 
	else if ( territory != null && productline == null) {
		title = "Top Ten for " + territory;
	}
	else if ( territory == null && productline == null) {
		title = "Top Ten Customers";
	}
	else  {
		title = "Top Ten for " + territory + ", " + productline;
	}
	
	String pie1 = "";
	String pie2 = "";
	String chart = "";

	SimpleParameterProvider parameters = new SimpleParameterProvider();
	parameters.setParameter( "drill-url", "PreviousHome?territory={territory}" );
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-width", "375"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "275"); //$NON-NLS-1$ //$NON-NLS-2$
	StringBuffer content = new StringBuffer(); 
	ArrayList messages = new ArrayList();
	ChartHelper.doPieChart( "samples", "steel-wheels/homeDashboard", "territory.widget.xml", parameters, content, userSession, messages, null ); 

	pie1 = content.toString();
	 
	parameters = new SimpleParameterProvider();

	if( territory == null ) {
	parameters.setParameter( "drill-url", "PreviousHome?productline={productline}" );
	} else {
	parameters.setParameter( "drill-url", "PreviousHome?territory="+territory+"&amp;productline={productline}" );
	}
	
	parameters.setParameter( "territory", territory );
	parameters.setParameter( "productline", productline );
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "inner-param", "productline"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-width", "375"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "275"); //$NON-NLS-1$ //$NON-NLS-2$
	content = new StringBuffer(); 
	messages = new ArrayList();
    ChartHelper.doPieChart( "samples", "steel-wheels/homeDashboard", "productline.widget.xml", parameters, content, userSession, messages, null ); 
	pie2 = content.toString();
	
	parameters = new SimpleParameterProvider();
	parameters.setParameter( "image-width", "500"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "525"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "territory", territory );
	parameters.setParameter( "productline", productline );  			
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "inner-param", "productline"); //$NON-NLS-1$ //$NON-NLS-2$

	content = new StringBuffer(); 
	messages = new ArrayList();
	ChartHelper.doChart( "samples/steel-wheels", "homeDashboard", "customer.widget.xml", parameters, content, userSession, messages, null ); 
	chart = content.toString();

	%>

	<%@page%>
<center>
	
		<BR/>
	
		<table style="width:1000" border="0">
			<tr>
				<!-- td>
					<span class="welcome_message"><%= Messages.getString("UI.USER_WELCOME") %></span>
				</td -->
				<td colspan='2' class='content_pagehead'>
					<%= Messages.getString( "UI.USER_HOME_INTRO" ) %>
				</td>
			</tr>
		</table>

		<BR/>

  		<table class="homeDashboard" cellpadding="0" cellspacing="0" border="0" >
			<tr>
				<td valign="top" align="center"><%= pie1 %></td>
				<td rowspan="2" valign="top">
					<%= title %>
					<%= chart %>
				</td>
			</tr>
			<tr>
				<td valign="top" align="center">
					<%= pie2 %>
				</td>
			</tr>
 		</table>	

		<table width="1000" style="padding: 5px 5px 5px 5px">
			<tr>
				<td width="500" class="content_header"><%= Messages.getString( "UI.USER_LINKS" ) %></td>
				<td width="500" class="content_header"><%= Messages.getString( "UI.USER_WHATS_NEW") %></td>
			</tr>
			<tr>
				<td valign="top" class="content_container3" style="text-align:left">
<%  messages = new ArrayList();
	HtmlComponent html = new HtmlComponent( HtmlComponent.TYPE_URL, "http://www.pentaho.org/demo/links.htm", Messages.getString("UI.USER_OFFLINE"), null, messages);
	html.validate( userSession, null ); %>
					<%=  html.getContent( "text/html" ) %>		
				</td>
				<td valign="top" class="content_container3" style="text-align:left">
<%	messages = new ArrayList();
	html = new HtmlComponent( HtmlComponent.TYPE_URL, "http://www.pentaho.org/demo/news1.htm", Messages.getString("UI.USER_OFFLINE"), null, messages);
	html.validate( userSession, null ); %>
					<%=  html.getContent( "text/html" ) %>
				</td>
			</tr>
		</table>
  </center>

	<%= footer %>
	
