package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.pentaho.metadata.messages.LocaleHelper;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.models.RelationalModel.ConnectionEditType;


@SuppressWarnings("nls")
public class RelationalModelTest {
  
  @Test
  public void test() {
    RelationalModel relationalModel = new RelationalModel();
    Assert.assertNull(relationalModel.getBusinessData());
    Assert.assertEquals(0, relationalModel.getConnections().size());
    Assert.assertEquals(0,relationalModel.getDataRows().size());
    Assert.assertEquals(ConnectionEditType.ADD,relationalModel.getEditType());
    Assert.assertNull(relationalModel.getQuery());
    Assert.assertNull(relationalModel.getSelectedConnection());
    Assert.assertNull(relationalModel.getDatasourceName());
    Assert.assertEquals(false, relationalModel.isValidated());
    IConnection connection = new Connection();
    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName("SampleData");
    connection.setPassword("password");
    connection.setUrl("jdbc:hsqldb:file:test-src/solution/system/data/sampledata");
    connection.setUsername("pentaho_user");
    List<IConnection> connectionList = new ArrayList<IConnection>();
    connectionList.add(connection);
    relationalModel.setConnections(connectionList);
    relationalModel.setSelectedConnection(connection);
    relationalModel.setEditType(ConnectionEditType.EDIT);
    relationalModel.setQuery("select * from customers");
    relationalModel.setPreviewLimit("10");
    relationalModel.setDatasourceName("newdatasource");
    relationalModel.validate();
    Assert.assertEquals(true, relationalModel.isPreviewValidated());
    Assert.assertEquals(false, relationalModel.isValidated());
    LogicalColumn logColumn = new LogicalColumn();
    logColumn.setDataType(DataType.NUMERIC);
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.add(AggregationType.AVERAGE);
    logColumn.setAggregationList(aggTypeList);
    logColumn.setName(new LocalizedString("En", "Column1"));
    List<ModelDataRow> dataRows = new ArrayList<ModelDataRow>();
    List<String> data = new ArrayList<String>();
    data.add("Sample1");
    data.add("Sample2");
    data.add("Sample3");
    data.add("Sample4");
    data.add("Sample5");
    ModelDataRow row = new ModelDataRow(logColumn, data, "En");
    dataRows.add(row);
    relationalModel.setDataRows(dataRows);
    BusinessData businessData = new BusinessData();
    List<List<String>> dataSample = new ArrayList<List<String>>();
    List<String> rowData = new ArrayList<String>();
    rowData.add("Data1");
    rowData.add("Data2");
    rowData.add("Data3");
    rowData.add("Data4");
    dataSample.add(rowData);

    String locale = LocaleHelper.getLocale().toString();
    
    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName("SampleData");
    model.setDatasource(dataSource);
    SqlPhysicalTable table = new SqlPhysicalTable(model);
    model.getPhysicalTables().add(table);
    table.setTargetTableType(TargetTableType.INLINE_SQL);
    table.setTargetTable("select * from customers");
    
    SqlPhysicalColumn column = new SqlPhysicalColumn(table);
    column.setTargetColumn("customername");
    column.setName(new LocalizedString(locale, "Customer Name"));
    column.setDescription(new LocalizedString(locale, "Customer Name Desc"));
    column.setDataType(DataType.STRING);
    
    table.getPhysicalColumns().add(column);
    
    LogicalModel logicalModel = new LogicalModel();
    model.setId("MODEL");
    model.setName(new LocalizedString(locale, "My Model"));
    model.setDescription(new LocalizedString(locale, "A Description of the Model"));
    
    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setPhysicalTable(table);
    
    logicalModel.getLogicalTables().add(logicalTable);
    
    LogicalColumn logicalColumn = new LogicalColumn();
    logicalColumn.setId("LC_CUSTOMERNAME");
    logicalColumn.setPhysicalColumn(column);
    
    logicalTable.addLogicalColumn(logicalColumn);
    
    Category mainCategory = new Category();
    mainCategory.setId("CATEGORY");
    mainCategory.setName(new LocalizedString(locale, "Category"));
    mainCategory.addLogicalColumn(logicalColumn);
    
    logicalModel.getCategories().add(mainCategory);
    
    Domain domain = new Domain();
    domain.addPhysicalModel(model);
    domain.addLogicalModel(logicalModel);
    List<LocaleType> localeTypeList = new ArrayList<LocaleType>();
    localeTypeList.add(new LocaleType("Code", "Locale Description"));
    domain.setLocales(localeTypeList);
    businessData.setData(dataSample);
    businessData.setDomain(domain);
    relationalModel.setBusinessData(businessData);
    Assert.assertEquals(true, relationalModel.isValidated()); 
  }
}
