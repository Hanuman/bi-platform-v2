/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
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
 * Created Apr 11, 2007 
 * @author dkincade
 */

package org.pentaho.platform.plugin.action.xml.webservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.action.xml.xquery.XQueryLookupRule;

/**
 * Performs the action of processing a webservice call in an action
 * by doing the same functionality as the XQuery action (but not
 * trying to retrieve the column types).
 * <br/>
 * The reason for the modification stems from a problem in the encoding of
 * the URL. Retrieving the columns types expects an XML decoded URL.
 * The XQuery processing expects an XML encoded URL. 
 * <br/>
 * @author dkincade
 */
public class WebServiceLookupRule extends XQueryLookupRule {

  private static final long serialVersionUID = -3785939302984708094L;

  /**
   * Returns the logger for this class
   */
  @Override
  public Log getLogger() {
    return LogFactory.getLog(this.getClass());
  }

  /**
   * For web services, we don't need to retrieve the columns types
   * during processing
   */
  @Override
  protected boolean retrieveColumnTypes() {
    return false;
  }
}
