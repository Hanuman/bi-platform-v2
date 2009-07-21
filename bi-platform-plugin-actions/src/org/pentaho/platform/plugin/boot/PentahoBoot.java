/*
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
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
 * Created Feb 4, 2009 
 * @author jdixon
 */
package org.pentaho.platform.plugin.boot;

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledOrJndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.jfreereport.JFreeReportSystemListener;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.platform.plugin.action.mondrian.MondrianSystemListener;
import org.pentaho.platform.plugin.outputs.FileOutputHandler;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.xquery.XQConnection;
import org.pentaho.platform.plugin.services.metadata.MetadataDomainRepository;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginAdapter;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;

/**
 * This class is designed to help embedded deployments start the Pentaho system
 * @author jamesdixon
 *
 */
public class PentahoBoot extends PentahoSystemBoot {

  public PentahoBoot( ) {
    super();
  }
  
  /**
   * Sets up the defaults:
   * - File-based repository
   * - SQL datasource connections
   * - MXL datasources
   * - File outputs
   */
  @Override
  protected void configure(String solutionPath, String baseUrl, IPentahoDefinableObjectFactory factory) {
    super.configure(null, null, null);
    IPentahoObjectFactory objectFactory = getFactory();
    if( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( ISolutionEngine.class, SolutionEngine.class, Scope.LOCAL );
      define( "systemStartupSession" , StandaloneSession.class, IPentahoDefinableObjectFactory.Scope.GLOBAL ); //$NON-NLS-1$
      define( ISolutionRepository.class, FileBasedSolutionRepository.class, Scope.SESSION );
      define( "connection-XML", XQConnection.class, Scope.LOCAL ); //$NON-NLS-1$
      define( "connection-SQL", SQLConnection.class, Scope.LOCAL ); //$NON-NLS-1$
      define( "file", FileOutputHandler.class, Scope.LOCAL ); //$NON-NLS-1$
    }
  }
  
  /**
   * Enables the components necessary to create reports
   */
  public void enableReporting() {
    addLifecycleListener( new JFreeReportSystemListener() );
  }
  
  /**
   * Enables the components necessary to create reports
   */
  public void enableOlap() {
    IPentahoObjectFactory objectFactory = getFactory();
    if( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define( "connection-MDX", MDXConnection.class.getName(), Scope.LOCAL ); //$NON-NLS-1$
    }
    addLifecycleListener( new MondrianSystemListener() );
  }

  /**
   * Enables the plugin manager
   */
  public void enablePluginManager() {
    if( getFactory() instanceof IPentahoDefinableObjectFactory ) {
      define(IPluginProvider.class, SystemPathXmlPluginProvider.class, Scope.GLOBAL );
      define(IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
      define(IServiceManager.class, DefaultServiceManager.class, Scope.GLOBAL );
      define(IPluginResourceLoader.class, PluginResourceLoader.class, Scope.GLOBAL );
    }
    addLifecycleListener( new PluginAdapter() );
    
  }
  
  /**
   * Enables the pooled datasources
   */
  public void enablePooledDatasources() {
    IPentahoObjectFactory objectFactory = getFactory();
    if( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define(IDatasourceService.class, PooledOrJndiDatasourceService.class, Scope.LOCAL );
    }
    addLifecycleListener( new PooledDatasourceSystemListener() );
  }
  
  /**
   * Enables the metadata services
   */
  public void enableMetadata() {
    IPentahoObjectFactory objectFactory = getFactory();
    if( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      define(IMetadataDomainRepository.class, MetadataDomainRepository.class, Scope.GLOBAL);
    }
  }
  
  /**
   * Enables the components necessary to create reports
   */
  public void enableDataIntegration() {
    addLifecycleListener( new KettleSystemListener() );
  }
  
}
