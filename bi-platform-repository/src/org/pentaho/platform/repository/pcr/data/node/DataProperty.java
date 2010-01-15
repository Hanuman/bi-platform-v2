package org.pentaho.platform.repository.pcr.data.node;

import java.util.Date;

import org.pentaho.platform.repository.pcr.data.node.DataNode.DataPropertyType;

public class DataProperty {

  private DataPropertyType type;

  private Object value;

  private String name;

  public DataProperty(final String name, final Object value, final DataPropertyType type) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public String getString() {
    return String.valueOf(value);
  }

  public boolean getBoolean() {
    return Boolean.valueOf(getString());
  }

  public long getLong() {
    return Long.valueOf(getString());
  }

  public double getDouble() {
    return Double.valueOf(getString());
  }

  public Date getDate() {
    if (!(value instanceof Date)) {
      throw new IllegalArgumentException();
    }
    return new Date(((Date) value).getTime());
  }

  public DataPropertyType getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    DataProperty other = (DataProperty) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
