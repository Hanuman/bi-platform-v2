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
 * @created Aug 17, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.services.solution.ComponentBase;

public class InstanceEnd extends ComponentBase {

  private static final long serialVersionUID = -1193493564794051700L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(InstanceEnd.class);
  }

  @Override
  protected boolean validateAction() {
    // if we got this far then we should be ok...
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // nothing to do here
    return true;
  }

  @Override
  public void done() {
    // update the runtime data object and flush it to the runtime repository

    // set a flag indicating that this runtime data is complete
    // TODO hook up to the method in the runtime context when it is available

    // flush the object to the repository

    // audit this completion
    audit(MessageTypes.INSTANCE_END, getInstanceId(), "", 0); //$NON-NLS-1$

  }

  @Override
  protected boolean executeAction() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean init() {
    // nothing to do here
    return true;
  }

}
