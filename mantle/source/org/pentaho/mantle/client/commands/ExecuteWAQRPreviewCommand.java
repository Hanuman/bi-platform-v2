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
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;

public class ExecuteWAQRPreviewCommand extends AbstractCommand {
  private String xml;
  private String url;

  public ExecuteWAQRPreviewCommand() {
    setupNativeHooks(this);
  }

  public ExecuteWAQRPreviewCommand(String url, String xml) {
    this();
    this.xml = xml;
    this.url = url;
  }

  private static native void setupNativeHooks(ExecuteWAQRPreviewCommand cmd)
  /*-{
    $wnd.mantle_waqr_preview = function(url, xml) {
      cmd.@org.pentaho.mantle.client.commands.ExecuteWAQRPreviewCommand::handleWAQRPreview(Ljava/lang/String;Ljava/lang/String;)(url, xml);
    }
  }-*/;

  protected void performOperation() {
    MantleTabPanel contentTabPanel = SolutionBrowserPerspective.getInstance().getContentTabPanel();
    SolutionBrowserPerspective.getInstance().showNewURLTab(Messages.getString("preview"), Messages.getString("adhocPreview"), "about:blank"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    NamedFrame namedFrame = ((IFrameTabPanel) contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab())).getFrame();
    final FormPanel form = new FormPanel(namedFrame);
    RootPanel.get().add(form);
    form.setMethod(FormPanel.METHOD_POST);
    form.setAction(url);
    form.add(new Hidden("reportXml", xml)); //$NON-NLS-1$
    form.submit();
    ((IFrameTabPanel) contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab())).setForm(form);
  }

  protected void performOperation(final boolean feedback) {
    performOperation();
  }

  /**
   * This method is called via JSNI
   */
  private void handleWAQRPreview(String url, String xml) {
    ExecuteWAQRPreviewCommand command = new ExecuteWAQRPreviewCommand(url, xml);
    command.execute();
  }

}
