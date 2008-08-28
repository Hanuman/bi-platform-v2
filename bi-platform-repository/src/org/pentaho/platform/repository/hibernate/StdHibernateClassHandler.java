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
 */
package org.pentaho.platform.repository.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository.IHibernatedObjectExtensionList;
import org.pentaho.platform.repository.content.BackgroundExecutedContentId;
import org.pentaho.platform.repository.content.ContentItem;
import org.pentaho.platform.repository.content.ContentItemFile;
import org.pentaho.platform.repository.content.ContentLocation;
import org.pentaho.platform.repository.datasource.Datasource;
import org.pentaho.platform.repository.runtime.RuntimeElement;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;

public class StdHibernateClassHandler implements IHibernatedObjectExtensionList {

  public List getHibernatedObjectResourceList() {
    List<String> rtn = new ArrayList<String>();
    String dialectFolder = HibernateUtil.getDialectFolder();
    assert dialectFolder != null;
    rtn.add("hibernate/" + dialectFolder + ".hbm.xml"); //$NON-NLS-1$ //$NON-NLS-2$
/*
 * Now, all settings are in a single hbm.xml file for each database.
 * 
    String hbmFolder = "hibernate/" + dialectFolder;
    rtn.add(hbmFolder + "/RuntimeElement.hbm.xml"); //$NON-NLS-1$
    rtn.add(hbmFolder + "/ContentLocation.hbm.xml");//$NON-NLS-1$
    rtn.add(hbmFolder + "/ContentItem.hbm.xml");//$NON-NLS-1$
    rtn.add(hbmFolder + "/ContentItemFile.hbm.xml");//$NON-NLS-1$
    rtn.add(hbmFolder + "/BackgroundExecutedContentId.hbm.xml");//$NON-NLS-1$
    rtn.add(hbmFolder + "/DefinitionVersionManager.hbm.xml");//$NON-NLS-1$
*/    
    return rtn;
  }

  public Map getHibernatedObjectVersionMap() {
    Map<String, Integer> objVersions = new HashMap<String, Integer>();
    objVersions.put("ContentItem", new Integer(ContentItem.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("ContentItemFile", new Integer(ContentItemFile.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("ContentLocation", new Integer(ContentLocation.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("BackgroundExecutedContentId", new Integer(BackgroundExecutedContentId.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("RuntimeElement", new Integer(RuntimeElement.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("Datasource", new Integer(Datasource.ClassVersionNumber)); //$NON-NLS-1$
    objVersions.put("UserSetting", new Integer(UserSetting.ClassVersionNumber)); //$NON-NLS-1$
    return objVersions;
  }

}
