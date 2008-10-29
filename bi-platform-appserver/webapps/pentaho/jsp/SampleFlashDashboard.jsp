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
		<title>Pentaho Territory Dashboard</title>
	</head>
	<body>
<%


// Take a look to see if we have a region parameter
String productline = request.getParameter("productline");
String title = "Sales for All Territories";
String territory = request.getParameter("territory");
String categoryName = request.getParameter("categoryName");
if( "territory".equals( categoryName ) ) {
	territory = request.getParameter("category");
}
if( "productline".equals( categoryName ) ) {
	productline = request.getParameter("category");
}
if( productline != null ) {
	title = "Sales for " + territory + ", " + productline;
} 
else if ( territory != null ) {
	title = "Sales for " + territory;
}


%>

  	<table  background="/sw-style/active/banner.png">
  		<tr>
  			<td width="800" height="40" align="center" valign="middle" style="font-family:Arial;font-weight:bold" border="0"/><%= title %></td>
  		</tr>		
  	</table>	
  	<table class="homeDashboard" cellpadding="0" cellspacing="0" border="0" >
<table>
	<tr>
		<td valign="top" style="font-family:Arial;font-weight:normal">Click on a Territory</br>
<%
        ArrayList messages = new ArrayList();
        SimpleParameterProvider parameters = new SimpleParameterProvider();
        parameters.setParameter( "drill-url", "SampleFlashDashboard?prochart=true&amp;categoryName=territory" );
        parameters.setParameter( "inner-param", "TERRITORY"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-width", "370"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-height", "275"); //$NON-NLS-1$ //$NON-NLS-2$
				StringBuffer content = new StringBuffer(); 
        FlashChartHelper.doFlashChart( "steel-wheels", "dashboards", "territory.flashwidget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
%>
<%= content.toString() %>
		</td>	
			<td valign="top" style="font-family:Arial;font-weight:normal">
<%
	if( territory != null ) {
%>
		Click on a Product Line</br>
<%
        	messages = new ArrayList();
        	parameters = new SimpleParameterProvider();
        	parameters.setParameter( "image-width", "430"); //$NON-NLS-1$ //$NON-NLS-2$
        	parameters.setParameter( "image-height", "275"); //$NON-NLS-1$ //$NON-NLS-2$
		parameters.setParameter( "connection", "SampleData" );
		parameters.setParameter( "query", "SELECT PRODUCTS.PRODUCTLINE, SUM(ORDERDETAILS.QUANTITYORDERED*ORDERDETAILS.PRICEEACH) REVENUE FROM ORDERS INNER JOIN ORDERDETAILS ON ORDERS.ORDERNUMBER = ORDERDETAILS.ORDERNUMBER INNER JOIN PRODUCTS ON ORDERDETAILS.PRODUCTCODE =PRODUCTS.PRODUCTCODE  INNER JOIN CUSTOMERS ON ORDERS.CUSTOMERNUMBER =CUSTOMERS.CUSTOMERNUMBER  INNER JOIN EMPLOYEES ON CUSTOMERS.SALESREPEMPLOYEENUMBER = EMPLOYEES.EMPLOYEENUMBER INNER JOIN OFFICES ON EMPLOYEES.OFFICECODE=OFFICES.OFFICECODE WHERE TERRITORY='{TERRITORY}' GROUP BY PRODUCTS.PRODUCTLINE ORDER BY 2 DESC" );
		parameters.setParameter( "TERRITORY", territory );
		parameters.setParameter( "outer-params", "TERRITORY" );
        	parameters.setParameter( "drill-url", "SampleFlashDashboard?prochart=true&amp;territory="+territory+"&amp;categoryName=productline" );
		content = new StringBuffer(); 
        	FlashChartHelper.doFlashChart( "steel-wheels", "dashboards", "productline.flashwidget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		<td valign="top" align="center" style="font-family:Arial;font-weight:bold">
<%
	if( productline != null ) {

		Date now = new Date();
		int seconds = now.getSeconds();
		long dialValue = Math.round((seconds/60.0)*100.0);
		
        messages = new ArrayList();
        parameters = new SimpleParameterProvider();
        parameters.setParameter( "image-width", "250"); //$NON-NLS-1$ //$NON-NLS-2$
        parameters.setParameter( "image-height", "250"); //$NON-NLS-1$ //$NON-NLS-2$
		parameters.setParameter( "value", Long.toString( dialValue ) );
		content = new StringBuffer();
        FlashChartHelper.doFlashDial( "steel-wheels", "dashboards", "flashdial.widget.xml", parameters, content, userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
%>
<%= content.toString() %>
<%
	}
%>

		</td>
		<td align="center" valign="middle" style="font-family:Arial;font-weight:bold">

<%
	if( productline != null ) {

        	messages = new ArrayList();
        	parameters = new SimpleParameterProvider();
		parameters.setParameter( "TERRITORY", territory );
		parameters.setParameter( "PRODUCTLINE", productline );
		content = new StringBuffer(); 
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	SolutionHelper.doAction( "steel-wheels", "dashboards/jsp", "Sales_by_Employee.xaction", "SampleFlashDashboard", parameters, outputStream , userSession, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		out.write( outputStream.toString() );
	}
%>
		</td>
	</tr>
</table>

</body>
</html>
