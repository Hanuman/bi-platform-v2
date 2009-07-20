
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
     
public interface IDatasourceService {
  /**
   * Returns the list of Logical Models. This method is used by the client app to display list of models
   * 
   * @return List of LogicalModelSummary.
   */
  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException;
  /**
   * Delete the Logical Mode identified by the Domain ID and the Model Name
   * 
   * @return true if the deletion of model was successful otherwise false.
   */  
  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException;
  /**
   * Returns the serialized version of SQL ResultSet. 
   * 
   * @param connectionName - Name of the connection
   * @param query - Query which needs to be executed
   * @param previewLimit - Number of row which needs to be returned for this query
   *
   * @throws DatasourceServiceException
   * @return SerializedResultSet - This object contains the data, column name and column types
   */    
  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException;
  /**
   * Returns the generated relational based logical model along with the sample data for the given connection name and query
   *
   * @param modelName - Name of the model to be generated   
   * @param connectionName - Name of the connection
   * @param query - Query which needs to be executed
   * @param previewLimit - Number of row which needs to be returned for this query
   * 
   * @throws DatasourceServiceException 
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */    
  public BusinessData generateLogicalModel(String modelName, String connectionName, String query, String previewLimit) throws DatasourceServiceException;
  /**
   * Returns the generated logical model along with the sample data for the given connection name and query.
   * This method also saves the model
   *
   * @param modelName - Name of the model to be generated   
   * @param connectionName - Name of the connection
   * @param query - Query which needs to be executed
   * @param previewLimit - Number of row which needs to be returned for this query
   * 
   * @throws DatasourceServiceException 
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */    
  public BusinessData generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit) throws DatasourceServiceException;  
  /**
   * Save the generated model. This could be either Relational or CSV based model
   *
   * @param domain - generated Domain     
   * @param overwrite - should the domain be overwritten or not
   * 
   * @throws DatasourceServiceException
   * @return true if the model was saved successfully otherwise false
   */    
  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException;
  /**
   * Returns the generated csv based logical model along with the sample data for the given connection name and query
   *
   * @param modelName - Name of the model to be generated   
   * @param relativeFilePath - Relative path to the file that needs to be uploaded
   * @param headersPresent - Are headers present in the file or not
   * @param delimeter - Delimiter that was used in the file
   * @param enclosure - Enclosure that was used in the file
   *  
   * @throws DatasourceServiceException 
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */    
  public BusinessData generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure) throws DatasourceServiceException;

  /**
   * Returns whether the current user has the authority to create/edit/delete datasources
   *
   * @throws DatasourceServiceException
   * @return true if the user has permission otherwise false
   */      
  public boolean hasPermission();
  /**
   * This is a method for the Gwt workaround. This should not be used by any client at all
   *
   * @return BogoPojo
   */      
  public BogoPojo gwtWorkaround(BogoPojo pojo);
  /**
   * Returns the save logical model for a given Domain ID and Model ID
   *
   * @param domainId - ID of the domain to be generated   
   * @param modelId - ID of the model to be generated
   * 
   * @throws DatasourceServiceException
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */      
  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException;
}

 