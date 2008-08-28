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
package org.pentaho.platform.repository.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository.IHibernatedObjectExtensionList;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;

public class SubscriptionHibernateHandler implements IHibernatedObjectExtensionList {

  public List getHibernatedObjectResourceList() {
    return new ArrayList<String>();
    /*
     * Now, it's all included in one file instead of multiple individual ones
     * 
    String dialectFolder = HibernateUtil.getDialectFolder();
    assert dialectFolder != null;
    String hbmFolder = "hibernate/" + dialectFolder;
    
    rtn.add(hbmFolder + "/Schedule.hbm.xml"); //$NON-NLS-1$
    rtn.add(hbmFolder + "/Subscription.hbm.xml"); //$NON-NLS-1$
    rtn.add(hbmFolder + "/SubscribeContent.hbm.xml"); //$NON-NLS-1$
    // Add the dbbased repository stuff
    rtn.add(hbmFolder + "/RepositoryFile.hbm.xml"); //$NON-NLS-1$

    return rtn;
    */
  }

  public Map getHibernatedObjectVersionMap() {
    Map<String, Integer> rtn = new HashMap<String, Integer>();
    rtn.put("Schedule", new Integer(Schedule.ClassVersionNumber)); //$NON-NLS-1$
    rtn.put("Subscription", new Integer(Subscription.ClassVersionNumber)); //$NON-NLS-1$
    rtn.put("SubscribeContent", new Integer(SubscribeContent.ClassVersionNumber)); //$NON-NLS-1$
    // Add the dbbased repository stuff
    rtn.put("RepositoryFile", new Integer(RepositoryFile.ClassVersionNumber)); //$NON-NLS-1$
    return rtn;
  }

}
