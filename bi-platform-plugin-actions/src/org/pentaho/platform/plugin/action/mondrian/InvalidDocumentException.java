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
 * @created July 15, 2007 
 * @author Gretchen Moran
 */

package org.pentaho.platform.plugin.action.mondrian;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class InvalidDocumentException extends PentahoCheckedChainedException {

  private static final long serialVersionUID = -3318198454699925064L;

  public InvalidDocumentException(final String message) {
    super(message);
  }

  public InvalidDocumentException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public InvalidDocumentException(final Throwable reas) {
    super(reas);
  }

}
