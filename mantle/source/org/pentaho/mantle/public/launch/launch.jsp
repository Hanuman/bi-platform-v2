<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%><%@
    page
  language="java"
  import="org.pentaho.platform.util.messages.LocaleHelper,
            org.pentaho.platform.api.engine.IPentahoSession,
            org.pentaho.platform.web.jsp.messages.Messages,
            org.apache.commons.lang.StringEscapeUtils"%>
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
 * @created Jul 23, 2005
 * @author James Dixon
 *
 */
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">


<title><%=Messages.getString( "UI.PUC.LAUNCH.TITLE" )%></title>

<style type="text/css">
<!--
body {
  color: #000000;
  background-color: #FFFFFF;
  margin: 0px;
}

p {
  margin: 0px;
  padding: 0px;
}

A:link,A:visited,A:hover {
  color: #7e932f;
  text-decoration: underline;
}

A:hover {
  color: #ca6333;
  text-decoration: underline;
}

.launchPanel {
  background-color: white;
  background-image: url(/pentaho/mantle/launch/images/quicklaunch_bg.png);
  background-repeat: no-repeat;
  background-position: center;
  height: 100%;
}

.ql_container {
  width: 564px;
  padding-bottom: 50px;
}

.ql_icon_bar_left {
  width: 41px;
  height: 147px;
  vertical-align: top;
  padding-top: 25px;
}

.ql_icon_bar_middle {
  background-image:
    url(/pentaho/mantle/launch/images/ql_icon_bar_middle.png);
  background-repeat: repeat-x;
  background-position: 0 25px;
  width: 100%;
  height: 147px;
}

.ql_icon_bar_right {
  width: 41px;
  height: 147px;
  vertical-align: top;
  padding-top: 25px;
}

.ql_spacer {
  width: 20px;
}

.ql_btn {
  width: 167px;
}

.ql_btn_left {
  width: 41px;
  height: 56px;
}

.ql_btn_middle {
  background-image: url(/pentaho/mantle/launch/images/ql_btn_middle.png);
  background-repeat: repeat-x;
  background-position: center;
  width: 100%;
  font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
  font-size: 1.25em;
  line-height: 20px;
  font-weight: 300;
  text-align: center;
  vertical-align: top;
  white-space: nowrap;
  padding-top: 5px;
}

.ql_btn_right {
  width: 41px;
  height: 56px;
}

.ql_btn_left_hover {
  width: 41px;
  height: 56px;
}

.ql_btn_middle_hover {
  background-image:
    url(/pentaho/mantle/launch/images/ql_btn_middle_hover.png);
  background-repeat: repeat-x;
  background-position: center;
  width: 100%;
  font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
  font-size: 1.25em;
  line-height: 20px;
  font-weight: 300;
  text-align: center;
  vertical-align: top;
  white-space: nowrap;
  padding-top: 5px;
}

.ql_btn_right_hover {
  width: 41px;
  height: 56px;
}

.ql_new_report {
  width: 120px;
  height: 130px;
  padding-bottom: 13px;
}

.ql_new_analysis {
  width: 120px;
  height: 130px;
  padding-bottom: 13px;
}

.ql_manage {
  width: 120px;
  height: 130px;
  padding-bottom: 13px;
}

.ql_logo {
  width: 290px;
  height: 91px;
  padding-bottom: 30px;
}

.button{
    cursor: pointer;
    width: 167px;
    padding:0px;
    spacing:0px;
    height: 56px;
}

.btn_left{
    background-image: url("/pentaho/mantle/launch/images/ql_btn_left.png");
    background-repeat: no-repeat;
    background-position: top right;
    height:56px;
    width:22px;
}
.btn_right{
    background-image: url("/pentaho/mantle/launch/images/ql_btn_right.png");
    background-repeat: no-repeat;
    background-position: top left;
    height:56px;
    width:22px;
}
.btn_center{
  background-image: url(/pentaho/mantle/launch/images/ql_btn_middle.png);
  background-repeat: repeat-x;
  background-position: center;
  width: 100%;
  font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
  font-size: 1.25em;
  line-height: 20px;
  font-weight: 300;
  text-align: center;
  vertical-align: top;
  white-space: nowrap;
  padding-top: 5px;
}


.btn_left_hover{
    background-image: url("/pentaho/mantle/launch/images/ql_btn_left_hover.png");
    background-repeat: no-repeat;
    background-position: top right;
    height:56px;
    width:22px;
}
.btn_right_hover{
    background-image: url("/pentaho/mantle/launch/images/ql_btn_right_hover.png");
    background-repeat: no-repeat;
    background-position: top left; 
    height:56px;
    width:22px;
}
.btn_center_hover{
  background-image: url(/pentaho/mantle/launch/images/ql_btn_middle_hover.png);
  background-repeat: repeat-x;
  background-position: center;
  width: 100%;
  font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
  font-size: 1.25em;
  line-height: 20px;
  font-weight: 300;
  text-align: center;
  vertical-align: top;
  white-space: nowrap;
  padding-top: 5px;
}
-->
</style>

