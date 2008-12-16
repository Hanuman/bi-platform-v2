package org.pentaho.platform.plugin.services.pluginmgr;

import org.pentaho.platform.api.engine.IXulOverlay;
import org.pentaho.ui.xul.XulOverlay;

public class XulOverlayAdapter implements IXulOverlay {

  private XulOverlay xulOverlay;
  
  public XulOverlayAdapter(XulOverlay xulOverlay) {
    this.xulOverlay = xulOverlay;
  }
  
  public String getId() {
    return xulOverlay.getId();
  }

  public String getOverlayUri() {
    return xulOverlay.getOverlayUri();
  }

  public String getOverlayXml() {
    return xulOverlay.getSource();
  }

  public String getResourceBundleUri() {
    return xulOverlay.getResourceBundleUri();
  }
}
