<%@ page import="com.pentaho.security.*" %>
<%@ page import="org.pentaho.core.util.*" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@
    page language="java"
     import="org.acegisecurity.ui.AbstractProcessingFilter,
            org.acegisecurity.ui.webapp.AuthenticationProcessingFilter,
            org.acegisecurity.ui.savedrequest.SavedRequest,
            org.acegisecurity.AuthenticationException,
            org.pentaho.platform.uifoundation.component.HtmlComponent,
            org.pentaho.platform.engine.core.system.PentahoSystem,
            org.pentaho.platform.util.messages.LocaleHelper,
            org.pentaho.platform.api.engine.IPentahoSession,
            org.pentaho.platform.web.http.WebTemplateHelper,
            org.pentaho.platform.api.engine.IUITemplater,
            org.pentaho.platform.web.jsp.messages.Messages,
            java.util.List,
            java.util.ArrayList,
            java.util.StringTokenizer,
            org.apache.commons.lang.StringEscapeUtils,
      org.pentaho.platform.web.http.PentahoHttpSessionHelper"
%>



  <%!
    // List of request URL strings to look for to send 401

    private List<String> send401RequestList;
  
    public void jspInit() {
      // super.jspInit(); 
      send401RequestList = new ArrayList<String>();
      String unauthList = getServletConfig().getInitParameter("send401List"); //$NON-NLS-1$
      if (unauthList == null) {
        send401RequestList.add("AdhocWebService"); //$NON-NLS-1$
      } else {
        StringTokenizer st = new StringTokenizer(unauthList, ","); //$NON-NLS-1$
        String requestStr;
        while (st.hasMoreElements()) {
          requestStr = st.nextToken();
          send401RequestList.add(requestStr.trim());
        }
      }
    }
  %>

  <%
    response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

    String path = request.getContextPath();

    IPentahoSession userSession = PentahoHttpSessionHelper.getPentahoSession( request );
    // ACEGI_SAVED_REQUEST_KEY contains the URL the user originally wanted before being redirected to the login page
    // if the requested url is in the list of URLs specified in the web.xml's init-param send401List,
    // then return a 401 status now and don't show a login page (401 means not authenticated)
  Object reqObj = request.getSession().getAttribute("ACEGI_SAVED_REQUEST_KEY"); //$NON-NLS-1$
  String requestedURL = "";
  if (reqObj != null) {
    requestedURL = ((SavedRequest) reqObj).getFullRequestUrl();
    
    String lookFor;
      for (int i=0; i<send401RequestList.size(); i++) {
        lookFor = send401RequestList.get(i);
        if ( requestedURL.indexOf(lookFor) >=0 ) {
          response.sendError(401);
          return;
        }
      }
  }


