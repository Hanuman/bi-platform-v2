/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Nov 7, 2005 
 * @author mbatchel
 * 
 * 99% of this file is directly copied from Hibernate. The problem with the hibernate
 * version is that it won't work in under JTA transactions.
 * 
 */
package org.pentaho.platform.repository.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.pentaho.platform.repository.messages.Messages;

/**
 * A commandline tool to update a database schema. May also be called from
 * inside an application.
 * 
 * @author Christoph Sturm
 */
public class PentahoSchemaUpdate {

  private static final Log log = LogFactory.getLog(PentahoSchemaUpdate.class);

  private ConnectionProvider connectionProvider;

  private Configuration configuration;

  private Dialect dialect;

  private List exceptions;

  public PentahoSchemaUpdate(final Configuration cfg) throws HibernateException {
    this(cfg, cfg.getProperties());
  }

  public PentahoSchemaUpdate(final Configuration cfg, final Properties connectionProperties) throws HibernateException {
    this.configuration = cfg;
    dialect = Dialect.getDialect(connectionProperties);
    Properties props = new Properties();
    props.putAll(dialect.getDefaultProperties());
    props.putAll(connectionProperties);
    connectionProvider = ConnectionProviderFactory.newConnectionProvider(props);
    exceptions = new ArrayList();
  }

  /**
   * Execute the schema updates
   * 
   * @param script
   *            print all DDL to the console
   */
  public void execute(final boolean script, final boolean doUpdate) {

    PentahoSchemaUpdate.log.info(Messages.getString("PentahoSchemaUpdate.USER_RUNNING_SCHEMA_UPDATE")); //$NON-NLS-1$

    Connection connection = null;
    DatabaseMetadata meta;
    Statement stmt = null;
    exceptions.clear();

    try {

      try {
        PentahoSchemaUpdate.log.info(Messages.getString("PentahoSchemaUpdate.USER_FETCHING_DBMETADATA")); //$NON-NLS-1$
        connection = connectionProvider.getConnection();
        meta = new DatabaseMetadata(connection, dialect);
        stmt = connection.createStatement();
      } catch (SQLException sqle) {
        exceptions.add(sqle);
        PentahoSchemaUpdate.log.error(
            Messages.getErrorString("PentahoSchemaUpdate.ERROR_0001_CANNOT_GET_DBMETADATA"), sqle); //$NON-NLS-1$
        throw sqle;
      }

      PentahoSchemaUpdate.log.info(Messages.getString("PentahoSchemaUpdate.USER_UPDATING_SCHEMA")); //$NON-NLS-1$

      String[] createSQL = configuration.generateSchemaUpdateScript(dialect, meta);
      for (final String sql : createSQL) {
        try {
          if (script) {
            System.out.println(sql);
          }
          if (doUpdate) {
            PentahoSchemaUpdate.log.debug(sql);
            stmt.executeUpdate(sql);
          }
        } catch (SQLException e) {
          exceptions.add(e);
          PentahoSchemaUpdate.log.error(Messages.getString("PentahoSchemaUpdate.USER_UNSUCCESSFUL") + sql); //$NON-NLS-1$
          PentahoSchemaUpdate.log.error(e.getMessage());
        }
      }

      PentahoSchemaUpdate.log.info(Messages.getString("PentahoSchemaUpdate.USER_UPDATE_COMPLETE")); //$NON-NLS-1$

    } catch (Exception e) {
      exceptions.add(e);
      PentahoSchemaUpdate.log.error(Messages.getErrorString("PentahoSchemaUpdate.ERROR_0002_COULD_NOT_UPDATE"), e); //$NON-NLS-1$
    } finally {

      try {
        if (stmt != null) {
          stmt.close();
        }
        connection.commit();
        connection.close();
      } catch (Exception e) {
        exceptions.add(e);
        PentahoSchemaUpdate.log.error(Messages.getErrorString("PentahoSchemaUpdate.ERROR_0003_CLOSING_CONNECTION"), e); //$NON-NLS-1$
      }

    }
  }

  /**
   * Returns a List of all Exceptions which occured during the export.
   * 
   * @return A List containig the Exceptions occured during the export
   */
  public List getExceptions() {
    return exceptions;
  }
}
