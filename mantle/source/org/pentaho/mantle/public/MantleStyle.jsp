<% response.setContentType("text/css"); %>

<%@page import="org.pentaho.mantle.server.*" %>

html{
  height: 100%;
}

body{
  height: 100%;
}

.launchImage {
  cursor: pointer;
  cursor: hand;
}

.launchButtonPanel {
  background-image: url(quicklaunch_icon_panel.png);
  background-repeat: no-repeat;
  background-position: center;
  width: 562px;
}

.launchPanel {
  background-color: <%= MantleStyleManager.getTabPanelBackgroundColor(request) %>;
  background-image: url(quicklaunch_bg.png);
  background-repeat: no-repeat;
  background-position: center;
  height: 100%;
}

.reportParameterDisclosurePanel {
  background-color: LightCyan;
  border-bottom:1px solid black; 
  text-decoration:none;
  cursor: pointer;
  cursor: hand;
}

.errorLabel {
  color: red;
}

.reportPageControl {
  cursor: pointer;
  cursor: hand;
  border: 1px solid black;
}

.pageControlPanel {
  background-color: LightCyan;
  border-bottom: 1px solid black;
  padding: 0px 5px;
}

.solutionDisclosureHeaderWidget {
  background-color:#EEEEEE;
  border:5px solid #EEEEEE;
  color:#778645;
  text-decoration:none;
  cursor: pointer;
  cursor: hand;
}

.solutionDisclosureHeaderWidgetHover {
  background-color:#DDDDDD;
  border:5px solid #DDDDDD; 
  color:#EF6507;
  text-decoration:none;
  cursor: pointer;
  cursor: hand;
}

.solutionDisclosureWidget {
  background-color:#EEEEEE;
  border:1px solid #808080;
  color:#778645;
  text-decoration:none;
  margin-top: 3px;
  cursor: pointer;
  cursor: hand;
}

.solutionDisclosureWidget-open .header {
 text-decoration: none;
  cursor: pointer;
  cursor: hand;
}

.solutionDisclosureWidget-closed .header {
 text-decoration: none;
  cursor: pointer;
  cursor: hand;
}

.solutionBrowserDescription {
  margin: 10px 20px 10px 20px;
}

.classicNavigatorTable {
  background-color: white;
}

.classicNavigatorTableCell {
 border-bottom: 1px solid #808080;
}

.classicNavigatorTableHeader {
 background-color: #C2CFA2;
 color: black;
 border: 1px solid #808080;
}

.classicNavigatorFileLabelHover {
  color: #EF6507;
  text-decoration: none;
  cursor: pointer;
  cursor: hand;
}

.classicNavigatorFileLabel {
  color: #778645;
  text-decoration: none;
  cursor: pointer;
  cursor: hand;
}

.classicNavigatorPanel {
  margin: 5px 5px 5px 5px;
}

.numSolutionsLabel {
  color: #778645;
  text-decoration: none;
}

.breadCrumbLabel {
  color: #778645;
  text-decoration: underline;
  cursor: pointer;
  cursor: hand;
}

.breadCrumbLabelHover {
  color: #EF6507;
  text-decoration: underline;
  cursor: pointer;
  cursor: hand;
}

.welcomeTab {
  padding: 0px 10 0px 10px;
}

.anchorLabelNoUnderline {
}

.anchorLabel {
  cursor: pointer;
  cursor: hand;
  text-decoration: underline;
}

.fileLabel {
  background-color: #ffffff;
  cursor: pointer;
  cursor: hand;
}

.fileLabelSelected {
  background-color: #B9B9B9;
  cursor: pointer;
  cursor: hand;
}

.permissionsTable {
  border: 1px solid #7f9db9;
}


.filesPanelMenuLabel {
	vertical-align: middle;
	line-height: 29px;
	background-color: #d8d8d8;
	background-image: url(style/images/subtoolbar_bg.png);
	border-bottom-width: 1px;
	border-bottom-style: solid;
	border-bottom-color: #848484;
	padding-left: 10px;
}

