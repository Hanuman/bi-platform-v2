/**
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created January 3rd, 2006
 * @author Michael D'Amour
 **/
package org.pentaho.platform.plugin.action.datatransforms;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.DataUtilities;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * 
 * Implements a PrintComponent class that will send a attached print file to a
 * specified printer.
 */
public class ResultSetExportComponent extends ComponentBase {
  /**
   * 
   */
  private static final long serialVersionUID = 3289900246113442203L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(ResultSetExportComponent.class);
  }

  @Override
  public boolean init() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    boolean hasResultSetParameter = isDefinedInput("result-set"); //$NON-NLS-1$ 
    if (!hasResultSetParameter) {
      error(Messages.getString("JFreeReport.ERROR_0022_DATA_INPUT_INVALID_OBJECT")); //$NON-NLS-1$
      return false;
    }
    if (getResultOutputName() == null) {
      error(Messages.getString("JFreeReport.ERROR_0022_DATA_INPUT_INVALID_OBJECT")); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean executeAction() {
    Object resultSetObject = getInputValue("result-set"); //$NON-NLS-1$
    if (resultSetObject instanceof IPentahoResultSet) {
      IPentahoResultSet resultset = (IPentahoResultSet) resultSetObject;
      if (getResultOutputName() != null) {
        setOutputValue(getResultOutputName(), DataUtilities.getXMLString(resultset));
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub
  }

  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ((outputs == null) || (outputs.size() == 0)) {
      error(Messages.getString("Template.ERROR_0002_OUTPUT_COUNT_WRONG")); //$NON-NLS-1$
      return null;
    }
    String outputName = null;
    try {
      outputName = getInputStringValue(StandardSettings.OUTPUT_NAME);
    } catch (Exception e) {
    }
    if (outputName == null) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
    }
    return outputName;
  }
}
