package org.pentaho.platform.dataaccess.datasource;

public enum Enclosure {
  SINGLEQUOTE("Single Quote", "'"), DOUBLEQUOTE("Double Quote", "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  private String name;
  private String value;
  Enclosure(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }
  public String getValue() {
    return value;
  }
  
  public static Enclosure lookupValue(String encl) {
    for (Enclosure enclObj : Enclosure.values()) {
      if (enclObj.getValue().equals(encl)) {
        return enclObj;
      }
    }
    return null;
  }
}