.browsePanelMenuLabel {
	vertical-align: middle;
	line-height: 29px;
	background-color: #d8d8d8;
	background-image: url(style/images/subtoolbar_bg.png);
	border-bottom-width: 1px;
	border-bottom-style: solid;
	border-bottom-color: #848484;
	padding-left: 10px;
}


.mainToolbar {
	height: 46px;
	background-image: url(toolbar_bg.png)
}

.gwt-DisclosurePanel-open .header {
  text-decoration: underline;
  cursor: pointer;
  cursor: hand;
}

.gwt-DisclosurePanel-closed .header {
  text-decoration: underline;
  cursor: pointer;
  cursor: hand;
}





.gwt-HorizontalSplitPanel {
}

.gwt-HorizontalSplitPanel .hsplitter {
  cursor: w-resize;
  width: <%= MantleStyleManager.getSplitPanelDividerWidth(request) %>;
  background-image: url(splitter_horz_bg.png);
  background-repeat: repeat-y;
  border-left: 1px solid #848484;
  border-right: 1px solid #848484;
}

.gwt-HorizontalSplitPanel .left {
  background-color: #E8EEF7;
}

.gwt-VerticalSplitPanel {
}

.gwt-VerticalSplitPanel .vsplitter {
  cursor: n-resize;
  background-image: url(splitter_vert_bg.png);
  background-repeat: repeat-x;
  border-top: 1px solid #848484;
  border-bottom: 1px solid #848484;
}









#loading {
  background: white;
  position: absolute;
  left: 48%;
  top: 45%;
  margin-left: -45px;
  padding: 2px;
  z-index: 20001;
  height: auto;
  border: 1px solid #ccc;
}

#loading a {
  background: white;
  color: #225588;
}

#loading .loading-indicator {
  background: white;
  color: #444;
  font: bold 13px tahoma, arial, helvetica;
  padding: 10px;
  margin: 0;
  height: auto;
}

#loading .loading-indicator img {
  background: white;
  margin-right:8px;
  float:left;
  vertical-align:top;
}

#loading-msg {
  background: white;
  font: normal 10px arial, tahoma, sans-serif;
}

.applicationShell {
	  border: 0px;
	  height: 100%;
	  width: 100%;
}

* {
  margin: 0px;
  padding: 0px;
}

body {
  background-color: <%= MantleStyleManager.getBodyBackground(request) %>;
  color: black;
  font-family: Tahoma, Arial, Helvetica, sans-serif;
  font-size: 10pt;
  margin: 0px;
  padding: 0px;
}

h2 {
  font-weight: normal;
}

table {
  font-size: 100%;
}

code {
  font-size: small;
}


a, a:visited {
  color:#ef8033;
}

a:active, a:hover {
  color:#949e3e;
}

.filePropertyTabContent {
  border-left: 2px solid #87944C;
  border-bottom: 2px solid #87944C;
  border-right: 2px solid #87944C;
}

.filePropertiesDialogContent {
  background: white;
}


.backgroundContentAction {
  color: blue;
  cursor: pointer;
  cursor: hand;
}

.backgroundContentTable content {
  margin: 10px 10px 10px 10px;
  padding: 10px 10px 10px 10px;
}

.backgroundContentTable {
 border: 1px solid black;
 background: white;
}

.backgroundContentHeaderTableCell {
 background: #c8c8c8;
 padding: 0px 10px 0px 10px;
 border-right: 1px solid black;
 border-bottom: 1px solid black;
}

.backgroundContentHeaderTableCellRight {
 background: #c8c8c8;
 padding: 0px 10px 0px 10px;
 border-bottom: 1px solid black;
}

.backgroundContentTableCell {
 padding: 0px 10px 0px 10px;
 border-right: 1px solid black;
 border-bottom: 1px solid black;
}

