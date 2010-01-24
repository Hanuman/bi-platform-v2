package org.pentaho.platform.repository.pcr.ws;

import java.util.Arrays;

public class JaxbSafeDataNode {
  public String id;
  public String name;
  public JaxbSafeDataNode[] childNodes = new JaxbSafeDataNode[0];
  public JaxbSafeDataProperty[] childProperties = new JaxbSafeDataProperty[0];
  
  @Override
  public String toString() {
    return "JaxbSafeDataNode [id=" + id + ", name=" + name + ", childNodes=" + Arrays.toString(childNodes)
        + ", childProperties=" + Arrays.toString(childProperties) + "]";
  }
  
  
}
