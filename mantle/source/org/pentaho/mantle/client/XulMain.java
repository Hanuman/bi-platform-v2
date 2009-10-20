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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.toolbars.MainToolbarController;
import org.pentaho.mantle.client.toolbars.MainToolbarModel;
import org.pentaho.mantle.login.client.MantleLoginDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.tags.GwtToolbar;
import org.pentaho.ui.xul.gwt.tags.GwtToolbarbutton;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class XulMain extends SimplePanel implements IXulLoaderCallback, SolutionBrowserListener {

  private Map<String, MantleXulOverlay> overlayMap = new HashMap<String, MantleXulOverlay>();

  private MainToolbarModel model;

  private MainToolbarController controller;

  private GwtXulDomContainer container;

  private static XulMain _instance = null;

  private SolutionBrowserPerspective solutionBrowser;

  public static synchronized XulMain instance(final SolutionBrowserPerspective solutionBrowser) {
    if (null == _instance) {
      _instance = new XulMain(solutionBrowser);
    }
    return _instance;
  }

  public static XulMain getInstance() {
    return _instance;
  }

  protected XulMain(final SolutionBrowserPerspective solutionBrowser) {
    this.solutionBrowser = solutionBrowser;
    // instantiate our Model and Controller
    controller = new MainToolbarController(solutionBrowser, new MainToolbarModel(solutionBrowser, this));

    // Invoke the async loading of the XUL DOM.
    AsyncXulLoader.loadXulFromUrl("xul/main_toolbar.xul", "messages/messages", this);
    solutionBrowser.addSolutionBrowserListener(this);
  }

  /**
   * Callback method for the MantleXulLoader. This is called when the Xul file has been processed.
   * 
   * @param runner
   *          GwtXulRunner instance ready for event handlers and initializing.
   */
  public void xulLoaded(GwtXulRunner runner) {

    // handlers need to be wrapped generically in GWT, create one and pass it our reference.
    EventHandlerWrapper wrapper = GWT.create(MainToolbarController.class);
    wrapper.setHandler(controller);

    // Add handler to container
    container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);
    container.addEventHandler(wrapper);

    try {
      runner.initialize();
    } catch (XulException e) {
      Window.alert("Error initializing XUL runner: " + e.getMessage()); //$NON-NLS-1$
      e.printStackTrace();
      return;
    }

    // TODO: remove controller reference from model when Bindings in place
    model = new MainToolbarModel(solutionBrowser, this);
    controller.setModel(model);
    controller.setSolutionBrowser(solutionBrowser);

    // Get the toolbar from the XUL doc
    Toolbar bar = (Toolbar) container.getDocumentRoot().getElementById("mainToolbar").getManagedObject(); //$NON-NLS-1$
    bar.setStylePrimaryName("mainToolbar"); //$NON-NLS-1$
    this.add(bar);

    // unfortunately hosted mode won't resolve the image with 'mantle/' in it
    cleanImageUrlsForHostedMode();

    fetchOverlays();

    // Fix for IE 6 transparent PNGs
    ElementUtils.convertPNGs();

  }

  private void fetchOverlays() {
    AsyncCallback<List<MantleXulOverlay>> callback = new AsyncCallback<List<MantleXulOverlay>>() {

      public void onFailure(Throwable caught) {
        doLogin();
      }

      public void onSuccess(List<MantleXulOverlay> overlays) {

        XulMain.getInstance().loadOverlays(overlays);
      }
    };
    MantleServiceCache.getService().getOverlays(callback);
  }

  private void doLogin() {
    MantleLoginDialog.performLogin(new AsyncCallback<Object>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("invalidLogin"), false, false, true) {

        }; //$NON-NLS-1$ //$NON-NLS-2$

        dialogBox.setCallback(new IDialogCallback() {
          public void cancelPressed() {
            // do nothing
          }

          public void okPressed() {
            doLogin();
          }
        });

        dialogBox.center();
      }

      public void onSuccess(Object result) {
        fetchOverlays();
      }

    });
  }

  private void cleanImageUrlsForHostedMode() {
    if (!GWT.isScript()) {

      GwtToolbar toolbar = (GwtToolbar) container.getDocumentRoot().getElementById("mainToolbar"); //$NON-NLS-1$
      for (XulComponent c : toolbar.getChildNodes()) {
        if (c instanceof XulToolbarbutton) {
          GwtToolbarbutton btn = (GwtToolbarbutton) c;

          String curSrc = btn.getImage();
          btn.setImage(curSrc.replace("mantle/", "")); //$NON-NLS-1$ //$NON-NLS-2$

          curSrc = btn.getDisabledImage();
          if (curSrc != null) {
            btn.setDisabledImage(curSrc.replace("mantle/", "")); //$NON-NLS-1$ //$NON-NLS-2$
          }
          curSrc = btn.getDownimage();
          if (curSrc != null) {
            btn.setDownimage(curSrc.replace("mantle/", "")); //$NON-NLS-1$ //$NON-NLS-2$
          }
          curSrc = btn.getDownimagedisabled();
          if (curSrc != null) {
            btn.setDownimagedisabled(curSrc.replace("mantle/", "")); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    }
  }

  public void overlayLoaded() {
    cleanImageUrlsForHostedMode();
    // Fix for IE 6 transparent PNGs
    ElementUtils.convertPNGs();
  }

  public void loadOverlays(List<MantleXulOverlay> overlays) {
    for (MantleXulOverlay overlay : overlays) {
      overlayMap.put(overlay.getId(), overlay);
      if (overlay.getId().startsWith("startup")) {
        AsyncXulLoader.loadOverlayFromSource(overlay.getSource(), overlay.getResourceBundleUri(), container, this);
      }
    }
  }

  public void applyOverlays(Set<String> overlayIds) {
    if (overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        applyOverlay(overlayId);
      }
    }
  }

  public void applyOverlay(String id) {
    if (overlayMap != null && !overlayMap.isEmpty()) {
      if (overlayMap.containsKey(id)) {
        MantleXulOverlay overlay = overlayMap.get(id);
        AsyncXulLoader.loadOverlayFromSource(overlay.getOverlayXml(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void removeOverlays(Set<String> overlayIds) {
    if (overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        removeOverlay(overlayId);
      }
    }
  }

  public void removeOverlay(String id) {
    if (overlayMap != null && !overlayMap.isEmpty()) {
      if (overlayMap.containsKey(id)) {
        MantleXulOverlay overlay = overlayMap.get(id);
        AsyncXulLoader.removeOverlayFromSource(overlay.getOverlayXml(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void overlayRemoved() {
    // TODO Auto-generated method stub

  }

  public void solutionBrowserEvent(EventType type, Widget panel, FileItem selectedFileItem) {
    if (panel instanceof IFrameTabPanel) {
      if (SolutionBrowserListener.EventType.OPEN.equals(type) || SolutionBrowserListener.EventType.SELECT.equals(type)) {
        if (panel != null) {
          applyOverlays(((IFrameTabPanel) panel).getOverlayIds());
        }
      } else if (SolutionBrowserListener.EventType.CLOSE.equals(type) || SolutionBrowserListener.EventType.DESELECT.equals(type)) {
        if (panel != null) {
          removeOverlays(((IFrameTabPanel) panel).getOverlayIds());
        }
      }
    }
  }

}
