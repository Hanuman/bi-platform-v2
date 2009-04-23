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
 * The purpose of this class is to maintain a list of versions of each hibernated
 * class (the object definition, not the contents of any one object) for the purposes
 * of initiating an automatic schema update.
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.repository.datasource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;

public class DatasourceMgmtService implements IDatasourceMgmtService {

  private static final Log logger = LogFactory.getLog(DatasourceMgmtService.class);
  public Log getLogger() {
    return DatasourceMgmtService.logger;
  }
  public DatasourceMgmtService() {
  }
  
  public void createDatasource(IDatasource newDatasource) throws DuplicateDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    if(newDatasource != null) {
      if (getDatasource(newDatasource.getName()) == null) {
        try {
          IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
          newDatasource.setPassword(passwordService.encrypt(newDatasource.getPassword()));
          session.save(newDatasource);
        } catch(ObjectFactoryException objface) {
          throw new DatasourceMgmtServiceException(Messages.getString(
            "DatasourceMgmtService.UNABLE_TO_INIT_PASSWORD_SERVICE"));
        } catch(PasswordServiceException pse) {
            session.evict(newDatasource);
          throw new DatasourceMgmtServiceException( pse.getMessage(), pse );
        } catch (HibernateException ex) {
          session.evict(newDatasource);
          throw new DatasourceMgmtServiceException(Messages.getString(
              "DatasourceMgmtService.UNABLE_TO_CREATE_DATASOURCE"), ex );
        }
      } else {
        throw new DuplicateDatasourceException(Messages.getString(
        "DatasourceMgmtService.DATASOURCE_ALREADY_EXIST") + newDatasource.getName());
      }
    } else {
      throw new DatasourceMgmtServiceException(Messages.getString(
          "DatasourceMgmtService.NULL_DATASOURCE_OBJECT"));
    }
    HibernateUtil.flushSession();
   }
  public void deleteDatasource(String jndiName) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    IDatasource datasource = getDatasource(jndiName);
    if (datasource != null) {
      deleteDatasource(datasource);
    } else {
      throw new NonExistingDatasourceException(Messages.getString(
        "DatasourceMgmtService.DATASOURCE_DOES_NOT_EXIST") +jndiName);
    }
  }  
  public void deleteDatasource(IDatasource datasource) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
      if (datasource != null) {
        try {
          session.delete(session.merge(datasource));
        } catch (HibernateException ex) {
          throw new DatasourceMgmtServiceException( ex.getMessage(), ex );
        }
      } else {
        throw new NonExistingDatasourceException(Messages.getString(
          "DatasourceMgmtService.DATASOURCE_DOES_NOT_EXIST") +datasource.getName());
      }

    HibernateUtil.flushSession();
  }

  public IDatasource getDatasource(String jndiName) throws DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    IDatasource datasource = null;
    try {
      IDatasource pentahoDatasource = (IDatasource) session.get(Datasource.class, jndiName);
      if(pentahoDatasource != null) {
        datasource = clone(pentahoDatasource);
        IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
        datasource.setPassword(passwordService.decrypt(datasource.getPassword()));
      }
      return datasource;
    } catch(ObjectFactoryException objface) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);
    } catch(PasswordServiceException pse) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_DECRYPT_PASSWORD"), pse );
    } catch (HibernateException ex) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_RETRIEVE_DATASOURCE"), ex);
    }
  }

  public List<IDatasource> getDatasources() throws DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    try {
      String nameQuery = "org.pentaho.platform.repository.datasource.Datasource.findAllDatasources"; //$NON-NLS-1$
      Query qry = session.getNamedQuery(nameQuery).setCacheable(true);
      List<IDatasource> pentahoDatasourceList = qry.list();
      List<IDatasource> datasourceList = new ArrayList<IDatasource>();
      for(IDatasource pentahoDatasource: pentahoDatasourceList) {
        IDatasource datasource = clone(pentahoDatasource);
        IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
        datasource.setPassword(passwordService.decrypt(datasource.getPassword()));
        datasourceList.add(datasource);        
      }
      return datasourceList;
    } catch(PasswordServiceException pse) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_ENCRYPT_PASSWORD"), pse );
    } catch(ObjectFactoryException objface) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);
    } catch (HibernateException ex) {
      throw new DatasourceMgmtServiceException(Messages.getString(
        "DatasourceMgmtService.UNABLE_TO_RETRIEVE_DATASOURCE"), ex );
    }
  }

  public void updateDatasource(IDatasource datasource) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    if(datasource != null) {
      IDatasource tmpDatasource = getDatasource(datasource.getName());
      if (tmpDatasource != null) {
        try {
          IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);          // Store the new encrypted password in the datasource object
          datasource.setPassword(passwordService.encrypt(datasource.getPassword()));
          session.update(session.merge(datasource));
        } catch(ObjectFactoryException objface) {
          throw new DatasourceMgmtServiceException(Messages.getString(
            "DatasourceMgmtService.UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);
        } catch(PasswordServiceException pse) {
            throw new DatasourceMgmtServiceException( Messages.getString(
              "DatasourceMgmtService.UNABLE_TO_ENCRYPT_PASSWORD"), pse );
        } catch (HibernateException ex) {
          throw new DatasourceMgmtServiceException(Messages.getString(
            "DatasourceMgmtService.UNABLE_TO_RETRIEVE_DATASOURCE"), ex );
        }
      } else {
        throw new NonExistingDatasourceException(Messages.getString(
          "DatasourceMgmtService.DATASOURCE_DOES_NOT_EXIST", datasource.getName()) );
      }
    } else {
      throw new DatasourceMgmtServiceException(Messages.getString(
          "DatasourceMgmtService.NULL_DATASOURCE_OBJECT"));
    }
  }
  
  public void init(final IPentahoSession session) {
    HibernateUtil.beginTransaction();
  }
  
  private IDatasource clone (IDatasource datasource) throws ObjectFactoryException {
      IDatasource returnDatasource = PentahoSystem.getObjectFactory().get(IDatasource.class, null);
      returnDatasource.setDriverClass(datasource.getDriverClass());
      returnDatasource.setIdleConn(datasource.getIdleConn());
      returnDatasource.setMaxActConn(datasource.getMaxActConn());
      returnDatasource.setName(datasource.getName());
      returnDatasource.setPassword(datasource.getPassword());
      returnDatasource.setQuery(datasource.getQuery());
      returnDatasource.setUrl(datasource.getUrl());
      returnDatasource.setUserName(datasource.getUserName());
      returnDatasource.setWait(datasource.getWait());
      return returnDatasource;
  }
  
}