.backgroundContentTableCellRight {
 padding: 0px 10px 0px 10px;
 border-bottom: 1px solid black;
}

.backgroundContentTableCellBottom {
 padding: 0px 10px 0px 10px;
 border-right: 1px solid black;
}

.backgroundContentTableCellBottomRight {
 padding: 0px 10px 0px 10px;
}


.gwt-BorderedPanel {
}

.gwt-Button {
   height:22px;
   background-image: expression('url(buttonbg.gif)');
   background-image:url(buttonbg.gif);
   font-weight: plain;
   border-style:solid;
   border-color:#000000;
   border-width:1px;
   margin: 0px 4px 2px 4px;
}

.gwt-Button:hover {
   margin: 0px 4px 2px 4px;
   height:22px;
   background-image:url(buttonhoverbg.gif);
   background-image: expression('url(buttonhoverbg.gif)');
   font-weight: plain;
   border-style:solid;
   border-color:#000000;
   border-width:1px;
   cursor: pointer;
   cursor: hand;
}

.gwt-Button:disabled {
   margin: 0px 4px 2px 4px;
   height:22px;
   background-image:url(buttonbg.gif);
   background-image: expression('url(buttonbg.gif)');
   border-style:solid;
   border-color:#c0c0c0;
   border-width:1px;
   cursor: default;
}

.gwt-Canvas {
}

.gwt-CheckBox {
}

.gwt-CheckBox label {
  margin: 0px 0px 0px 4px;
}

.dialogContentPanel {
	background-color: #e8eefa;
  	padding: 0px 0px 0px 0px;
    font-size: 12px;
}

.viewContentDialogBox {
  background-color: #ececec;
  border: 3px solid #000000;
  padding: 2px 2px 2px 2px;
}

.viewContentDialogBox .Caption {
  background-color: #d0d0d0;
  color: black;
  cursor: move;
  border-bottom: 1px solid #000000;
  text-align: left;
  padding: 3px 0px 3px 5px;
}

.gwt-FileUpload {
}

.gwt-Frame {
	background-color: <%= MantleStyleManager.getBodyBackground(request) %>;
	border: none;
	overflow:visible; 
	width:100%; 
}

.gwt-HTML {
  text-align: left;
}

.gwt-Hyperlink {
}

.gwt-Image {
}

.gwt-Label {
}

.gwt-ListBox {
}


.menuBarAndLogoPanel {
  background-color: #dbdbdb;
  background-image:url(menubar_bg.png);
  background-position: top;
  background-repeat: repeat-x;
  border-bottom: 1px solid #848484;
  
}

.logoPanel-ContainerToolBar {
  background-image:url(toolbar_bg.png);
  background-position: bottom;
  background-repeat: repeat-x;
}

.logoPanel-Container {
  background-image:url(logo_bg.png);
  background-position: center;
  background-repeat: no-repeat;
}


gwt-TabPanelBottom {
  background-color: #d8d8d8;
}


.gwt-TabBar {
  background-image: url(tab_background.png);
  background-repeat: repeat-x;
  height: 37px;
}

.gwt-TabBar .tabWidget {
  background-image: url(tabright.png);
  background-position: 100% 0px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;
}

.gwt-TabBar .tabWidget-selected {
  background-image: url(tabright.png);
  background-position: 100% -42px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;
}

.gwt-TabBar .tabWidget-hover {
  background-image: url(tabright.png);
  background-position: 100% -84px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;  
}

.gwt-TabBar .tabWidgetCap {
  background-image: url(tableft.png);
  background-position: 100% 0px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;
}

.gwt-TabBar .tabWidgetCap-selected {
  background-image: url(tableft.png);
  background-position: 100% -42px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;
}

.gwt-TabBar .tabWidgetCap-hover {
  background-image: url(tableft.png);
  background-position: 100% -84px;
  cursor: pointer;
  cursor: hand;
  height: 32px;
  margin-top: 5px;
}




