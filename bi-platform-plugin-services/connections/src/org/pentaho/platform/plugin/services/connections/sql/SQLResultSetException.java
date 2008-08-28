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
 * Created July 31, 2006
 * @author mdamour
 */
package org.pentaho.platform.plugin.services.connections.sql;

import org.pentaho.platform.api.util.PentahoChainedException;

/**
 * This exception just signals that an exception occurred during DB interaction
 */
public class SQLResultSetException extends PentahoChainedException {

  private static final long serialVersionUID = 1063956390289262889L;

  /**
   * lame ass ctor that really shouldn't be used.
   * 
   */
  public SQLResultSetException() {
  }

  public SQLResultSetException(final String message, final Throwable reas) {
    super(message, reas);
  }

  public SQLResultSetException(final String message) {
    super(message);
  }

  public SQLResultSetException(final Throwable reas) {
    super(reas);
  }
}
