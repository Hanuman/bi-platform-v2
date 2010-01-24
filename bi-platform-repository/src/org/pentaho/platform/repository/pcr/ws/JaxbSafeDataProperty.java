package org.pentaho.platform.repository.pcr.ws;

import org.pentaho.platform.repository.pcr.data.node.DataNode.DataPropertyType;

public class JaxbSafeDataProperty {
  public DataPropertyType type;

  public Object value;

  public String name;

  @Override
  public String toString() {
    return "JaxbSafeDataProperty [name=" + name + ", type=" + type + ", value=" + value + "]";
  }
  
  
}
