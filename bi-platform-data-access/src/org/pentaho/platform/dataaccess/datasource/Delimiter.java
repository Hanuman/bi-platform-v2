package org.pentaho.platform.dataaccess.datasource;

public enum Delimiter {
  NONE("None", ""), COMMA("Comma",","), TAB("Tab"," "), SEMICOLON("Semicolon", ";"), SPACE("Space", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

  private String name;
  private String value;
  
  Delimiter(String name, String value) {
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
  
  public static Delimiter lookupValue(String delim) {
    for (Delimiter delimObj : Delimiter.values()) {
      if (delimObj.getValue().equals(delim)) {
        return delimObj;
      }
    }
    return null;
  }
}
