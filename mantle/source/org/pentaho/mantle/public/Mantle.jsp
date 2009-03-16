<%@page import="java.util.Locale"%>
<%@page import="org.pentaho.platform.util.messages.LocaleHelper"%>
<%@page import="org.apache.commons.lang.StringUtils"%>

<% 
  Locale effectiveLocale = request.getLocale(); 
  if (!StringUtils.isEmpty(request.getParameter("locale"))) {
    effectiveLocale = new Locale(request.getParameter("locale"));
    request.getSession().setAttribute("locale_override", request.getParameter("locale"));
    LocaleHelper.setLocaleOverride(effectiveLocale);
  }
%>

<html>
	<head>
		<title>Pentaho User Console</title>
		<meta name="gwt:property" content="locale=<%=effectiveLocale%>">
		<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />
		<link rel='stylesheet' href='mantle/MantleStyle.css'/>
		<!--[if lt IE 7.]>
			<script defer type="text/javascript" src="pngfix.js"></script>
		<![endif]-->
		<script type="text/javascript">
			if(window.opener && window.opener.reportWindowOpened != undefined){
			  window.opener.reportWindowOpened();
			}
		</script>
	</head>

	<body oncontextmenu="return false;">

	<div id="loading">
    		<div class="loading-indicator">
    			<img src="mantle/large-loading.gif" width="32" height="32"/>Pentaho User Console is Loading...<a href="http://www.pentaho.com"></a><br/>
    			<span id="loading-msg">Please Wait</span>
    		</div>
	</div>
		
	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>

	</body>

	<script language='javascript' src='mantle/mantle.nocache.js'></script>

</html>