//boolean loggedIn;
//String remoteUser = request.getRemoteUser();
//if(remoteUser != null && remoteUser != ""){
//  loggedIn = true;
//}
%>
<html>
  <head>
    <title>Pentaho User Console</title>
    <meta name="gwt:property" content="locale=<%=request.getLocale()%>">
    <link rel="shortcut icon" href="/pentaho-style/favicon.ico" />
    <link href="/pentaho-style/styles-new.css" rel="stylesheet" type="text/css" />
  </head>

  <body class="body" dir="LTR">
    <center>
      <table width="1000" border="0" style="width:1000">
        <%
          ArrayList messages = new ArrayList();
          HtmlComponent html = new HtmlComponent( HtmlComponent.TYPE_URL, "http://www.pentaho.com/demo/news1.htm", Messages.getString("UI.USER_OFFLINE"), null, messages);
        %>
        <tr>
          <td width="690" valign="top">
            <h1>Getting Started with Pentaho Open BI Suite</h1>
            <p class="welcome">Thank you for choosing Pentaho. The Pentaho Open BI Suite provides a full spectrum of Business Intelligence (BI) capabilities including reporting, dashboards data mining and data integration. Use the links below to view samples and begin creating your own reports. 
            </p>
          </td>
          
          <td width="344" class='content_pagehead' align="right">
          <!--<td valign="top" style=" padding-left: 15px;">-->
          <!--BEGIN MESSAGE BOX-->
            <form id="login" action="<c:url value='j_acegi_security_check'/>" method="POST">
            <table width="270" border="0" cellpadding="0" cellspacing="0">
              <!--<tr>
                 <td width="10"><img src="/pentaho-style/images/grey_top_left.png" width="10" height="10" /></td>
                 <td bgcolor="#dedede"><img src="/pentaho-style/images/Clr.png" width="341" height="10" /></td>
                 <td width="10"><img src="/pentaho-style/images/grey_top_right.png" width="25" height="10" /></td>
              </tr>-->
              <tr>
              <td class="message_top_left"><img border="0" src="/pentaho-style/images/spacer.gif" class="message_spacer" /></td>
              <td class="message_middle_top"><img border="0" src="/pentaho-style/images/spacer.gif" /></td>
              <td class="message_top_right"><img border="0" src="/pentaho-style/images/spacer.gif" class="message_spacer" /></td>
            </tr>
              <tr>
                <td class="message_middle_left"><img border="0" src="/pentaho-style/images/spacer.gif" /></td>
                <td class="message_middle" style="vertical-align:top; width:220px;"><div id="messagebox">
            
                
                  <% String remoteUser = StringEscapeUtils.escapeXml(request.getRemoteUser()); %>
                
                  <table cellpadding="0" cellspacing="0" border="0">

                  <%  if (null != remoteUser && remoteUser.length() > 0) {%>
                                 <tr>
                             <td align="left" style="padding: 5px 0px 0px 0px;" ><%= Messages.getString("UI.USER_LOGIN_LOGGED_IN_AS", remoteUser) %></td>
                             <td align="right"></td>
                           </tr>
                               <tr>
                             <td align="left" style="padding: 5px 0px 0px 0px;" ><a href="<c:url value='Logout'/>" /><%=Messages.getString("UI.USER_LOGIN_LOGIN_AS_DIFFERENT_USER")%></a></td>
                             <td align="right"></td>
                           </tr>
                  <% } else { %>
                    <tr>
                      <td colspan="2" align="center" valign="middle" >
                        <input type="button" onClick="openLoginDialog('<%=requestedURL%>')" style="width:80%; height:50px; font-size:18px" value="<%= Messages.getString("UI.USER_LOGIN") %>"/> 
                      </td>
                    </tr>
                    <% } %>
                  </table>
                   
                </div>
              </td>
              <td class="message_middle_right"><img border="0" src="/pentaho-style/images/spacer.gif" /></td>
            </tr>
            <tr>
              <td class="message_bottom_left"><img border="0" src="/pentaho-style/images/spacer.gif" class="message_spacer" /></td>
              <td class="message_middle_bottom"><img border="0" src="/pentaho-style/images/spacer.gif" /></td>
              <td class="message_bottom_right"><img border="0" src="/pentaho-style/images/spacer.gif" class="message_spacer" /></td>
            </tr>
          </table>
          </form>
      <!--END MESSAGE BOX-->
      </td>
      </tr>
          <tr>
            <td colspan="2">&nbsp;</td>
          </tr>
      <tr>
        <td colspan="2">
          <table width="1000" cellpadding="0" cellspacing="0">
          <tr>          
            <td colspan="2">
              <table width="1000" cellpadding="0" cellspacing="0">
              <tr>
                <td width="10"><img src="/pentaho-style/images/gb_top_left.png" height="10" width="10" /></td>
                <td background="/pentaho-style/images/gb_top_mid.png" width="326"><img src="/pentaho-style/images/Clr.png" width="362" height="10" /></td>
                <td background="/pentaho-style/images/gb_top_mid.png" width="654"><img src="/pentaho-style/images/Clr.png" width="654" height="10" /></td>
                <td width="10"><img src="/pentaho-style/images/gb_top_right.png" height="10" width="10" /></td>
              </tr>

                <tr>
                  <td rowspan="5" background="/pentaho-style/images/gg_mid_left.png">&nbsp;</td>
                  <br>
                  <td valign="top" rowspan="5">
                  
              <h2>&nbsp;</h2>
            </td>
            <td>
              <table width="654" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td width="10"><img src="/pentaho-style/images/g_top_left.png" width="10" height="9" /></td>
                  <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                  <td width="10"><img src="/pentaho-style/images/g_top_right.png" width="10" height="9" /></td>
                </tr>
                <tr>
                  <td bgcolor="#dee7cb">&nbsp;</td>
                  <td width="150" bgcolor="#dee7cb"><h3>Design Custom Report</h3></td>
                  <td width="285" bgcolor="#dee7cb"><p>Report Designer is a powerful, desktop tool for designing and publishing rich, pixel-perfect reports.</p></td>
                  <td width="199" valign="bottom" bgcolor="#dee7cb"><a href="ping/report.html" class="button"><span>Learn More</span></a></td>
                  <td bgcolor="#dee7cb">&nbsp;</td>
                </tr>
                <tr>
                  <td width="10"><img src="/pentaho-style/images/g_bottom_left.png" width="10" height="10" /></td>
                  <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                  <td width="10"><img src="/pentaho-style/images/g_bottom_right.png" width="10" height="10" /></td>
                </tr>
              </table>
            </td>
                <td rowspan="5" background="/pentaho-style/images/gb_mid_right.png">&nbsp;</td>
          </tr>
          <tr>
              <td><img src="/pentaho-style/images/Clr.png" height="4" width="10" /></td>
          </tr>
          <tr>
            <td><table width="654" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td width="10"><img src="/pentaho-style/images/g_top_left.png" width="10" height="9" /></td>
                <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                <td width="10"><img src="/pentaho-style/images/g_top_right.png" width="10" height="9" /></td>
              </tr>
              <tr>
                <td bgcolor="#dee7cb">&nbsp;</td>
                <td width="150" bgcolor="#dee7cb"><h3>Ad Hoc Report</h3></td>
                <td width="285" bgcolor="#dee7cb"><p>The ad hoc reporting interface delivers self-service reporting to business users in a simple, thin-client wizard.</p></td>
                <td width="199" bgcolor="#dee7cb" valign="bottom"><a href="ping/adhoc.html" class="button"><span>Learn More</span></a></td>
                <td bgcolor="#dee7cb">&nbsp;</td>
              </tr>
              <tr>
                <td width="10"><img src="/pentaho-style/images/g_bottom_left.png" width="10" height="10" /></td>
                <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                <td width="10"><img src="/pentaho-style/images/g_bottom_right.png" width="10" height="10" /></td>
              </tr>
            </table></td>
          </tr>
          <tr>
            <td><img src="/pentaho-style/images/Clr.png" height="4" width="10" /></td>
          </tr>
          <tr>
          <td><table width="654" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td width="10"><img src="/pentaho-style/images/g_top_left.png" width="10" height="9" /></td>
                <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                <td width="10"><img src="/pentaho-style/images/g_top_right.png" width="10" height="9" /></td>
              </tr>
              <tr>
                <td bgcolor="#dee7cb">&nbsp;</td>
                <td width="150" bgcolor="#dee7cb"><h3>Samples</h3></td>
                <td width="285" bgcolor="#dee7cb"><p>The following samples demonstrate a variety of reporting, dashboards and analysis views that can be created using the Pentaho Open BI Suite:</p></td>
                <td width="199" bgcolor="#dee7cb" valign="bottom">&nbsp;</td>
                <td bgcolor="#dee7cb">&nbsp;</td>
              </tr>
              <tr>
                <td width="10"><img src="/pentaho-style/images/g_bottom_left.png" width="10" height="10" /></td>
                <td colspan="3" bgcolor="#dee7cb"><img src="/pentaho-style/images/Clr.png" width="634"  height="9"/></td>
                <td width="10"><img src="/pentaho-style/images/g_bottom_right.png" width="10" height="10" /></td>
              </tr>
            </table></td>
          </tr>
          <tr>
          <td width="10"><img src="/pentaho-style/images/gb_bottom_left.png" height="10" width="10" /></td>
            <td background="/pentaho-style/images/gb_bottom_mid.png" width="326"><img src="/pentaho-style/images/Clr.png" width="362" height="10" /></td>
            <td background="/pentaho-style/images/gb_bottom_mid.png" width="654"><img src="/pentaho-style/images/Clr.png" width="654" height="10" /></td>
            <td width="10"><img src="/pentaho-style/images/gb_bottom_right.png" height="10" width="10" /></td>
          </tr>
        </table> <!-- main lower table ends here -->
                <br>
                <br>
                <br>
                <br>
                <br>
                <br>
                <br>
                <br>
            </td>
          </tr>
      </table>   
      </td>
      </tr>
      </table>
    </center> 
  </body>

  <script language='javascript' src='mantleLogin/mantleLogin.nocache.js'></script>

</html>
<%!
// reads the exception stored by AbstractProcessingFilter
private String getUserMessage(final AuthenticationException e) {
    String userMessage = Messages.getString("UI.USER_LOGIN_FAILED_DEFAULT_REASON");
    if (null != e) {
        String errorClassName = e.getClass().getName();
        errorClassName = errorClassName.replace('.', '_');
        errorClassName = errorClassName.toUpperCase();
        String key = "UI.USER_LOGIN_FAILED_REASON_" + errorClassName;
        String tmp = Messages.getString(key);
        if (null != tmp && 0 != tmp.length() && !tmp.startsWith("!")) {
            userMessage = tmp;
        }
    }
    return userMessage;
}

%>