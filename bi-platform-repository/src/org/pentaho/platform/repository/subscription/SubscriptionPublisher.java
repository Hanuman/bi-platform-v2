/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jun 25, 2008 
 * @author wseyler
 */


package org.pentaho.platform.repository.subscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * @author wseyler
 *
 */
public class SubscriptionPublisher extends BasePublisher {
  private static final Log logger = LogFactory.getLog(SubscriptionPublisher.class);

   public Log getLogger() {
    return logger;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.engine.core.system.BasePublisher#publish(org.pentaho.platform.api.engine.IPentahoSession)
   */
  @Override
  public String publish(IPentahoSession session) {
    String publishSrcPath = PentahoSystem.getApplicationContext().getSolutionPath("") + "system/ScheduleAndContentImport.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document document = DocumentHelper.createDocument();
    Element root = DocumentHelper.createElement("importContentResults"); //$NON-NLS-1$
    document.add(root);
    try {
      ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(session);
      File file = new File(publishSrcPath);
      if ( !file.canRead() ) {
        throw new FileNotFoundException( "SubscriptionPublisher.publish() requires the file \""
            + publishSrcPath
            + "\" to exist. The file does not exist." );
      }
      Document importDoc = XmlDom4JHelper.getDocFromFile(file, null);
      
      root.add(subscriptionRepository.importSchedules(importDoc));
      root.add(subscriptionRepository.importContent(importDoc));
    } catch (FileNotFoundException e) {
      getLogger().error(Messages.getString("SubscriptionPublisher.ERROR_0001", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getString("SubscriptionPublisher.ERROR_0002", publishSrcPath); //$NON-NLS-1$
    } catch (DocumentException e) {
      getLogger().error(Messages.getString("SubscriptionPublisher.ERROR_0003", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getString("SubscriptionPublisher.ERROR_0004") + publishSrcPath; //$NON-NLS-1$
    } catch (IOException e) {
      getLogger().error(Messages.getString("SubscriptionPublisher.ERROR_0005", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getString("SubscriptionPublisher.ERROR_0006", publishSrcPath); //$NON-NLS-1$
    }
    
    List resultNodes = document.selectNodes("//@result"); //$NON-NLS-1$
    for (Iterator iter = resultNodes.iterator(); iter.hasNext(); ) {
      Attribute attribute = (Attribute) iter.next();
      if ("ERROR".equalsIgnoreCase(attribute.getValue())) { //$NON-NLS-1$
        return Messages.getString("SubscriptionPublisher.ERROR_0007"); //$NON-NLS-1$
      }
    }
    
    return Messages.getString("SubscriptionPublisher.INFO_0001"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IPentahoPublisher#getDescription()
   */
  public String getDescription() {
    return Messages.getString("SubscriptionPublisher.INFO_0002"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IPentahoPublisher#getName()
   */
  public String getName() {
    return Messages.getString("SubscriptionPublisher.INFO_0003"); //$NON-NLS-1$
  }

}
