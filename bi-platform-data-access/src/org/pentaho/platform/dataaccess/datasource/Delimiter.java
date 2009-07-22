package org.pentaho.platform.dataaccess.datasource;

public enum Delimiter {
  COMMA("Delimiter.USER_COMMA_DESC", ","), TAB("Delimiter.USER_TAB_DESC", "  "), SEMICOLON("Delimiter.USER_SEMI_COLON_DESC", ";"), SPACE("Delimiter.USER_SPACE_DESC", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

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
