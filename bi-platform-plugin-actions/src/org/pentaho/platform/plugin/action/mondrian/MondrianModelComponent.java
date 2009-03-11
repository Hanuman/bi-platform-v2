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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Apr 28, 2005
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.mondrian;

import java.util.Properties;

import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.MondrianException;
import mondrian.olap.Schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.util.logging.Logger;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MondrianModelComponent extends ComponentBase {

  private static final long serialVersionUID = -718697500002076945L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog(MondrianModelComponent.class);
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  public boolean init() {
    // get the settings from the system configuration file
    return true;

  }

  @Override
  public boolean validateAction() {

    return true;

  }

  @Override
  public boolean executeAction() {

    return true;
  }

  @Override
  public void done() {

  }

  public static String getInitialQuery(final Properties properties, final String cubeName, IPentahoSession session)
      throws Throwable {
    MDXConnection mdxConnection = (MDXConnection) PentahoConnectionFactory.getConnection(
        IPentahoConnection.MDX_DATASOURCE, properties, session, null);
    // mdxConnection.setProperties( properties );
    Connection connection = mdxConnection.getConnection();
    if (connection == null) {
      Logger
          .error(
              "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0001_INVALID_CONNECTION", properties.toString())); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    try {
      return MondrianModelComponent.getInitialQuery(connection, cubeName);
    } catch (Throwable t) {
      if (t instanceof MondrianException) {
        // pull the cause out, otherwise it never gets logged
        Throwable cause = ((MondrianException) t).getCause();
        if (cause != null) {
          throw cause;
        } else {
          throw t;
        }
      } else {
        throw t;
      }
    }
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param driver
   * @param user
   * @param password
   * @param cubeName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery(final String modelPath, final String connectionString, final String driver,
      final String user, final String password, final String cubeName, IPentahoSession session) throws Throwable {
    return MondrianModelComponent.getInitialQuery(modelPath, connectionString, driver, user, password, cubeName, null,
        session);
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param driver
   * @param user
   * @param password
   * @param cubeName
   * @param roleName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery(String modelPath, final String connectionString, final String driver,
      final String user, final String password, final String cubeName, final String roleName, IPentahoSession session)
      throws Throwable {

    Properties properties = new Properties();

    // TODO support driver manager connections
    if (!PentahoSystem.ignored) {
      if (driver != null) {
        properties.put("Driver", driver); //$NON-NLS-1$
      }
      if (user != null) {
        properties.put("User", user); //$NON-NLS-1$
      }
      if (password != null) {
        properties.put("Password", password); //$NON-NLS-1$
      }
    }

    if (modelPath.indexOf("http") == 0) { //$NON-NLS-1$
      properties.put("Catalog", modelPath); //$NON-NLS-1$
    } else {
      if (modelPath.indexOf("http") == 0) { //$NON-NLS-1$
        properties.put("Catalog", modelPath); //$NON-NLS-1$
      } else {
        if (!modelPath.startsWith("solution:")) { //$NON-NLS-1$
          modelPath = "solution:" + modelPath; //$NON-NLS-1$
        }
        properties.put("Catalog", modelPath); //$NON-NLS-1$
      }
    }
    properties.put("Provider", "mondrian"); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put("PoolNeeded", "false"); //$NON-NLS-1$//$NON-NLS-2$
    properties.put("dataSource", connectionString); //$NON-NLS-1$
    if (roleName != null) {
      properties.put("Role", roleName); //$NON-NLS-1$
    }

    return MondrianModelComponent.getInitialQuery(properties, cubeName, session);
  }

  /**
   * @param modelPath
   * @param connectionString
   * @param cubeName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery(final String modelPath, final String connectionString, final String cubeName,
      IPentahoSession session) throws Throwable {
    return MondrianModelComponent.getInitialQuery(modelPath, connectionString, cubeName, null, session);
  }

  /**
   * @param modelPath
   * @param jndi
   * @param cubeName
   * @param roleName
   * @return mdx string that represents the initial query
   * @throws Throwable
   * @deprecated
   */
  @Deprecated
  public static String getInitialQuery(String modelPath, String jndi, final String cubeName, final String roleName,
      IPentahoSession session) throws Throwable {

    Properties properties = new Properties();

    if (modelPath.indexOf("http") == 0) { //$NON-NLS-1$
      properties.put("Catalog", modelPath); //$NON-NLS-1$
    } else {
      if (!modelPath.startsWith("solution:")) { //$NON-NLS-1$
        modelPath = "solution:" + modelPath; //$NON-NLS-1$
      }
      properties.put("Catalog", modelPath); //$NON-NLS-1$
    }

    properties.put("Provider", "mondrian"); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put("PoolNeeded", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    properties.put("dataSource", jndi); //$NON-NLS-1$

    if (roleName != null) {
      properties.put("Role", roleName); //$NON-NLS-1$
    }
    return MondrianModelComponent.getInitialQuery(properties, cubeName, session);
  }

  public static String getInitialQuery(final Connection connection, final String cubeName) throws Throwable {

    String measuresMdx = null;
    String columnsMdx = null;
    StringBuffer rowsMdx = new StringBuffer();

    try {

      Schema schema = connection.getSchema();
      if (schema == null) {
        Logger
            .error(
                "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0002_INVALID_SCHEMA", connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      Cube cubes[] = schema.getCubes();
      if ((cubes == null) || (cubes.length == 0)) {
        Logger
            .error(
                "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0003_NO_CUBES", connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      if ((cubes.length > 1) && (cubeName == null)) {
        Logger
            .error(
                "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0004_CUBE_NOT_SPECIFIED", connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      Cube cube = null;
      if (cubes.length == 1) {
        cube = cubes[0];
      } else {
        for (Cube element : cubes) {
          if (element.getName().equals(cubeName)) {
            cube = element;
            break;
          }
        }
      }

      if (cube == null) {
        Logger
            .error(
                "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0005_CUBE_NOT_FOUND", cubeName, connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      Dimension dimensions[] = cube.getDimensions();
      if ((dimensions == null) || (dimensions.length == 0)) {
        Logger
            .error(
                "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0006_NO_DIMENSIONS", cubeName, connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }

      for (Dimension element : dimensions) {

        Hierarchy hierarchy = element.getHierarchy();
        if (hierarchy == null) {
          Logger
              .error(
                  "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0007_NO_HIERARCHIES", element.getName(), cubeName, connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }

        Member member = hierarchy.getDefaultMember();

        if (member == null) {
          Logger
              .error(
                  "MondrianModelComponent", Messages.getErrorString("MondrianModel.ERROR_0008_NO_DEFAULT_MEMBER", element.getName(), cubeName, connection.getConnectString())); //$NON-NLS-1$ //$NON-NLS-2$
          return null;
        }
        if (element.isMeasures()) {
          // measuresMdx = "with member "+ member.getUniqueName();
          // //$NON-NLS-1$
          measuresMdx = ""; //$NON-NLS-1$
          columnsMdx = " select NON EMPTY {" + member.getUniqueName() + "} ON columns, "; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          if (rowsMdx.length() > 0) {
            rowsMdx.append(", "); //$NON-NLS-1$
          }
          rowsMdx.append(member.getUniqueName());
        }
      }
      if ((measuresMdx != null) && (columnsMdx != null) && (rowsMdx.length() > 0)) {
        StringBuffer result = new StringBuffer(measuresMdx.length() + columnsMdx.length() + rowsMdx.length() + 50);
        result.append(measuresMdx).append(columnsMdx).append("NON EMPTY {(") //$NON-NLS-1$
            .append(rowsMdx).append(")} ON rows ") //$NON-NLS-1$
            .append("from [" + cube.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        return result.toString();

      }
      return null;
    } catch (Throwable t) {
      if (t instanceof MondrianException) {
        // pull the cause out, otherwise it never gets logged
        Throwable cause = ((MondrianException) t).getCause();
        if (cause != null) {
          throw cause;
        } else {
          throw t;
        }
      } else {
        throw t;
      }
    }
  }

  protected SQLConnection getConnection(final String jndiName, final String driver, final String userId,
      final String password, final String connectionInfo) {
    SQLConnection connection = null;
    try {
      if (jndiName != null) {
        connection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE,
            jndiName, getSession(), this);
      }
      if (connection == null) {
        if ((driver == null) && (connectionInfo == null)) {
          // TODO raise an error
        }
        connection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, driver,
            connectionInfo, userId, password, getSession(), this);
      }
      if (connection == null) {
        Logger.error(
            "MondrianModelComponent", Messages.getErrorString("SQLBaseComponent.ERROR_0005_INVALID_CONNECTION")); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }
      return connection;
    } catch (Exception e) {
      Logger.error(
          "MondrianModelComponent", Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", ""), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return null;
  }

}
