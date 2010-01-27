package org.pentaho.platform.repository.pcr.data.node;

import java.io.Serializable;

public class DataNodeRef {
  
  private Serializable id;
  
  public DataNodeRef(final Serializable id) {
    super();
    this.id = id;
  }

  public Serializable getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DataNodeRef other = (DataNodeRef) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}