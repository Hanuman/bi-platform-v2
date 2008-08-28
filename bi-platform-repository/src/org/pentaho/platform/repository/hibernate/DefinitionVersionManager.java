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
 * Created Sep 28, 2005 
 * @author mbatchel
 */
package org.pentaho.platform.repository.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.pentaho.platform.api.repository.IHibernatedObjectExtensionList;
import org.pentaho.platform.api.repository.RepositoryException;

public class DefinitionVersionManager {
  private Map versionMap = new HashMap();

  private int revision = -1; // Hibernate Revision

  private String key;

  private static final String VersionKey = "PENTAHO_HIBERNATE_VERSIONS"; //$NON-NLS-1$

  protected static String getVersionKey() {
    return DefinitionVersionManager.VersionKey;
  }

  public static void performAutoUpdateIfRequired() {
    Session session = HibernateUtil.getSession();
    try {
      DefinitionVersionManager mgr = null;
      try {
        HibernateUtil.beginTransaction();
        mgr = (DefinitionVersionManager) session.get(DefinitionVersionManager.class, DefinitionVersionManager
            .getVersionKey(), LockMode.READ); // Ignore Caches and just check the object
        HibernateUtil.commitTransaction();
      } catch (Exception handledForPG) {
        // Gotta rollback or the connection gets messed up by PostgreSQL
        try {
          session.connection().rollback();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        HibernateUtil.closeSession();
      }
      if (mgr == null) {
        // Never been stored before - Create one
        mgr = new DefinitionVersionManager();
        mgr.setKey(DefinitionVersionManager.getVersionKey());
      }
      boolean needsUpdate = mgr.versionsUpdated();
      if (needsUpdate) {
        // Make sure that hibernate session is closed
        HibernateUtil.closeSession();
        // Update the schema using PentahoSchemaUpdate
        HibernateUtil.updateSchema();
        // Gets the session
        session = HibernateUtil.getSession();
        // Persist the DefinitionVersionManager class with the updated version numbers
        session.saveOrUpdate(mgr);
        // Write the changes to the DB
        session.flush();
        try {
          // Commit the work
          session.connection().commit();
        } catch (Exception ex) {
          try {
            session.connection().rollback();
          } catch (Exception e2) {
            throw new RepositoryException(e2);
          }
          throw new RepositoryException(ex);
        }
      }
    } finally {
      HibernateUtil.closeSession();
    }
  }

  public boolean versionsUpdated() {
    Map verMap = this.getVersionMap();
    boolean needsUpdate = false;
    List objectHandlers = HibernateUtil.getHibernatedObjectHandlerList();
    IHibernatedObjectExtensionList handler;
    Map objectVersionMap;
    Iterator it;
    Map.Entry ent;
    String clName;
    Integer clVersion;
    for (int i = 0; i < objectHandlers.size(); i++) {
      handler = (IHibernatedObjectExtensionList) objectHandlers.get(i);
      objectVersionMap = handler.getHibernatedObjectVersionMap();
      it = objectVersionMap.entrySet().iterator();
      while (it.hasNext()) {
        ent = (Map.Entry) it.next();
        clName = (String) ent.getKey();
        clVersion = (Integer) ent.getValue();
        needsUpdate |= checkVersion(verMap, clVersion.intValue(), clName);
      }
    }
    /*
     * needsUpdate = checkVersion(verMap, ContentItem.ClassVersionNumber,
     * "ContentItem"); //$NON-NLS-1$ needsUpdate |= checkVersion(verMap,
     * ContentItemFile.ClassVersionNumber, "ContentItemFile"); //$NON-NLS-1$
     * needsUpdate |= checkVersion(verMap,
     * ContentLocation.ClassVersionNumber, "ContentLocation"); //$NON-NLS-1$
     * needsUpdate |= checkVersion(verMap,
     * RuntimeElement.ClassVersionNumber, "RuntimeElement"); //$NON-NLS-1$
     */
    return needsUpdate;
  }

  private boolean checkVersion(final Map verMap, final int curVerNum, final String className) {
    Long currentVersionNumber = new Long(curVerNum);
    Long storedVerNum = (Long) verMap.get(className);
    if (storedVerNum == null) {
      verMap.put(className, currentVersionNumber);
      return true;
    } else if (!storedVerNum.equals(currentVersionNumber)) {
      verMap.put(className, currentVersionNumber);
      return true;
    }
    return false;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(final int revision) {
    this.revision = revision;
  }

  protected Map getVersionMap() {
    return versionMap;
  }

  protected void setVersionMap(final Map versionMap) {
    this.versionMap = versionMap;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String value) {
    this.key = value;
  }

}
