package org.pentaho.mantle.client.perspective.solutionbrowser;

import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.ManageContentCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LaunchPanel extends Frame {
  Image launchWaqrImage;
  Image launchAnalysisViewImage;
  Image manageContentImage;
  SolutionBrowserPerspective perspective;

  public LaunchPanel(SolutionBrowserPerspective perspective) {

    
    this.perspective = perspective;

    String url = "mantle/launch/launch.jsp";
    if (GWT.isScript()) {
      
      String mypath = Window.Location.getPath();
      if (!mypath.endsWith("/")) {
        mypath = mypath.substring(0, mypath.lastIndexOf("/") + 1);
      }
      mypath = mypath.replaceAll("/mantle/", "/");
      if (!mypath.endsWith("/")) {
        mypath = "/" + mypath;
      }    
      url = mypath + url;
    } else {
      url = "http://localhost:8080/pentaho/mantle/launch/launch.jsp?userid=joe&password=password";
    }
    this.setUrl(url);
    
//    FlexTable table = new FlexTable();
//    table.setStyleName("launchButtonPanel"); //$NON-NLS-1$
//
//    String pathPrefix = ""; //$NON-NLS-1$
//    // if we are not running in hosted mode, the images will be pathed differently
//    if (GWT.isScript()) {
//      pathPrefix = "mantle/"; //$NON-NLS-1$
//    }
//    
//    launchWaqrImage.setUrl(pathPrefix + "btn_ql_newreport.png"); //$NON-NLS-1$
//    launchWaqrImage.setTitle(Messages.getInstance().newAdhocReport());
//    launchWaqrImage.addClickListener(this);
//
//    launchAnalysisViewImage.setUrl(pathPrefix + "btn_ql_newanalysis.png"); //$NON-NLS-1$
//    launchAnalysisViewImage.setTitle(Messages.getInstance().newAnalysisView());
//    launchAnalysisViewImage.addClickListener(this);
//
//    manageContentImage.setUrl(pathPrefix + "btn_ql_manage.png"); //$NON-NLS-1$
//    manageContentImage.setTitle(Messages.getInstance().manageContent());
//    manageContentImage.addClickListener(this);
//
//    launchWaqrImage.setStyleName("launchImage"); //$NON-NLS-1$
//    launchAnalysisViewImage.setStyleName("launchImage"); //$NON-NLS-1$
//    manageContentImage.setStyleName("launchImage"); //$NON-NLS-1$
//
//    // set the style of contentTabPanel's "deck" (bottom)
//    setStyleName("launchPanel"); //$NON-NLS-1$
//
//    // set debug id's for selenium
//    launchWaqrImage.getElement().setAttribute("id", "launch_new_report"); //$NON-NLS-1$ //$NON-NLS-2$
//    launchAnalysisViewImage.getElement().setAttribute("id", "launch_new_analysis"); //$NON-NLS-1$ //$NON-NLS-2$
//    manageContentImage.getElement().setAttribute("id", "manage_content"); //$NON-NLS-1$ //$NON-NLS-2$
//
//    table.setWidget(0, 0, launchWaqrImage);
//    table.setWidget(0, 1, launchAnalysisViewImage);
//    table.setWidget(0, 2, manageContentImage);
//    table.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
//    table.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
//    table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
//
//    add(table);
  }
  
  @Override
  protected void onLoad() {
    hookNativeEvents(this);
  }
  
  private native void hookNativeEvents(LaunchPanel panel)/*-{
    $wnd.openWAQR = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openWAQR()();
    }
    $wnd.openAnalysis = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openAnalysis()();
    }
    $wnd.openManage = function(){
      panel.@org.pentaho.mantle.client.perspective.solutionbrowser.LaunchPanel::openManage()();
    }
  }-*/;
  
  public void openWAQR(){
    new WAQRCommand(perspective).execute();
  }

  public void openAnalysis(){
    new AnalysisViewCommand(perspective).execute();
  }

  public void openManage(){
    new ManageContentCommand(perspective).execute();
  }

}
