/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.pentaho.mantle.client.messages.Messages;

public class ReportParameter implements Serializable {
  // parameter types
  public static final int STRING = 0;
  public static final int NUMBER = 1;
  public static final int DATE = 2;

  // parameter prompt types
  public static final int SELECTION_TYPE_LIST = 0;
  public static final int SELECTION_TYPE_SELECT = 1;
  public static final int SELECTION_TYPE_TEXTBOX = 2;
  public static final int SELECTION_TYPE_SLIDER = 3;

  private HashMap<Number, String> numberParameterChoices = new HashMap<Number, String>();
  private HashMap<String, String> stringParameterChoices = new HashMap<String, String>();
  private HashMap<Date, String> dateParameterChoices = new HashMap<Date, String>();
  private Date defaultDateValue = null;
  private Number defaultNumberValue = null;
  private String defaultStringValue = null;
  private String name = null;
  private boolean isMultiSelect = false;
  private int promptType = SELECTION_TYPE_LIST;

  // actual user inputted values
  private List<Date> dateValues = new ArrayList<Date>();
  private List<Number> numberValues = new ArrayList<Number>();
  private List<String> stringValues = new ArrayList<String>();

  // parameter classType
  private int parameterType = STRING;

  public HashMap getChoices() {
    if (numberParameterChoices.size() > 0) {
      return numberParameterChoices;
    } else if (stringParameterChoices.size() > 0) {
      return stringParameterChoices;
    } else if (dateParameterChoices.size() > 0) {
      return dateParameterChoices;
    }
    return null;
  }

  public Object getDefaultValue() {
    if (defaultDateValue != null) {
      return defaultDateValue;
    } else if (defaultStringValue != null) {
      return defaultStringValue;
    } else if (defaultNumberValue != null) {
      return defaultNumberValue;
    }
    return null;
  }

  // for single selection
  public Object getValue() {
    if (dateValues.size() > 0) {
      return dateValues.get(0);
    } else if (stringValues.size() > 0) {
      return stringValues.get(0);
    } else if (numberValues.size() > 0) {
      return numberValues.get(0);
    }
    // no values
    return null;
  }

  // for multiselection
  public List getValues() {
    if (dateValues.size() > 0) {
      return dateValues;
    } else if (stringValues.size() > 0) {
      return stringValues;
    } else if (numberValues.size() > 0) {
      return numberValues;
    }
    // no values, empty
    return new ArrayList();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

    for (Object choiceKey : getChoices().keySet()) {
      sb.append(Messages.getString("choiceColon") + " " + choiceKey + " = " + getChoices().get(choiceKey) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    for (Object value : getValues()) {
      sb.append(Messages.getString("selectionColon") + " " + value + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (getValues() == null) {
      sb.append(Messages.getString("selectionColon") + " " + getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return sb.toString();
  }

  public Date getDefaultDateValue() {
    return defaultDateValue;
  }

  public void setDefaultDateValue(Date defaultDateValue) {
    this.defaultDateValue = defaultDateValue;
  }

  public Number getDefaultNumberValue() {
    return defaultNumberValue;
  }

  public void setDefaultNumberValue(Number defaultNumberValue) {
    this.defaultNumberValue = defaultNumberValue;
  }

  public String getDefaultStringValue() {
    return defaultStringValue;
  }

  public void setDefaultStringValue(String defaultStringValue) {
    this.defaultStringValue = defaultStringValue;
  }

  public HashMap<Number, String> getNumberParameterChoices() {
    return numberParameterChoices;
  }

  public void setNumberParameterChoices(HashMap<Number, String> numberParameterChoices) {
    this.numberParameterChoices = numberParameterChoices;
  }

  public HashMap<String, String> getStringParameterChoices() {
    return stringParameterChoices;
  }

  public void setStringParameterChoices(HashMap<String, String> stringParameterChoices) {
    this.stringParameterChoices = stringParameterChoices;
  }

  public HashMap<Date, String> getDateParameterChoices() {
    return dateParameterChoices;
  }

  public void setDateParameterChoices(HashMap<Date, String> dateParameterChoices) {
    this.dateParameterChoices = dateParameterChoices;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isMultiSelect() {
    return isMultiSelect;
  }

  public void setMultiSelect(boolean isMultiSelect) {
    this.isMultiSelect = isMultiSelect;
  }

  public List<Date> getDateValues() {
    return dateValues;
  }

  public void setDateValues(List<Date> dateValues) {
    this.dateValues = dateValues;
  }

  public List<Number> getNumberValues() {
    return numberValues;
  }

  public void setNumberValues(List<Number> numberValues) {
    this.numberValues = numberValues;
  }

  public List<String> getStringValues() {
    return stringValues;
  }

  public void setStringValues(List<String> stringValues) {
    this.stringValues = stringValues;
  }

  public int getParameterType() {
    return parameterType;
  }

  public void setParameterType(int parameterType) {
    this.parameterType = parameterType;
  }

  public void clearSelections() {
    dateValues.clear();
    numberValues.clear();
    stringValues.clear();
  }

  public int getPromptType() {
    return promptType;
  }

  public void setPromptType(int promptType) {
    this.promptType = promptType;
  }
}