<script type="text/javascript">
Button = function(label, container){

var btn = document.createElement("input");
btn.setAttribute("type","button");


    table = document.createElement("table");
    document.getElementById(container).innerHTML="";
    document.getElementById(container).appendChild(table);
    
    table.setAttribute("cellpadding","0");
    table.setAttribute("cellspacing","0");
    table.setAttribute("border","0");
    table.className="button";
    table.setAttribute("height","56");
    table.setAttribute("width","167");
    table.cellSpacing = "0px";
    table.cellPadding = "0px";

    var tbody = document.createElement("tbody");
    
    var tr = document.createElement("tr");

    var left_td = document.createElement("td");
    left_td.className="btn_left";
    left_td.innerHTML = "<img src='/pentaho/mantle/launch/images/ql_spacer.png'/ width='22' height='1'/><br/>";
    tr.appendChild(left_td);

    var center_td = document.createElement("td");
    center_td.setAttribute("width","100%");
    center_td.className="btn_center";
    center_td.onselectstart=function(){return false;}
    center_td.style.MozUserSelect='none';
    center_td.innerHTML = label;
    tr.appendChild(center_td);

    var right_td = document.createElement("td");
    right_td.innerHTML = "<img src='/pentaho/mantle/launch/images/ql_spacer.png'/ width='22' height='1'/><br/>";
    right_td.className="btn_right";
    tr.appendChild(right_td);
    tbody.appendChild(tr);
    
    table.appendChild(tbody);

    table.onmouseover=function(){
	    left_td.className="btn_left_hover";
	    right_td.className="btn_right_hover";
	    center_td.className="btn_center_hover";
    };
    table.onmouseout=function(){
	    left_td.className="btn_left";
	    right_td.className="btn_right";
	    center_td.className="btn_center";
    }
    this.onClick=function(onClick){
      if(window.parent && window.parent.mantle_initialized){
        table.onclick = function(){window.parent[onClick]()};
      }
    };
    
    
}

function loader(){
    new Button("<%=Messages.getString( "UI.PUC.LAUNCH.NEW_REPORT" )%>", "launch_new_report").onClick("openWAQR");
    new Button("<%=Messages.getString( "UI.PUC.LAUNCH.NEW_ANALYSIS" )%>", "launch_new_analysis").onClick("openAnalysis");
    new Button("<%=Messages.getString( "UI.PUC.LAUNCH.MANAGE_CONTENT" )%>", "manage_content").onClick("openManage");
}

</script>

</head>

<body onLoad="loader()">

<div style="margin: 0px; padding: 0px; width: 100%; height: 100%;">
<table style="width: 100%; height: 100%;" class="launchPanel"
  cellpadding="0" cellspacing="0">
  <tr>
    <td style="vertical-align: middle;" align="center">

    <table width="564" border="0" align="center" cellpadding="0"
      cellspacing="0" class="ql_container">
      <tr>
        <td colspan="3" align="center"><img
          src="/pentaho/mantle/launch/images/ql_logo.png" alt="Pentaho.com"
          class="ql_logo" /></td>
      </tr>
      <tr>
        <td class="ql_icon_bar_left"><img
          src="/pentaho/mantle/launch/images/ql_icon_bar_left.png" width="41"
          height="147" /></td>
        <td class="ql_icon_bar_middle">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%">
          <tr>
            <td align="center" valign="top"><img
              src="/pentaho/mantle/launch/images/btn_ql_newreport.png"
              class="ql_new_report" /></td>
            <td align="center" valign="top">&nbsp;</td>
            <td align="center" valign="top"><img
              src="/pentaho/mantle/launch/images/btn_ql_newanalysis.png"
              class="ql_new_analysis" /></td>
            <td align="center" valign="top">&nbsp;</td>
            <td align="center" valign="top"><img
              src="/pentaho/mantle/launch/images/btn_ql_manage.png"
              class="ql_manage" /></td>
          </tr>
          <tr>
            <td id="launch_new_report" height="100%">
                            innerhtml
                        </td>
            <td><img src="/pentaho/mantle/launch/images/ql_spacer.png"
              class="ql_spacer" /></td>
            <td id="launch_new_analysis">
                            innerhtml
                        </td>
            <td><img src="/pentaho/mantle/launch/images/ql_spacer.png"
              class="ql_spacer" /></td>
            <td id="manage_content">
                            innerhtml
                         </td>
          </tr>
        </table>
        </td>
        <td class="ql_icon_bar_right"><img
          src="/pentaho/mantle/launch/images/ql_icon_bar_right.png"
          width="41" height="147" /></td>
      </tr>
    </table>
    </td>
  </tr>
</table>
</div>
  
</body>
</html>