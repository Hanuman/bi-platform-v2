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
 */
package org.pentaho.platform.web.http;

import org.pentaho.platform.api.util.PentahoChainedException;

public class UIException extends PentahoChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public UIException() {
    super();
  }

  /**
   * @param message
   */
  public UIException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param reas
   */
  public UIException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * @param reas
   */
  public UIException(final Throwable reas) {
    super(reas);
  }

}
