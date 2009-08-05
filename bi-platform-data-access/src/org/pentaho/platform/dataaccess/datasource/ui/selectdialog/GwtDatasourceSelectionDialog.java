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
 *
 * Created June 2, 2009
 * @author mlowery
 */
package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditor;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.core.client.GWT;

public class GwtDatasourceSelectionDialog implements IXulLoaderCallback, DialogController<LogicalModelSummary> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private DatasourceSelectionDialogController datasourceSelectionDialogController;

  private GwtDatasourceEditor gwtDatasourceEditor;

  private IXulAsyncDatasourceService datasourceService;

  private AsyncConstructorListener constructorListener;

  private boolean initialized;

  // ~ Constructors ====================================================================================================

  public GwtDatasourceSelectionDialog(final IXulAsyncDatasourceService datasourceService,
      final GwtDatasourceEditor gwtDatasourceEditor, final AsyncConstructorListener constructorListener) {
    this.gwtDatasourceEditor = gwtDatasourceEditor;
    this.datasourceService = datasourceService;
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl("datasourceSelectionDialog.xul", "datasourceSelectionDialog", this); //$NON-NLS-1$//$NON-NLS-2$
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ~ Methods =========================================================================================================

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
  }

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayRemoved() {
  }

  public void xulLoaded(final GwtXulRunner runner) {
    try {
      GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);

      BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());

      // begin DatasourceSelectionDialogController setup
      datasourceSelectionDialogController = new DatasourceSelectionDialogController();
      datasourceSelectionDialogController.setBindingFactory(bf);
      datasourceSelectionDialogController.setDatasourceService(datasourceService);
      EventHandlerWrapper editPanelControllerWrapper = GWT.create(DatasourceSelectionDialogController.class);
      editPanelControllerWrapper.setHandler(datasourceSelectionDialogController);
      container.addEventHandler(editPanelControllerWrapper);
      // end DatasourceSelectionDialogController setup

      datasourceSelectionDialogController.setDatasourceDialogController(gwtDatasourceEditor);

      runner.initialize();

      runner.start();

      initialized = true;

      if (constructorListener != null) {
        constructorListener.asyncConstructorDone();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("You must wait until the constructor listener is notified."); //$NON-NLS-1$
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener) {
    checkInitialized();
    datasourceSelectionDialogController.addDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    checkInitialized();
    datasourceSelectionDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<LogicalModelSummary> listener) {
    checkInitialized();
    datasourceSelectionDialogController.removeDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    checkInitialized();
    datasourceSelectionDialogController.showDialog();
  }

}
