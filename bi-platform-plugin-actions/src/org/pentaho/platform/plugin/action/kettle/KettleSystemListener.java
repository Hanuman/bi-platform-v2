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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
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
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class KettleSystemListener implements IPentahoSystemListener {

  public boolean startup(final IPentahoSession session) {
    
    hookInDataSourceProvider();
    
    /* Load the plugins etc. */
    KettleSystemListener.environmentInit(session);
    
    try {
      // StepLoader is using the old method of loading plugins
      StepLoader.init(new String[]{PentahoSystem.getApplicationContext().getSolutionPath("system/kettle/plugins/steps")}); //$NON-NLS-1$
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
    
    //Load plugins for jobs using the new method
    String pluginPath = PentahoSystem.getApplicationContext().getSolutionPath("system/kettle/plugins/jobentries"); //$NON-NLS-1$
    
	  if (pluginPath!=null)
	  {
		  try {
			  KettleConfig.getInstance().addConfig("platform-kettle-cfg",new PlatformConfigManager<PluginLocation>(pluginPath)); //$NON-NLS-1$
			  PluginLoader.getInstance().load("platform-kettle-cfg"); //$NON-NLS-1$
		  }
		  catch(KettleConfigException e) {
			 Logger.error(KettleSystemListener.class.getName(),Messages
			          .getString("KettleSystemListener.ERROR_0001_PLUGIN_LOAD_FAILED", pluginPath)); //$NON-NLS-1$
		  }
	  }
        
    return true;
  }

  private void hookInDataSourceProvider() {
    try {
      Class clazz = Class.forName("org.pentaho.di.core.database.DataSourceProviderInterface"); //$NON-NLS-1$
      PlatformKettleDataSourceProvider.hookupProvider();
    } catch (Exception ignored) {
      // if here, then it's because we're running with an older
      // kettle.
    }
  }
  
  public static Map readProperties(final IPentahoSession session) {

    Properties props = new Properties();
    String kettlePropsFilename = "system" + File.separator + "kettle" + File.separator + "kettle.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    InputStream is = null;
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class,session);

      if (!repository.resourceExists(kettlePropsFilename, ISolutionRepository.ACTION_EXECUTE)) {
        return props;
      }
      is = repository.getResourceInputStream(kettlePropsFilename, false, ISolutionRepository.ACTION_EXECUTE);
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
