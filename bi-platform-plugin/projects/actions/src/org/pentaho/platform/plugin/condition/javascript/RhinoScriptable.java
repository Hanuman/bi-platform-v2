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
 * @created Mar 31, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.plugin.condition.javascript;

import org.mozilla.javascript.ScriptableObject;

public class RhinoScriptable extends ScriptableObject {

  /**
   * 
   */
  private static final long serialVersionUID = 6876272459770131778L;

  /*
   * (non-Javadoc)
   * 
   * @see org.mozilla.javascript.Scriptable#getClassName()
   */
  @Override
  public String getClassName() {
    // TODO Auto-generated method stub
    return "org.pentaho.platform.plugin.javascript.RhinoScriptable"; //$NON-NLS-1$
  }

}
