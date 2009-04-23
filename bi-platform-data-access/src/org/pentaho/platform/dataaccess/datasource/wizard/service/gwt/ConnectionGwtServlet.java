package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pentaho.commons.metadata.mqleditor.ColumnType;
import org.pentaho.commons.metadata.mqleditor.CombinationType;
import org.pentaho.commons.metadata.mqleditor.MqlBusinessTable;
import org.pentaho.commons.metadata.mqleditor.MqlCategory;
import org.pentaho.commons.metadata.mqleditor.MqlColumn;
import org.pentaho.commons.metadata.mqleditor.MqlCondition;
import org.pentaho.commons.metadata.mqleditor.MqlDomain;
import org.pentaho.commons.metadata.mqleditor.MqlModel;
import org.pentaho.commons.metadata.mqleditor.MqlOrder;
import org.pentaho.commons.metadata.mqleditor.MqlQuery;
import org.pentaho.commons.metadata.mqleditor.Operator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.DatabaseColumnType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.beans.Datasource;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.session.PentahoHttpSession;
import org.pentaho.pms.schema.v3.envelope.Envelope;
import org.pentaho.pms.schema.v3.model.Attribute;
import org.pentaho.pms.schema.v3.model.Column;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class ConnectionGwtServlet extends RemoteServiceServlet implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;
  IDatasourceMgmtService datasourceMgmtSvc;
 
  public ConnectionGwtServlet() {

  }

  private ConnectionServiceDelegate getService(){
    if(SERVICE == null){
      try {
        IPentahoSession session = PentahoHttpSessionHelper.getPentahoSession(this.getThreadLocalRequest());
        datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session); //$NON-NLS-1$
        SERVICE = new ConnectionServiceDelegate(datasourceMgmtSvc);
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  
  public List<IConnection> getConnections() {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) {
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection) throws ConnectionServiceException {
    return getService().testConnection(connection);    
  }
  
  @Override
  protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest arg0, String arg1, String arg2) {
      return new SerializationPolicy(){

        List<Class<?>> classes = new ArrayList<Class<?>>();
        {
          classes.add(Exception.class);
          classes.add(Integer.class);
          classes.add(Number.class);
          classes.add(Boolean.class);
          classes.add(RuntimeException.class);
          classes.add(String.class);
          classes.add(Throwable.class);
          classes.add(ArrayList.class);
          classes.add(HashMap.class);
          classes.add(LinkedHashMap.class);
          classes.add(LinkedList.class);
          classes.add(Stack.class);
          classes.add(Vector.class);
          classes.add(MqlDomain.class);
          classes.add(MqlColumn.class);
          classes.add(MqlCondition.class);
          classes.add(MqlOrder.class);
          classes.add(ColumnType.class);
          classes.add(CombinationType.class);
          classes.add(MqlBusinessTable.class);
          classes.add(MqlCategory.class);
          classes.add(MqlModel.class);
          classes.add(MqlQuery.class);
          classes.add(Operator.class);
          classes.add(DatabaseColumnType.class);
          classes.add(Connection.class);
          classes.add(Datasource.class);
          classes.add(MqlOrder.class);
          classes.add(Column.class);
          classes.add(Attribute.class);
          classes.add(Envelope.class);
          classes.add(BusinessData.class);
          classes.add(ResultSetObject.class);
        }
        @Override
        public boolean shouldDeserializeFields(Class<?> clazz) {
          return classes.contains(clazz);
        }

        @Override
        public boolean shouldSerializeFields(Class<?> clazz) {

          return classes.contains(clazz);
            
        }

        @Override
        public void validateDeserialize(Class<?> arg0) throws SerializationException {
          
            
        }

        @Override
        public void validateSerialize(Class<?> arg0) throws SerializationException {
          
            
        }
        
      };
  }

}