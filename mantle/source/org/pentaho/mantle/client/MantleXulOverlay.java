package org.pentaho.mantle.client;

import java.io.Serializable;

public class MantleXulOverlay implements Serializable {

  private String id;

  private String overlayUri;

  private String source;

  private String resourceBundleUri;

  public MantleXulOverlay() {
  }
  
  public MantleXulOverlay(String id, String overlayUri, String source, String resourceBundleUri) {
    this.id = id;
    this.overlayUri = overlayUri;
    this.source = source;
    this.resourceBundleUri = resourceBundleUri;
  }

  public String getId() {
    return id;
  }

  public String getOverlayUri() {
    return overlayUri;
  }

  public String getOverlayXml() {
    return getSource();
  }

  public String getResourceBundleUri() {
    return resourceBundleUri;
  }

  public String getSource() {
    return source;
  }
}
