package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

public  class ConnectionServiceHelper {
  private static final Log logger = LogFactory.getLog(ConnectionServiceHelper.class);
  private static IDatasourceMgmtService datasourceMgmtSvc;
  private static char PASSWORD_REPLACE_CHAR = '*';

  static {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session);    
  }

  public static String getConnectionPassword(String connectionName, String password) throws ConnectionServiceException {
    try {
      HibernateUtil.beginTransaction();
      IDatasource datasource = datasourceMgmtSvc.getDatasource(connectionName);
      HibernateUtil.commitTransaction();
      if (datasource != null && !hasPasswordChanged(password)) {
        return datasource.getPassword();
      } else {
        return password;
      }
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD",
          connectionName, e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceHelper.ERROR_0001_UNABLE_TO_GET_CONNECTION_PASSWORD", connectionName, e.getLocalizedMessage()), e);
    }
  }
  
  private static boolean hasPasswordChanged(String password) {
    if (password != null && password.length() > 0) {
      for (char character : password.toCharArray()) {
        if (character != PASSWORD_REPLACE_CHAR) {
          return true;
        }
      }
    }
    return false;
  }
  
  public static String encodePassword(String password) {
    StringBuffer buffer = null;
    if (password != null && password.length() > 0) {
      buffer = new StringBuffer(password.length());
      for (int i = 0; i < password.length(); i++) {
        buffer.append(PASSWORD_REPLACE_CHAR);
      }
    } else {
      buffer = new StringBuffer();
    }
    return buffer.toString();
  }

}