.gwt-Dialog-TabPanel .gwt-TabPanelBottom {
  background-color: #e2e2e2;
  border-left: 1px solid #8d8d8d;
  border-bottom: 1px solid #8d8d8d;
  border-right: 1px solid #8d8d8d;
  padding: 5px;
}

.gwt-Dialog-TabBar {
  background-image: url(dialog_tab_bg.png);
  background-repeat: repeat-x;
  height: 29px;
}

.gwt-Dialog-TabBar .tabWidget {
  background-image: url(dialog_tab_right.png);
  background-position: 100% 0px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 24px;
  margin-top: 4px;
}

.gwt-Dialog-TabBar .tabWidget-selected {
  background-image: url(dialog_tab_right.png);
  background-position: 100% -42px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 24px;
  margin-top: 4px;
}

.gwt-Dialog-TabBar .tabWidget-hover {
  background-image: url(dialog_tab_right.png);
  background-position: 100% -84px;
  margin-right: 2px;
  cursor: pointer;
  cursor: hand;
  height: 24px;
  margin-top: 4px;  
}

.gwt-Dialog-TabBar .tabWidgetCap {
  background-image: url(dialog_tab_left.png);
  background-position: 100% 0px;
  cursor: pointer;
  cursor: hand;
  height: 25px;
  margin-top: 4px;
  width: 6px;
}

.gwt-Dialog-TabBar .tabWidgetCap-selected {
  background-image: url(dialog_tab_left.png);
  background-position: 100% -42px;
  cursor: pointer;
  cursor: hand;
  height: 25px;
  margin-top: 4px;
  width: 6px;
}

.gwt-Dialog-TabBar .tabWidgetCap-hover {
  background-image: url(dialog_tab_left.png);
  background-position: 100% -84px;
  cursor: pointer;
  cursor: hand;
  height: 25px;
  margin-top: 4px;
  width: 6px;
}










.gwt-TextBox-readonly {
  color: #888;
}

.gwt-Tree {
  background: white;
}

.gwt-Tree .gwt-TreeItem {
  white-space: nowrap;
  cursor: pointer;
  cursor: hand;
}

.gwt-Tree .gwt-TreeItem-selected {
  white-space: nowrap;
  background-color: #DDDDDD;
  cursor: pointer;
  cursor: hand;
}

.gwt-StackPanel {
  background-color: #EEEEEE;
  border: 1px solid #808080;
}

.gwt-StackPanel .gwt-StackPanelItem {
  padding: 5px 5px 5px 5px;
  background-color: #EEEEEE;
  cursor: pointer;
  cursor: hand;
}

.gwt-PushButton-up {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
  cursor: pointer;
  cursor: hand;
}

.gwt-PushButton-up-hovering {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
  cursor: pointer;
  cursor: hand;
}

.gwt-PushButton-down {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
  cursor: pointer;
  cursor: hand;
}

.gwt-PushButton-down-hovering {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
  cursor: pointer;
  cursor: hand;
}

.gwt-ToggleButton-up {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
  cursor: pointer;
  cursor: hand;
}

.gwt-ToggleButton-up-hovering {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  border: 2px solid transparent;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
  cursor: pointer;
  cursor: hand;
}

.gwt-ToggleButton-down {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  background-color: #E8F1FF;
  border: 2px solid transparent;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
  cursor: pointer;
  cursor: hand;
}

.gwt-ToggleButton-down-hovering {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
  background-color: #E8F1FF;
  border: 2px solid transparent;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
  cursor: pointer;
  cursor: hand;
}

.gwt-RichTextArea {
  border: 1px solid black;
  background-color: white;
}

.gwt-RichTextToolbar {
  background-color: #C3D9FF;
  padding: 2px 2px 2px 2px;
}

.gwt-RichTextToolbar .gwt-PushButton-up {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
}

.gwt-RichTextToolbar .gwt-PushButton-up-hovering {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
}

.gwt-RichTextToolbar .gwt-PushButton-down {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
}

