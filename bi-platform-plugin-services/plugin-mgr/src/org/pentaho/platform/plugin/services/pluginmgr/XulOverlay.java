package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.Serializable;

import org.pentaho.platform.api.engine.IXulOverlay;

public class XulOverlay implements IXulOverlay, Serializable {

  public XulOverlay() {
  }

  /**
   * 
   */
  private static final long serialVersionUID = -2295937752856350528L;
  private String id;
  private String overlayUri;
  private String overlayXml;
  private String resourceBundleUri;
  
  public XulOverlay( String id, String overlayUri, String overlayXml, String resourceBundleUri ) {
    this.id = id;
    this.overlayUri = overlayUri;
    this.overlayXml = overlayXml;
    this.resourceBundleUri = resourceBundleUri;
  }
  
  public String getId() {
    return id;
  }

  public String getOverlayUri() {
    return overlayUri;
  }

  public String getOverlayXml() {
    return overlayXml;
  }

  public String getResourceBundleUri() {
    return resourceBundleUri;
  }

  public void onModuleLoad() {
    // no-up
  }

}
