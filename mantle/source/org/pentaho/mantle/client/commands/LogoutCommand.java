package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class LogoutCommand implements Command {

  public LogoutCommand() {
  }

  public native void execute() /*-{
    
    var loc = $wnd.location.href.substring(0, $wnd.location.href.lastIndexOf('/')) + "/Logout";
    if($wnd.opener != null){
      try{
        if($wnd.opener.location.href.indexOf($wnd.location.host) > -1){
          $wnd.opener.location.href = loc;
          $wnd.close();
          return;
        }
      } catch(e){
        //XSS exception when original window changes domain
      }
    }
    $wnd.open(loc, "_top","");
  
    
  }-*/;

}
