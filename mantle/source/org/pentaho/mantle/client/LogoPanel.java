package org.pentaho.mantle.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class LogoPanel extends VerticalPanel {

  public LogoPanel(final String launchURL) {
    setStyleName("logoPanel-ContainerToolBar");
    setSpacing(0);
    setHeight("100%");
    setWidth("100%");

    VerticalPanel toolBarBackgroundPanel = new VerticalPanel();
    toolBarBackgroundPanel.setSpacing(0);
    toolBarBackgroundPanel.setStyleName("logoPanel-Container");
    toolBarBackgroundPanel.setWidth("100%");
    toolBarBackgroundPanel.setHeight("100%");

    Image logoImage = new Image();
    if (GWT.isScript()) {
      logoImage.setUrl("/pentaho/mantle/logo.png");
    } else {
      logoImage.setUrl("logo.png");
    }
    if (launchURL != null && !"".equals(launchURL)) {
      logoImage.setStyleName("launchImage");
      logoImage.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          Window.open(launchURL, "_blank", "");
        }
      });
    }
    toolBarBackgroundPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    toolBarBackgroundPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    toolBarBackgroundPanel.add(logoImage);

    add(toolBarBackgroundPanel);
  }

}
