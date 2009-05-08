package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.DatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDebugImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDebugImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swing.SwingXulLoader;
import org.pentaho.ui.xul.swing.SwingXulRunner;

public class SwingDatasourceEditor {

  private static Log log = LogFactory.getLog(SwingDatasourceEditor.class);
  
  public SwingDatasourceEditor(){
    try{
      XulDomContainer container = new SwingXulLoader().loadXul("org/pentaho/platform/dataaccess/datasource/wizard/public/connectionFrame.xul");
    
      final XulRunner runner = new SwingXulRunner();
      runner.addContainer(container);
      
      
      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      
    
      final DatasourceController datasourceController = new DatasourceController();
      datasourceController.setBindingFactory(bf);
      container.addEventHandler(datasourceController);
      
      final ConnectionController connectionController = new ConnectionController();
      connectionController.setBindingFactory(bf);
      container.addEventHandler(connectionController);
      
      ConnectionService service = new ConnectionServiceDebugImpl();
      connectionController.setService(service);

      DatasourceService datasourceService = new DatasourceServiceDebugImpl();
      datasourceController.setService(datasourceService);
      try {
      service.getConnections(new XulServiceCallback<List<IConnection>>(){

        public void error(String message, Throwable error) {
          System.out.println(error.getLocalizedMessage());
        }

        public void success(List<IConnection> connections) {
          DatasourceModel datasourceModel = new DatasourceModel();
          ConnectionModel connectionModel = new ConnectionModel();
          datasourceModel.setConnections(connections);
          datasourceController.setDatasourceModel(datasourceModel);
          datasourceController.setConnectionModel(connectionModel);
          connectionController.setDatasourceModel(datasourceModel);
          connectionController.setConnectionModel(connectionModel);

          try{
            runner.initialize();
            runner.start();
          } catch(XulException e){
            log.error("error starting Xul application", e);
          }
        }
        
      });
      } catch(ConnectionServiceException cse) {
        log.error("error loading Xul application", cse);
      }
      
      
    } catch(XulException e){
      log.error("error loading Xul application", e);
    }
  }
  
  public static void main(String[] args){
    new SwingDatasourceEditor();
  }
  
}
