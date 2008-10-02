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

  private String launchURL;

  public LogoPanel(String launchURL) {
    this.launchURL = launchURL;
    setStyleName("logoPanel-ContainerToolBar"); //$NON-NLS-1$
    setSpacing(0);
    setHeight("100%"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$

    VerticalPanel toolBarBackgroundPanel = new VerticalPanel();
    toolBarBackgroundPanel.setSpacing(0);
    toolBarBackgroundPanel.setStyleName("logoPanel-Container"); //$NON-NLS-1$
    toolBarBackgroundPanel.setWidth("100%"); //$NON-NLS-1$
    toolBarBackgroundPanel.setHeight("100%"); //$NON-NLS-1$

    Image logoImage = new Image();
    if (GWT.isScript()) {
      logoImage.setUrl("mantle/logo.png"); //$NON-NLS-1$
    } else {
      logoImage.setUrl("logo.png"); //$NON-NLS-1$
    }
    if (launchURL != null && !"".equals(launchURL)) { //$NON-NLS-1$
      logoImage.setStyleName("launchImage"); //$NON-NLS-1$
      logoImage.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          Window.open(getLaunchURL(), "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
      });
    }
    toolBarBackgroundPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    toolBarBackgroundPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    toolBarBackgroundPanel.add(logoImage);

    add(toolBarBackgroundPanel);
  }

  public String getLaunchURL() {
    return launchURL;
  }

  public void setLaunchURL(String launchURL) {
    this.launchURL = launchURL;
  }

}
