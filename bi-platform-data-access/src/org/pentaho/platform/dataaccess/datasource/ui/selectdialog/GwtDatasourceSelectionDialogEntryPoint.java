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
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

public class GwtDatasourceSelectionDialogEntryPoint implements EntryPoint {

  private GwtDatasourceSelectionDialog selectDialog;

  private GwtDatasourceEditor editor;

  private IXulAsyncDatasourceService datasourceService;

  private IXulAsyncConnectionService connectionService;

  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    editor = new GwtDatasourceEditor(datasourceService, connectionService, null);
    selectDialog = new GwtDatasourceSelectionDialog(datasourceService, editor, null);
    setupNativeHooks(this);
  }

  public native void setupNativeHooks(final GwtDatasourceSelectionDialogEntryPoint d) /*-{
    $wnd.showDatasourceSelectionDialog = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
  }-*/;

  private void show(final JavaScriptObject callback) {
    final DialogListener<LogicalModelSummary> listener = new DialogListener<LogicalModelSummary>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final LogicalModelSummary logicalModelSummary) {
        notifyCallbackSuccess(callback, logicalModelSummary.getDomainId(), logicalModelSummary.getModelId());
      }
    };
    selectDialog.addDialogListener(listener);
    selectDialog.showDialog();
  }
  
  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId, String modelId) /*-{
    callback.onFinish(domainId, modelId);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback) /*-{
    callback.onCancel();
  }-*/;
}
