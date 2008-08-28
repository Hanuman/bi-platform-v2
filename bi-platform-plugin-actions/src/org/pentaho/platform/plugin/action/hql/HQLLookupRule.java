/*
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
 * Created Dec 27, 2006
 * @author mdamour
 */

package org.pentaho.platform.plugin.action.hql;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

public class HQLLookupRule extends HQLBaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4633628885885427927L;

  /**
   * 
   */
  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ((outputs == null) || (outputs.size() == 0)) {
      // if( (outputs == null) || (outputs.size() == 0 ) ||
      // (outputs.size() > 1 ) ) {
      error(Messages.getString("Template.ERROR_0002_OUTPUT_COUNT_WRONG")); //$NON-NLS-1$
      return null;
    }

    // Did we override the output name? // TODO Deprecation Warning
    String outputName = getInputStringValue(StandardSettings.OUTPUT_NAME);
    if ((outputName == null) && outputs.contains("query-result")) { //$NON-NLS-1$ // Get the query-result node - This is the preferred method to use 
      outputName = "query-result"; //$NON-NLS-1$
    }

    if (outputName == null) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
      // TODO Deprecation Warning
    }
    return outputName;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(HQLLookupRule.class);
  }

  @Override
  public boolean init() {
    return true;
  }
}
