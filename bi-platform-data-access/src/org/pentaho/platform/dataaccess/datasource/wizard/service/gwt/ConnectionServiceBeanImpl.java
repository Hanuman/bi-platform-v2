package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.webservices.PentahoSessionHolder;

public class ConnectionServiceBeanImpl implements ConnectionGwtService {

  ConnectionServiceDelegate SERVICE;
  IDatasourceMgmtService datasourceMgmtSvc;
  
  private ConnectionServiceDelegate getService(){
    if(SERVICE == null){
      try {
        IPentahoSession session = PentahoSessionHolder.getSession();
        
        datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session); //$NON-NLS-1$
        SERVICE = new ConnectionServiceDelegate(datasourceMgmtSvc);
        
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<IConnection> getConnections() throws ConnectionServiceException {
    return getService().getConnections();
  }
  public IConnection getConnectionByName(String name) throws ConnectionServiceException {
    return getService().getConnectionByName(name);
  }
  public Boolean addConnection(IConnection connection) throws ConnectionServiceException {
    return getService().addConnection(connection);
  }

  public Boolean updateConnection(IConnection connection) throws ConnectionServiceException {
    return getService().updateConnection(connection);
  }

  public Boolean deleteConnection(IConnection connection) throws ConnectionServiceException {
    return getService().deleteConnection(connection);
  }
    
  public Boolean deleteConnection(String name) throws ConnectionServiceException {
    return getService().deleteConnection(name);    
  }

  public Boolean testConnection(IConnection connection) throws ConnectionServiceException {
    return getService().testConnection(connection);    
  }
  
//  @Override
//  protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest arg0, String arg1, String arg2) {
//      return new SerializationPolicy(){
//
//        List<Class<?>> classes = new ArrayList<Class<?>>();
//        {
//          classes.add(Exception.class);
//          classes.add(Integer.class);
//          classes.add(Number.class);
//          classes.add(Boolean.class);
//          classes.add(RuntimeException.class);
//          classes.add(String.class);
//          classes.add(Throwable.class);
//          classes.add(ArrayList.class);
//          classes.add(HashMap.class);
//          classes.add(LinkedHashMap.class);
//          classes.add(LinkedList.class);
//          classes.add(Stack.class);
//          classes.add(Vector.class);
//          classes.add(MqlDomain.class);
//          classes.add(MqlColumn.class);
//          classes.add(MqlCondition.class);
//          classes.add(MqlOrder.class);
//          classes.add(ColumnType.class);
//          classes.add(CombinationType.class);
//          classes.add(MqlBusinessTable.class);
//          classes.add(MqlCategory.class);
//          classes.add(MqlModel.class);
//          classes.add(MqlQuery.class);
//          classes.add(Operator.class);
//          classes.add(DatabaseColumnType.class);
//          classes.add(Connection.class);
//          classes.add(Datasource.class);
//          classes.add(MqlOrder.class);
//          classes.add(Column.class);
//          classes.add(Attribute.class);
//          classes.add(Envelope.class);
//          classes.add(BusinessData.class);
//          classes.add(SerializedResultSet.class);
//          classes.add(DataType.class);
//          classes.add(Domain.class);
//          classes.add(IPhysicalColumn.class);
//          classes.add(LogicalModel.class);
//          classes.add(Category.class);
//          classes.add(LogicalColumn.class);
//          classes.add(SqlPhysicalColumn.class);
//          classes.add(SqlPhysicalModel.class);
//          classes.add(SqlPhysicalTable.class);
//          classes.add(Concept.class);
//          classes.add(AggregationType.class);
//          classes.add(DataType.class);
//          classes.add(TargetColumnType.class);
//          classes.add(TargetTableType.class);
//          classes.add(LocalizedString.class);
//        }
//        @Override
//        public boolean shouldDeserializeFields(Class<?> clazz) {
//          return classes.contains(clazz);
//        }
//
//        @Override
//        public boolean shouldSerializeFields(Class<?> clazz) {
//
//          return classes.contains(clazz);
//            
//        }
//
//        @Override
//        public void validateDeserialize(Class<?> arg0) throws SerializationException {
//          
//            
//        }
//
//        @Override
//        public void validateSerialize(Class<?> arg0) throws SerializationException {
//          
//            
//        }
//        
//      };

}