/*
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * @created Oct 12, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.xmla;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XMLADataComponent extends XMLABaseComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -2067865149724338823L;

  @Override
  public boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public String getResultOutputName() {
    return null;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(XMLADataComponent.class);
  }

  @Override
  public boolean init() {
    return true;
  }
}
