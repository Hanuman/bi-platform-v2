package org.pentaho.mantle.client.perspective.solutionbrowser;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;

public class MantlePopupPanel extends PopupPanel{
  
  public MantlePopupPanel(){
    this(true);
  }
  
  public MantlePopupPanel(boolean autohide){
    super(autohide);
    
    // This catches auto-hiding initiated closes
    addPopupListener(new PopupListener(){

      public void onPopupClosed(PopupPanel arg0, boolean arg1) {

        ReloadableIFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getCurrentFrame();
        if(iframeTab == null || iframeTab.getFrame() == null){
          return;
        }
        Frame currentFrame = iframeTab.getFrame();
        FrameUtils.setEmbedVisibility(currentFrame, true);
      }
      
    });
  }
  
  @Override
  public void hide() {
    super.hide();

    ReloadableIFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getCurrentFrame();
    if(iframeTab == null || iframeTab.getFrame() == null){
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    FrameUtils.setEmbedVisibility(currentFrame, true);
  }

  @Override
  public void show() {

    super.show();
    ReloadableIFrameTabPanel iframeTab = SolutionBrowserPerspective.getInstance().getCurrentFrame();
    if(iframeTab == null || iframeTab.getFrame() == null){
      return;
    }
    Frame currentFrame = iframeTab.getFrame();
    if(ElementUtils.elementsOverlap(this.getElement(), 
        currentFrame.getElement())){
      FrameUtils.setEmbedVisibility(currentFrame, false);
    }
  }
}