package org.pentaho.test.plugin.services.webservices;

import org.pentaho.platform.api.engine.IMimeTypeListener;

public class MimeTypeListener implements IMimeTypeListener {

    public String mimeType = null;
    
    public String name = null;
  
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setName(String name) {
    this.name = name;
  }

}
