package org.pentaho.mantle.client.menus;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class MantleMenuBar extends MenuBar{

  SolutionBrowserPerspective solutionBrowser;
  
  public MantleMenuBar(){
    super();
  }
  public MantleMenuBar(boolean vertical){
    super(vertical);
  }
  
  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);

    final MenuItem item = getSelectedItem();
    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK: {
        if(item != null){
          hidePDFFrames(item);
        }
        break;
      }
      case Event.ONMOUSEOVER: {
        if (!"DIV".equals(event.getTarget().getNodeName())) { //$NON-NLS-1$ 
          this.getSelectedItem().addStyleDependentName("selected"); //$NON-NLS-1$
        }
        if(item != null){
          hidePDFFrames(item);
        }
        break;
      }
    }
  }

  private void hidePDFFrames(MenuItem item){
    Frame frame = getActiveBrowserPerspectiveFrame();
    if(frame == null){
      return;
    }
    if(item.getSubMenu() != null && item.getSubMenu().isVisible()){
      if(ElementUtils.elementsOverlap(item.getSubMenu().getElement(), getActiveBrowserPerspectiveFrame().getElement())){
        FrameUtils.setEmbedVisibility(getActiveBrowserPerspectiveFrame(),false);
      }
    } else if(item.getParentMenu() != null){ //popups
      if(ElementUtils.elementsOverlap(item.getParentMenu().getElement(), getActiveBrowserPerspectiveFrame().getElement())){
        FrameUtils.setEmbedVisibility(getActiveBrowserPerspectiveFrame(),false);
      }
    }
  }
  
  private Frame getActiveBrowserPerspectiveFrame(){
    return SolutionBrowserPerspective.getInstance().getCurrentFrame().getFrame();
  }
    
  @Override
  public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
    super.onPopupClosed(sender, autoClosed);
    this.getSelectedItem().removeStyleDependentName("selected"); //$NON-NLS-1$


    Frame frame = getActiveBrowserPerspectiveFrame();
    if(frame == null){
      return;
    }
    FrameUtils.setEmbedVisibility(getActiveBrowserPerspectiveFrame(),true);
    
  }
}

  