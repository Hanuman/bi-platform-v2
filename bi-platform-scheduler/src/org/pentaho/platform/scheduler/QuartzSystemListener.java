/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
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
 * @created Jul 22, 2005 
 * @author William Seyler
 * 
 */
package org.pentaho.platform.scheduler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.scheduler.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

public class QuartzSystemListener implements IPentahoSystemListener {

  protected static Scheduler schedulerInstance = null;

  private final static boolean debug = PentahoSystem.debug;
  private static boolean useNewDatasourceService = false;


  public synchronized void setUseNewDatasourceService(boolean useNewService) {
    //
    // The platform should not be calling this method. But, in case someone really 
    // really wants to use the new datasource service features to talk to
    // a core service like Quartz, this is now toggle-able. 
    //
    useNewDatasourceService = useNewService;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.IPentahoSystemListener#startup()
   */
  
  private static final String DEFAULT_QUARTZ_PROPERTIES_FILE = "quartz/quartz.properties"; //$NON-NLS-1$
  Properties quartzProperties;
  String quartzPropertiesFile = DEFAULT_QUARTZ_PROPERTIES_FILE;
  
  public boolean startup(final IPentahoSession session) {
    Properties quartzProps = null;
    if (quartzPropertiesFile != null) {
      quartzProps = PentahoSystem.getSystemSettings().getSystemSettingsProperties(quartzPropertiesFile);
    } else {
      quartzProps = quartzProperties;
    }
    if (quartzProps == null) {
      try {
        quartzProps = findPropertiesInClasspath();
      } catch (IOException ex) {
        Logger.error(QuartzSystemListener.class.getName(), Messages
            .getErrorString("QuartzSystemListener.ERROR_0004_LOAD_PROPERTIES_FROM_CLASSPATH"), ex); //$NON-NLS-1$
      }
    }
    if (quartzProps == null) {
      return false;
    }
    String dsName = quartzProps.getProperty("org.quartz.dataSource.myDS.jndiURL"); //$NON-NLS-1$
    if (dsName != null) {
      try {

        IDatasourceService datasourceService = getQuartzDatasourceService(session);
        String boundDsName = datasourceService.getDSBoundName(dsName);
        
        if (boundDsName != null) {
          quartzProps.setProperty("org.quartz.dataSource.myDS.jndiURL", boundDsName); //$NON-NLS-1$
        }
      } catch (ObjectFactoryException objface) {
      	Logger.error(this, Messages.getErrorString(
            "QuartzSystemListener.ERROR_0005_UNABLE_TO_INSTANTIATE_OBJECT",QuartzSystemListener.class.getName()), objface); //$NON-NLS-1$
      	return false;
      } catch (DatasourceServiceException dse) {
        Logger.error(this, Messages.getErrorString(
            "QuartzSystemListener.ERROR_0006_UNABLE_TO_GET_DATASOURCE",QuartzSystemListener.class.getName()), dse); //$NON-NLS-1$
        return false;        
      }
    }

    try {
      SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory(quartzProps);
      QuartzSystemListener.schedulerInstance = schedFact.getScheduler();
      if (QuartzSystemListener.debug) {
        Logger.debug(QuartzSystemListener.class.getName(), QuartzSystemListener.schedulerInstance.getSchedulerName());
      }
      QuartzSystemListener.schedulerInstance.start();
    } catch (SchedulerException e) {
      Logger.error(this, Messages.getErrorString(
          "QuartzSystemListener.ERROR_0001_Scheduler_Not_Initialized",QuartzSystemListener.class.getName()), e); //$NON-NLS-1$
      return false;        
    }
    return true;
  }

  private IDatasourceService getQuartzDatasourceService(IPentahoSession session) throws ObjectFactoryException {
    //
    // Our new datasource stuff is provided for running queries and acquiring data. It is
    // NOT there for the inner workings of the platform. So, the Quartz datasource should ALWAYS
    // be provided by JNDI. However, the class could be twiddled so that it will use the factory. 
    //
    // And, since the default shipping condition should be to NOT use the factory (and force JNDI), 
    // I've reversed the logic in the class to have the negative condition first (the default execution
    // path).
    //
    // Marc - BISERVER-2004
    //
    if (!useNewDatasourceService) {
      return new JndiDatasourceService();
    } else {
      IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,session);
      return datasourceService;
    }
  }
  
  private Properties findPropertiesInClasspath() throws IOException {
    // Do my best to find the properties file...
    File propFile = new File("quartz.properties"); //$NON-NLS-1$
    if (!propFile.exists()) {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("quartz.properties"); //$NON-NLS-1$
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        return props;
      }
      return null; // Couldn't find properties file.
    } else {
      InputStream iStream = new BufferedInputStream(new FileInputStream(propFile));
      Properties props = new Properties();
      props.load(iStream);
      return props;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.IPentahoSystemListener#shutdown()
   */
  public void shutdown() {
    try {
      QuartzSystemListener.schedulerInstance.shutdown(true);
      QuartzSystemListener.schedulerInstance = null;
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @return Returns the schedulerInstance.
   * @throws Exception
   */
  public static Scheduler getSchedulerInstance() throws SchedulerInitializationException {
    if (QuartzSystemListener.schedulerInstance == null) {
      throw new SchedulerInitializationException(Messages
          .getErrorString("QuartzSystemListener.ERROR_0001_Scheduler_Not_Initialized")); //$NON-NLS-1$
    }
    return QuartzSystemListener.schedulerInstance;
  }

  public Properties getQuartzProperties() {
    return quartzProperties;
  }

  public void setQuartzProperties(Properties quartzProperties) {
    this.quartzProperties = quartzProperties;
    if (quartzProperties != null) {
      quartzPropertiesFile = null;
    }
  }

  public String getQuartzPropertiesFile() {
    return quartzPropertiesFile;
  }

  public void setQuartzPropertiesFile(String quartzPropertiesFile) {
    this.quartzPropertiesFile = quartzPropertiesFile;
    if (quartzPropertiesFile != null) {
      quartzProperties = null;
    }
  }
  
  
}
