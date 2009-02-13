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
 * @created Feb 2, 2009 
 * @author wseyler
 */

package org.pentaho.platform.api.engine;
  /**
   * A "something" that refers to a session.
   *
   * This is mainly used to ensure that sessions don't leak.
   * 
   * @author <a href="mailto:andreas.kohn@fredhopper.com">Andreas Kohn</a>
   * @see BISERVER-2639
   */
  /* TODO: should provide a getSession(), or ideally a 'clearSessionIfCurrent(IPentahoSession s)' to facilitate
   * easy cleaning.
   */
public interface ISessionContainer {
  /**
   * Set the session for this session container.
   * 
   * @param sess
   *            The IPentahoSession to set
   */
  public void setSession(IPentahoSession sess);
}
