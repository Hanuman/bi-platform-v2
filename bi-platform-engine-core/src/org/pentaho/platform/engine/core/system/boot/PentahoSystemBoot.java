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
package org.pentaho.platform.engine.core.system.boot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

/**
 * This class is designed to help embedded deployments start the Pentaho system
 * @author jamesdixon
 *
 */
public class PentahoSystemBoot {

  // the object factory
  private IPentahoObjectFactory objectFactory = new StandaloneObjectFactory();

  // list of the system listeners to hook up
  private List<IPentahoSystemListener> lifecycleListeners = new ArrayList<IPentahoSystemListener>();
  
  // list of startup actions to execute
  private List<ISessionStartupAction> startupActions = new ArrayList<ISessionStartupAction>();
  
  // list of admin plugins (aka publishers)
  private List<IPentahoPublisher> adminActions = new ArrayList<IPentahoPublisher>();
  
  private String filePath;
  
  private ISystemSettings settingsProvider = null;
  
  private boolean initialized = false;
  
  private String defaultFilePath = "."; //$NON-NLS-1$
  
  public PentahoSystemBoot( ) {
    setupDefaults();
  }
  
  /**
   * Sets up the defaults:
   * Override this method to create a different set of defaults or
   * use the 'add' methods to override defaults on a case by case
   * basis
   */
  protected void setupDefaults() {
    
    filePath = new File( defaultFilePath ).getAbsolutePath();
  }

  /**
   * Sets the file path to be used to find configuration and content files
   * If this is not set the current directory (.) is used.
   * @param filePath
   */
  public void setFilePath( final String filePath ) {
    this.filePath = filePath;
  }
  
  /**
   * Override this method if you want to change the type and state of the application
   * context used to initialize the system.
   * @return an application context for system initialization
   */
  protected IApplicationContext createApplicationContext() {
    return new StandaloneApplicationContext(filePath, ""); //$NON-NLS-1$
  }
  
  /**
   * Starts the Pentaho platform using the defaults and options set
   * @return
   */
  public boolean start() {
    PentahoSystem.setObjectFactory( objectFactory );
    PentahoSystem.setSystemListeners( lifecycleListeners );
    PentahoSystem.setSystemSettingsService( settingsProvider );
    PentahoSystem.setSessionStartupActions(startupActions);
    PentahoSystem.setAdministrationPlugins(adminActions);
    // initialize the system
    initialized = false;
    try {
      initialized = PentahoSystem.init( createApplicationContext() );
    } catch (Exception e) {
      e.printStackTrace();
    }

    return initialized;
  }

  /**
   * Stops the Pentaho platform
   * @return
   */
  public boolean stop() {
    initialized = false;
    PentahoSystem.shutdown();
    return true;
  }
  
  /**
   * Gets the object factory for the Pentaho platform
   * @return
   */
  public IPentahoObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * Sets the object factory for the Pentaho platform, This defaults to the
   * StandaloneObjectFactory
   * @return
   */
  public void setObjectFactory( IPentahoObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
    //object factory needs to also be early here so clients that do not need to
    //run the platform can have an object factory available
    PentahoSystem.setObjectFactory( objectFactory );
  }
  
  /**
   * Adds an administrative action to the system.
   * @param adminAction
   */
  public void addAdminAction(final IPentahoPublisher adminAction) {
    adminActions.add(adminAction);
  }
  
  public void setAdminActions(final List<IPentahoPublisher> adminActions) {
    this.adminActions = adminActions;
  }

  /**
   * Adds a lifecycle listener. This object will be notified when the Pentaho platform
   * starts and stops.
   * @param lifecycleListener
   */
  public void addLifecycleListener( final IPentahoSystemListener lifecycleListener ) {
    lifecycleListeners.add( lifecycleListener );
  }
  
  /**
   * Returns the list of lifecycle listeners that will be used.
   * These objects will be notified when the Pentaho platform
   * starts and stops.
   * @return
   */
  public List<IPentahoSystemListener> getLifecycleListeners() {
    return lifecycleListeners;
  }

  /**
   * Returns the list of lifecycle listeners that will be used.
   * These objects will be notified when the Pentaho platform
   * starts and stops.
   * @return
   */
  public void setLifecycleListeners(final List<IPentahoSystemListener> lifecycleListeners) {
    this.lifecycleListeners = lifecycleListeners;
  }

  /**
   * Gets the system settings object that will be used by the Pentaho platform
   * @return
   */
  public ISystemSettings getSettingsProvider() {
    return settingsProvider;
  }

  /**
   * Sets the system settings object that will be used by the Pentaho platform
   * @return
   */
  public void setSettingsProvider(final ISystemSettings settingsProvider) {
    this.settingsProvider = settingsProvider;
  }

  /**
   * Sets the file path that will be used to get to file-based
   * resources
   * @return
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Returns true if the Pentaho platform has initialized successfully.
   * @return
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Returns the list of startup actions.
   * These actions will be executed on system startup or on session creation.
   * @return
   */
  public List<ISessionStartupAction> getStartupActions() {
    return startupActions;
  }

  /**
   * Sets the list of startup actions
   * These actions will be executed on system startup or on session creation.
   * @param startupActions
   */
  public void setStartupActions(final List<ISessionStartupAction> startupActions) {
    this.startupActions = startupActions;
  }

  /**
   * Adds a strtup action to the system.
   * These actions will be executed on system startup or on session creation.
   * @param startupAction
   */
  public void addStartupAction( final ISessionStartupAction startupAction ) {
    startupActions.add( startupAction );
  }

  /**
   * Define an arbitrarily scoped object
   * @param key  the key to retrieval of this object
   * @param implClassName  the actual type that is served back to you when requested.
   * @param scope  the scope of the object
   * @return  the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError if the object factory does not support runtime object definition 
   */
  public PentahoSystemBoot define(String key, String implClassName, Scope scope) {
    if( objectFactory instanceof IPentahoDefinableObjectFactory ) {
      IPentahoDefinableObjectFactory factory = (IPentahoDefinableObjectFactory) objectFactory;
      factory.defineObject( key, implClassName, scope ); 
    } else {
      throw new NoSuchMethodError("define is only supported by IPentahoDefinableObjectFactory"); //$NON-NLS-1$
    }
    return this;
  }
  
  /**
   * Define an arbitrarily scoped object
   * @param interfaceClass  the key to retrieval of this object
   * @param implClass  the actual type that is served back to you when requested.
   * @param scope  the scope of the object
   * @return  the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError if the object factory does not support runtime object definition
   */
  public PentahoSystemBoot define(Class<?> interfaceClass, Class<?> implClass, Scope scope) {
    return define(interfaceClass.getSimpleName(), implClass.getName(), scope);
  }
  
  /**
   * Define an arbitrarily scoped object
   * @param key  the key to retrieval of this object
   * @param implClass  the actual type that is served back to you when requested.
   * @param scope  the scope of the object
   * @return  the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError if the object factory does not support runtime object definition
   */
  public PentahoSystemBoot define(String key, Class<?> implClass, Scope scope) {
    return define(key, implClass.getName(), scope);
  }
  
}
