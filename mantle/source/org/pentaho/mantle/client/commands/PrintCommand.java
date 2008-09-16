package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class PrintCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public PrintCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    printFrame(navigatorPerspective.getCurrentFrameElementId());
  }

  /**
   * This method will print the frame with the given element id.
   * 
   * @param elementId
   */
  public static native void printFrame(String elementId) /*-{
    var frame = $doc.getElementById(elementId);
    if (!frame) { 
      $wnd.alert("Error: Can't find printing frame. Please try again."); 
      return; 
    } 
    frame = frame.contentWindow;
    frame.focus();    
    frame.print();
  }-*/;

}
