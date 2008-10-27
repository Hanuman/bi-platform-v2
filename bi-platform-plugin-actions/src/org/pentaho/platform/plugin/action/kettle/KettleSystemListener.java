/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Jan 9, 2006 
 * @author mbatchel
 */
package org.pentaho.platform.plugin.action.kettle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.plugins.PluginLoader;
import org.pentaho.di.core.plugins.PluginLocation;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class KettleSystemListener implements IPentahoSystemListener {

  public boolean startup(final IPentahoSession session) {
    /* Load the plugins etc. */
    KettleSystemListener.environmentInit(session);
    try {
      StepLoader.init();
    } catch (Throwable t) {
      t.printStackTrace();
      Logger.error(KettleSystemListener.class.getName(), Messages
          .getErrorString("KettleSystemListener.ERROR_0001_STEP_LOAD_FAILED")); //$NON-NLS-1$
    }

    try {
      JobEntryLoader.init();
    } catch (Throwable t) {
      t.printStackTrace();
      Logger.error(KettleSystemListener.class.getName(), Messages
          .getString("KettleSystemListener.ERROR_0002_JOB_ENTRY_LOAD_FAILED")); //$NON-NLS-1$
    }
    
    ISolutionFile pluginsFolder =  PentahoSystem.get(ISolutionRepository.class, session).getFileByPath("/system/kettle/plugins");//$NON-NLS-1$
	  if (pluginsFolder!=null)
	  {
		  try {
			  KettleConfig.getInstance().addConfig("platform-kettle-cfg",new PlatformConfigManager<PluginLocation>(pluginsFolder));
			  PluginLoader.getInstance().load("platform-kettle-cfg");
		  }
		  catch(KettleConfigException e) {
			 Logger.error(KettleSystemListener.class.getName(),Messages
			          .getString("KettleSystemListener.ERROR_0001_PLUGIN_LOAD_FAILED",pluginsFolder.getFullPath())); //$NON-NLS-1$
		  }
	  }
        
    return true;
  }

  public static Map readProperties(final IPentahoSession session) {

    Properties props = new Properties();
    String kettlePropsFilename = "system" + File.separator + "kettle" + File.separator + "kettle.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    InputStream is = null;
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class,session);

      if (!repository.resourceExists(kettlePropsFilename)) {
        return props;
      }
      is = repository.getResourceInputStream(kettlePropsFilename, false);
      props.load(is);
    } catch (IOException ioe) {
      Logger.error(KettleSystemListener.class.getName(), Messages
          .getString("KettleSystemListener.ERROR_0003_PROPERTY_FILE_READ_FAILED") + ioe.getMessage(), ioe); //$NON-NLS-1$
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    props.put("pentaho.solutionpath", PentahoSystem.getApplicationContext().getFileOutputPath("")); //$NON-NLS-1$ //$NON-NLS-2$
    return props;

  }

  public static void environmentInit(final IPentahoSession session) {
    EnvUtil.environmentInit();
  }

  public void shutdown() {
    // Nothing required
  }

}
