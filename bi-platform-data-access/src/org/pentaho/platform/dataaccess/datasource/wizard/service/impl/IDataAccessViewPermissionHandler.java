package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;
import java.util.Map;

import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Implement this interface to override the view permissions behavior of
 * data access.
 * 
 * This interface may be implemented and then the implementation class specified 
 * in the data-access settings.xml file, within the
 * settings/dataaccess-permission-handler. The specific setting for this class
 * is data-access-view-roles and data-access-view-users
 * 
 * @author Ramaiz Mansoor (rmansoor@pentaho.com)
 */
public interface IDataAccessViewPermissionHandler {
  /**
   * This method returns list of permitted roles who are allowed to view and use datasource
   * 
   * @param session pentaho session
   * @return List of permitted roles
   */

  List<String> getPermittedRoleList(IPentahoSession session);
  /**
   * This method returns list of permitted user who are allowed to view and use datasource
   * 
   * @param session pentaho session
   * @return List of permitted users
   */
  
  List<String> getPermittedUserList(IPentahoSession session);
}
