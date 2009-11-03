/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;

public class WAQRCommand extends AbstractCommand {

  public WAQRCommand() {
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    String waqrURL = "adhoc/waqr.html"; //$NON-NLS-1$
    if (!GWT.isScript()) {
      waqrURL = "http://localhost:8080/pentaho/adhoc/waqr.html?userid=joe&password=password"; //$NON-NLS-1$
    }
    SolutionBrowserPerspective.getInstance().getContentTabPanel().showNewURLTab(Messages.getString("untitled"), Messages.getString("newAdhocReport"), waqrURL, false); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
