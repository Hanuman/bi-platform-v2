package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDelegate;
import org.pentaho.platform.plugin.services.webservices.PentahoSessionHolder;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

public class DatasourceServiceBeanImpl implements DatasourceGwtService {

  DatasourceServiceDelegate SERVICE;

  public DatasourceServiceBeanImpl() {
    SERVICE = new DatasourceServiceDelegate();
  }

  public Boolean addDatasource(IDatasource datasource) {
    return SERVICE.addDatasource(datasource);
  }

  public Boolean deleteDatasource(IDatasource datasource) {
    return SERVICE.deleteDatasource(datasource);
  }

  public Boolean deleteDatasource(String name) {
    return SERVICE.deleteDatasource(name);
  }

  public SerializedResultSet doPreview(IConnection connection, String query, String previewLimit)
      throws DatasourceServiceException {
    return SERVICE.doPreview(connection, query, previewLimit);
  }

  public SerializedResultSet doPreview(IDatasource datasource) throws DatasourceServiceException {
    return SERVICE.doPreview(datasource);
  }

  public IDatasource getDatasourceByName(String name) {
    return SERVICE.getDatasourceByName(name);
  }

  public List<IDatasource> getDatasources() {
    return SERVICE.getDatasources();
  }

  public Boolean updateDatasource(IDatasource datasource) {
    return SERVICE.updateDatasource(datasource);
  }

  public BusinessData generateModel(String modelName, IConnection connection, String query, String previewLimit)
      throws DatasourceServiceException {
    return SERVICE.generateModel(modelName, connection, query, previewLimit);
  }
  public BusinessData saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit)
  throws DatasourceServiceException {
    return SERVICE.saveModel(modelName, connection, query, overwrite, previewLimit);
  }

  public Boolean saveModel(BusinessData businessData, Boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveModel(businessData, overwrite);
  }
  public Boolean isAdministrator() {
    SERVICE.setSession(PentahoSessionHolder.getSession());
    return SERVICE.isAdministrator();
  }

//  @Override
//  protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest arg0, String arg1, String arg2) {
//    return new SerializationPolicy() {
//
//      List<Class<?>> classes = new ArrayList<Class<?>>();
//      {
//        classes.add(Exception.class);
//        classes.add(Integer.class);
//        classes.add(Number.class);
//        classes.add(Boolean.class);
//        classes.add(RuntimeException.class);
//        classes.add(String.class);
//        classes.add(Throwable.class);
//        classes.add(ArrayList.class);
//        classes.add(HashMap.class);
//        classes.add(LinkedHashMap.class);
//        classes.add(LinkedList.class);
//        classes.add(Stack.class);
//        classes.add(Vector.class);
//        classes.add(MqlDomain.class);
//        classes.add(MqlColumn.class);
//        classes.add(MqlCondition.class);
//        classes.add(MqlOrder.class);
//        classes.add(ColumnType.class);
//        classes.add(CombinationType.class);
//        classes.add(MqlBusinessTable.class);
//        classes.add(MqlCategory.class);
//        classes.add(MqlModel.class);
//        classes.add(MqlQuery.class);
//        classes.add(Operator.class);
//        classes.add(DatabaseColumnType.class);
//        classes.add(Connection.class);
//        classes.add(Datasource.class);
//        classes.add(MqlOrder.class);
//        classes.add(Column.class);
//        classes.add(Attribute.class);
//        classes.add(Envelope.class);
//        classes.add(BusinessData.class);
//        classes.add(SerializedResultSet.class);
//        classes.add(DataType.class);
//        classes.add(Domain.class);
//        classes.add(IPhysicalColumn.class);
//        classes.add(LogicalModel.class);
//        classes.add(Category.class);
//        classes.add(LogicalColumn.class);
//        classes.add(SqlPhysicalColumn.class);
//        classes.add(SqlPhysicalModel.class);
//        classes.add(SqlPhysicalTable.class);
//        classes.add(Concept.class);
//        classes.add(AggregationType.class);
//        classes.add(DataType.class);
//        classes.add(TargetColumnType.class);
//        classes.add(TargetTableType.class);
//        classes.add(LocalizedString.class);
//      }
//
//      @Override
//      public boolean shouldDeserializeFields(Class<?> clazz) {
//        return classes.contains(clazz);
//      }
//
//      @Override
//      public boolean shouldSerializeFields(Class<?> clazz) {
//
//        return classes.contains(clazz);
//
//      }
//
//      @Override
//      public void validateDeserialize(Class<?> arg0) throws SerializationException {
//
//      }
//
//      @Override
//      public void validateSerialize(Class<?> arg0) throws SerializationException {
//
//      }
//
//    };
//  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

  public BusinessData generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure) throws DatasourceServiceException {
    return SERVICE.generateInlineEtlModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure);
   }

  public Boolean saveInlineEtlModel(Domain modelName, Boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveInlineEtlModel(modelName, overwrite);
  }
  
}