.gwt-RichTextToolbar .gwt-PushButton-down-hovering {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
}

.gwt-RichTextToolbar .gwt-ToggleButton-up {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
}

.gwt-RichTextToolbar .gwt-ToggleButton-up-hovering {
  margin-right: 2px;
  border: 1px solid #C3D9FF;
  border-color: #E8F1FF rgb(157, 174, 205) rgb(157, 174, 205) rgb(232, 241, 255);
}

.gwt-RichTextToolbar .gwt-ToggleButton-down {
  margin-right: 2px;
  background-color: #E8F1FF;
  border: 1px solid #C3D9FF;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
}

.gwt-RichTextToolbar .gwt-ToggleButton-down-hovering {
  margin-right: 2px;
  background-color: #E8F1FF;
  border: 1px solid #C3D9FF;
  border-color: #9DAECD rgb(232, 241, 255) rgb(232, 241, 255) rgb(157, 174, 205);
}


.gwt-SuggestBoxPopup {
  background-color: #FEF9CE;
  border-bottom: 1px solid black;
  border-right: 1px solid black;
}

.gwt-SuggestBoxPopup .item {
  padding: 2px 2px 2px 2px;
  cursor: pointer;
  cursor: hand;
}

.gwt-SuggestBoxPopup .item-selected {
  color: white;
  background-color: #87944c;
  border: 1px solid #87944c;
  padding: 2px 2px 2px 2px;
  cursor: pointer;
  cursor: hand;
}

.gwt-SliderBar-shell {
  border: 2px solid #faf9f7;
  border-right: 2px solid #848280;
  border-bottom: 2px solid #848280;
  background-color: #efebe7;
  height: 34pt;
  width: 50%;
}
.gwt-SliderBar-shell .gwt-SliderBar-line {
  border: 1px solid black;
  background-color: white;
  height: 4px;
  width: 95%;
  top: 22pt;
  overflow: hidden;
}
.gwt-SliderBar-shell .gwt-SliderBar-knob {
  top: 14pt;
  width: 11px;
  height: 21px;
  z-index: 1;
  cursor: pointer;
}
.gwt-SliderBar-shell .gwt-SliderBar-tick {
  top: 12pt;
  width: 1px;
  height: 8pt;
  background: black;
  overflow: hidden;
}
.gwt-SliderBar-shell .gwt-SliderBar-label {
  top: 2pt;
  font-size: 8pt;
  cursor: default;
}
.gwt-SliderBar-shell-focused {
}
.gwt-SliderBar-shell .gwt-SliderBar-line-sliding {
  background-color: #DDDDDD;
  cursor: pointer;
}

.titledToolbar{
  position:absolute;
  width:100%;
  
}

.panelWithTitledToolbar{
  height: 100%;
  background-color: #FFF;
}

.files-list-panel{
  position:absolute;
  height:100% !important;
  height:100% -29px;
  width:100%;
  overflow:auto;
}
.files-toolbar{
  width:100%;
  height:29px;
  position:absolute;
  z-index: 10;
  overflow: hidden;
}

.folder-list-panel{
  position:absolute;
  height:100% !important;
  height:100% -29px;
  width:100%;
  overflow:auto;
}

.gwt-PopupPanel{
  z-index:100;
}

.waitPopup{
  position: absolute;
  left: 48%;
  top: 45%;
  padding: 15px 8px;
  z-index: 20001;
  height: auto;
  border: 1px solid #ccc;
  background: white;
}
.waitPopup_table{
  color: #444;
  background-image: url(large-loading.gif);
  background-position: left center;
  background-repeat: no-repeat;
  font: bold 13px tahoma, arial, helvetica;
}

.waitPopup_title{

  color: #444;
  font: bold 16px tahoma, arial, helvetica;
  margin-left: 40px;
  padding-bottom: 3px;
}

.waitPopup_msg{
  font: normal 13px arial, tahoma, sans-serif;
  margin-left: 40px;

}
