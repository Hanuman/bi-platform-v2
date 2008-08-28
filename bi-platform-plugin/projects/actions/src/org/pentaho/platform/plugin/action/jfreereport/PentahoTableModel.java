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
 * @created Jul 08, 2006
 * @author Thomas Morgner
 */
package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.commons.connection.IPentahoResultSet;

/**
 * Creation-Date: 08.07.2006, 13:19:45
 * 
 * @author Thomas Morgner
 * @deprecated This is an empty stub in case we have to maintain backward
 *             compatiblity.
 */
@Deprecated
public class PentahoTableModel extends org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel {
  private static final long serialVersionUID = 3946748761053175483L;

  public PentahoTableModel(final IPentahoResultSet rs) {
    super(rs);
  }
}
