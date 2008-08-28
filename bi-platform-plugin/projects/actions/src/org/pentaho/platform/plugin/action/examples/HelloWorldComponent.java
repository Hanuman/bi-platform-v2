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
 * @created Jun 23, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.examples;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.HelloWorldAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HelloWorldComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = 9050456842938084174L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(HelloWorldComponent.class);
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    boolean result = true;
    if (!(getActionDefinition() instanceof HelloWorldAction)) {
      error(Messages.getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() {
    HelloWorldAction helloWorldAction = (HelloWorldAction) getActionDefinition();
    boolean result = true;

    // return the quote as the result of this component
    String msg = Messages.getString("HelloWorld.USER_HELLO_WORLD_TEXT", helloWorldAction.getQuote().getStringValue("")); //$NON-NLS-1$

    OutputStream outputStream = getDefaultOutputStream("text/html"); //$NON-NLS-1$
    if (outputStream != null) {
      try {
        outputStream.write(msg.getBytes(LocaleHelper.getSystemEncoding()));
      } catch (Exception e) {
        error(Messages.getErrorString("HelloWorld.ERROR_0001_COULDNOTWRITE"), e); //$NON-NLS-1$
        result = false;
      }
    }

    info(msg);
    return result;
  }

  @Override
  public boolean init() {

    // nothing to do here really
    return true;
  }

}
