package org.pentaho.mantle.client.dialogs;

import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WaitPopup extends SimplePanel{

  static private WaitPopup instance = new WaitPopup();
  private static FocusPanel pageBackground = null;
  private static int clickCount = 0;
  
  public WaitPopup(){
    setStyleName("waitPopup");
    this.setVisible(false);
    VerticalPanel vp = new VerticalPanel();
    Label lbl = new Label(Messages.getInstance().pleaseWait());
    lbl.setStyleName("waitPopup_title");
    vp.add(lbl);
    lbl = new Label(Messages.getInstance().waitMessage());
    lbl.setStyleName("waitPopup_msg");
    vp.add(lbl);
    vp.setStyleName("waitPopup_table");
    this.add(vp);
    
    if (pageBackground == null) {
      pageBackground = new FocusPanel();
      pageBackground.setStyleName("modalDialogPageBackground"); //$NON-NLS-1$
      pageBackground.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          clickCount++;
          if (clickCount > 2) {
            clickCount = 0;
            pageBackground.setVisible(false);
          }
        }
      });
      RootPanel.get().add(pageBackground, 0, 0);
    }
  }
  
  public static WaitPopup getInstance(){
    return instance;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    
  }
  
  
}
