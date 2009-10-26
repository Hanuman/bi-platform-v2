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
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionDocumentManager;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class RefreshRepositoryCommand extends AbstractCommand {

  private static native void setupNativeHooks(RefreshRepositoryCommand cmd)
  /*-{
    $wnd.mantle_refreshRepository = function() {
      cmd.@org.pentaho.mantle.client.commands.RefreshRepositoryCommand::execute(Z)(false);
    }
  }-*/;

  public RefreshRepositoryCommand() {
    setupNativeHooks(this);
  }

  public void execute() {
    super.execute();
    SolutionDocumentManager.getInstance().beforeFetchSolutionDocument();
  }

  protected void performOperation(final boolean feedback) {
    // high level details:
    // ask the server to reload/resolve the solution repository against disk (if needed)
    // -this is only done if the user is the admin
    // if the user is not admin, we're still going to cause a refetch of the solution repo document
    // -because something might have been published or an admin might have reloaded with a different session
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("refreshRepositoryFailed"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }

      public void onSuccess(Void nothing) {
        SolutionDocumentManager.getInstance().fetchSolutionDocument(true);
      }
    };
    MantleServiceCache.getService().refreshRepository(callback);
  }

  protected void performOperation() {
    performOperation(true);
  }

}
