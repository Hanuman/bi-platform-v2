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

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshRepositoryCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public RefreshRepositoryCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute(final boolean feedback) {
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        WaitPopup.getInstance().setVisible(false);
        // the success message is nothing but annoying after the waitpopup finishes
        // MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("refreshRepositoryFailed"), false, false, true);
        // //$NON-NLS-1$ //$NON-NLS-2$
        // dialogBox.center();
      }

      public void onSuccess(Void nothing) {
        WaitPopup.getInstance().setVisible(false);
        navigatorPerspective.refreshPerspective(feedback);
      }
    };
    WaitPopup.getInstance().setVisible(true);
    MantleServiceCache.getService().refreshRepository(callback);
  }

  public void execute() {
    execute(true);
  }

}
