package org.pentaho.test.platform.engine.core;

import java.util.List;
import java.util.Map;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;

@SuppressWarnings({"all"})
public class TestActionParameter implements IActionParameter {

  public Object value;
  
  public void dispose() {
    // TODO Auto-generated method stub

  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getPromptStatus() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getSelectionDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSelectionNameForValue(String value) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map getSelectionNameMap() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getSelectionValues() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getStringValue() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getType() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getValue() {
    return value;
  }

  public List getValueAsList() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoResultSet getValueAsResultSet() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasDefaultValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasSelections() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isDefaultValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNull() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setParamSelections(List selValues, Map selNames, String displayname) {
    // TODO Auto-generated method stub

  }

  public boolean setPromptStatus(int status) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setValue(Object value) {
    this.value = value;
  }

}
