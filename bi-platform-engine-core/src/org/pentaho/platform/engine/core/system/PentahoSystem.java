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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created May 6, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.core.system;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections.list.UnmodifiableList;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IAclPublisher;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.IScheduler;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISubscriptionScheduler;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.api.ui.IXMLComponent;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;


public class PentahoSystem {

  public static final boolean debug = true;

  public static final boolean trace = false;

  public static final boolean ignored = false; // used to suppress compiler

  public static int loggingLevel = ILogger.ERROR;

  private static IApplicationContext applicationContext;

  protected static final String CONTENT_REPOSITORY = "IContentRepository"; //$NON-NLS-1$

  protected static final String RUNTIME_REPOSITORY = "IRuntimeRepository"; //$NON-NLS-1$

  private static final String SOLUTION_REPOSITORY = "ISolutionRepository"; //$NON-NLS-1$

  protected static final String SOLUTION_ENGINE = "ISolutionEngine"; //$NON-NLS-1$

  public static final String BACKGROUND_EXECUTION = "IBackgroundExecution"; //$NON-NLS-1$
  
  // TODO: Read the Conditional Execution information from Pentaho XML.
  public static final String CONDITIONAL_EXECUTION = "IConditionalExecution"; //$NON-NLS-1$
  public static String DEFAULT_CONDITIONAL_EXECUTION_PROVIDER;
  public static String DEFAULT_MESSAGE_FORMATTER;
  public static String DEFAULT_NAVIGATION_COMPONENT;

  
  // TODO: Read the Scheduler Class from Pentaho XML.
  public static final String SCHEDULER = "IScheduler"; //$NON-NLS-1$

  public static final String MESSAGE_FORMATTER = "IMessageFormatter"; //$NON-NLS-1$
  
  public static final String NAVIGATION_COMPONENT = "INavigationComponent"; //$NON-NLS-1$
  
  public static final String SCOPE_GLOBAL = "global"; //$NON-NLS-1$

  public static final String SCOPE_SESSION = "session"; //$NON-NLS-1$

  public static final String SCOPE_THREAD = "thread"; //$NON-NLS-1$

  public static final String SCOPE_LOCAL = "local"; //$NON-NLS-1$

  public static final String SCOPE = "scope"; //$NON-NLS-1$

  public static final String PENTAHO_SESSION_KEY = "pentaho-session-context"; //$NON-NLS-1$

  /**
   * maps an interface name to an instance of a class that implements the interface connectionClassNameMap, connectionScopeMap, and globalConnectionsMap are related Map<String, Object>
   */
  private static final Map globalConnectionsMap = Collections.synchronizedMap(new HashMap());

  private static Map globalAttributes;

  private static SimpleParameterProvider globalParameters;

  private static ISystemSettings systemSettings;

  private static List<IPentahoPublisher> publishers = new ArrayList<IPentahoPublisher>();

  private static List<IPentahoSystemListener> listeners = new ArrayList<IPentahoSystemListener>();
  
  private static List<ISessionStartupAction> sessionStartupActions = new ArrayList<ISessionStartupAction>();
  
  /**
   * Maps an interface name to the scope as defined in the pentaho.xml file connectionClassNameMap, connectionScopeMap, and globalConnectionsMap are related
   */
  private static Map connectionScopeMap;

  /**
   * maps interface name to a class name that implements the interface connectionClassNameMap, connectionScopeMap, and globalConnectionsMap are related Map<String, String>
   */
  private static Map connectionClassNameMap;

  private static final Map initializationFailureDetailsMap = new HashMap();

//  private static IRuntimeRepository runtimeRepository;

  private static IContentRepository contentRepository;

  private static final List<String> RequiredObjects = new ArrayList<String>();

  private static final List<String> KnownOptionalObjects = new ArrayList<String>();

  private static final List<String> IgnoredObjects = new ArrayList<String>();

  public static final int SYSTEM_NOT_INITIALIZED = -1;

  public static final int SYSTEM_INITIALIZED_OK = 0;

  public static final int SYSTEM_LISTENERS_FAILED = (int) Math.pow(2, 0); // 1

  public static final int SYSTEM_OBJECTS_FAILED = (int) Math.pow(2, 1); // 2

  public static final int SYSTEM_PUBLISHERS_FAILED = (int) Math.pow(2, 2); // 4

  public static final int SYSTEM_AUDIT_FAILED = (int) Math.pow(2, 3); // 8

  public static final int SYSTEM_PENTAHOXML_FAILED = (int) Math.pow(2, 4); // 16

  public static final int SYSTEM_SETTINGS_FAILED = (int) Math.pow(2, 5); // 32

  private static int initializedStatus = PentahoSystem.SYSTEM_NOT_INITIALIZED;

  private static final String SUBSCRIPTION_REPOSITORY = "ISubscriptionRepository"; //$NON-NLS-1$

  private static final String SUBSCRIPTION_SCHEDULER = "ISubscriptionScheduler"; //$NON-NLS-1$

  private static final String USERSETTING_SERVICE = "IUserSettingService"; //$NON-NLS-1$
  
  private static final String ACL_PUBLISHER = "IAclPublisher"; //$NON-NLS-1$

  private static final String ACL_VOTER = "IAclVoter"; //$NON-NLS-1$

  private static final String CACHE_MANAGER = "ICacheManager"; //$NON-NLS-1$

  private static final String CONNECTION_PREFIX = "connection-"; //$NON-NLS-1$

  // private static Map globalConnectionsMap = Collections.synchronizedMap(new
  // HashMap());
  private static IUserDetailsRoleListService userDetailsRoleListService;

  private static final List ACLFileExtensionList = new ArrayList();

  private static final List UnmodifiableACLFileExtensionList = UnmodifiableList
      .decorate(PentahoSystem.ACLFileExtensionList);

  private static final List logoutListeners = Collections.synchronizedList(new ArrayList());

  // private static ISolutionRepository solutionRepository;

  // TODO even if logging is not configured messages need to make it out to
  // the console

  static {
    PentahoSystem.RequiredObjects.add(PentahoSystem.SOLUTION_ENGINE);

    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.SOLUTION_REPOSITORY);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.ACL_VOTER);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.CONDITIONAL_EXECUTION);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.RUNTIME_REPOSITORY);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.SUBSCRIPTION_SCHEDULER);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.SUBSCRIPTION_REPOSITORY);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.CACHE_MANAGER);

    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.CONTENT_REPOSITORY);
    PentahoSystem.KnownOptionalObjects.add("IUITemplater"); //$NON-NLS-1$
    PentahoSystem.KnownOptionalObjects.add("IUserFilesComponent"); //$NON-NLS-1$
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.BACKGROUND_EXECUTION);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.SCHEDULER);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.MESSAGE_FORMATTER);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.NAVIGATION_COMPONENT);
    PentahoSystem.KnownOptionalObjects.add(PentahoSystem.USERSETTING_SERVICE);

    PentahoSystem.IgnoredObjects.add("IAuditEntry"); //$NON-NLS-1$
  }

  public static synchronized boolean retrySystemInit() {
    IApplicationContext appContext = PentahoSystem.applicationContext;
    PentahoSystem.connectionScopeMap.clear();
    PentahoSystem.globalAttributes.clear();
    PentahoSystem.globalParameters = null;
    PentahoSystem.connectionScopeMap = null;
    PentahoSystem.globalAttributes = null;
    return PentahoSystem.init(appContext);
  }

  public static boolean init(final IApplicationContext pApplicationContext) {
    return PentahoSystem.init(pApplicationContext, null);
  }

  public static boolean init(final IApplicationContext pApplicationContext, final Map listenerMap) {
    PentahoSystem.initializedStatus = PentahoSystem.SYSTEM_INITIALIZED_OK;

    PentahoSystem.connectionScopeMap = new HashMap();
    PentahoSystem.connectionClassNameMap = new HashMap();
    PentahoSystem.globalAttributes = Collections.synchronizedMap(new HashMap());
    PentahoSystem.globalParameters = new SimpleParameterProvider(PentahoSystem.globalAttributes);

    PentahoSystem.applicationContext = pApplicationContext;

    String propertyPath = PentahoSystem.applicationContext.getSolutionPath(""); //$NON-NLS-1$
    propertyPath = propertyPath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("pentaho.solutionpath", propertyPath); //$NON-NLS-1$

    if (LocaleHelper.getLocale() == null) {
      LocaleHelper.setLocale(Locale.getDefault());
    }
    
    PentahoSystem.systemSettings = new PathBasedSystemSettings();

    // test to see if we have a valid document
    String test = PentahoSystem.getSystemSetting("pentaho-system", null);//$NON-NLS-1$ 
    if( test == null ) {
    	return false;
    }
    
    // Set Up ACL File Extensions by reading pentaho.xml for acl-files
    //
    // Read the files that are permitted to have ACLs on them from
    // the pentaho.xml.
    //
    String aclFiles = PentahoSystem.getSystemSetting("acl-files", "xaction,url");//$NON-NLS-1$ //$NON-NLS-2$
    StringTokenizer st = new StringTokenizer(aclFiles, ","); //$NON-NLS-1$
    String extn;
    while (st.hasMoreElements()) {
      extn = st.nextToken();
      if (!extn.startsWith(".")) { //$NON-NLS-1$
        extn = "." + extn; //$NON-NLS-1$
      }
      PentahoSystem.ACLFileExtensionList.add(extn);
    }

    DEFAULT_CONDITIONAL_EXECUTION_PROVIDER = PentahoSystem.systemSettings.getSystemSetting(
        "objects/IConditionalExecution", //$NON-NLS-1$
        "org.pentaho.platform.plugin.condition.javascript.ConditionalExecution"); //$NON-NLS-1$    

    DEFAULT_MESSAGE_FORMATTER = PentahoSystem.systemSettings.getSystemSetting("objects/IMessageFormatter", //$NON-NLS-1$
        "org.pentaho.platform.engine.services.MessageFormatter"); //$NON-NLS-1$    
    
    DEFAULT_NAVIGATION_COMPONENT = PentahoSystem.systemSettings.getSystemSetting("objects/INavigationComponent", //$NON-NLS-1$
        "org.pentaho.platform.uifoundation.component.xml.NavigationComponent"); //$NON-NLS-1$  
    
    List settingsList = PentahoSystem.systemSettings.getSystemSettings("pentaho-system"); //$NON-NLS-1$
    if (null == settingsList) {
      // the application context is not configure correctly
      Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
          "PentahoSystem.ERROR_0001_SYSTEM_SETTINGS_INVALID", PentahoSystem.systemSettings.getSystemCfgSourceName())); //$NON-NLS-1$
      PentahoSystem.initializedStatus |= PentahoSystem.SYSTEM_SETTINGS_FAILED;
      PentahoSystem.addInitializationFailureMessage(PentahoSystem.SYSTEM_SETTINGS_FAILED, Messages
          .getErrorString("PentahoSystem.ERROR_0001_SYSTEM_SETTINGS_INVALID")); //$NON-NLS-1$
    }

    PentahoSystem.initXMLFactories();

    // Check the audit log class to make sure it's there.
    String auditClass = PentahoSystem.systemSettings.getSystemSetting(
        "objects/IAuditEntry", "org.pentaho.platform.engine.core.audit.AuditFileEntry"); //$NON-NLS-1$ //$NON-NLS-2$
    if (!PentahoSystem.checkClassExists(auditClass)) {
      PentahoSystem.initializedStatus |= PentahoSystem.SYSTEM_OBJECTS_FAILED | PentahoSystem.SYSTEM_AUDIT_FAILED;
      String msg = Messages.getErrorString("PentahoSystem.ERROR_0020_SPECIFIED_CLASS_NOT_FOUND", //$NON-NLS-1$
          null == auditClass ? "unknown" : auditClass); //$NON-NLS-1$
      Logger.error(PentahoSystem.class.getName(), msg);
      PentahoSystem.addInitializationFailureMessage(PentahoSystem.SYSTEM_OBJECTS_FAILED, msg);
      return false;
    }

    StandaloneSession session = new StandaloneSession("system session"); //$NON-NLS-1$
    PentahoSystem.loggingLevel = Logger
        .getLogLevel(PentahoSystem.systemSettings.getSystemSetting("log-level", "ERROR")); //$NON-NLS-1$//$NON-NLS-2$
    Logger.setLogLevel(PentahoSystem.loggingLevel);

    // to guarantee hostnames in SSL mode are not being spoofed
    PentahoSystem.registerHostnameVerifier();

    // boolean isOk = true;


    // get a list of the connection providers
    // TODO move some ofthis code to the connection provider factory
    List connectionNodes = PentahoSystem.systemSettings.getSystemSettings("connections/*"); //$NON-NLS-1$
    if (connectionNodes != null) {
      Iterator connectionIterator = connectionNodes.iterator();
      while (connectionIterator.hasNext()) {
    	  Element node = (Element) connectionIterator.next();
    	  String connectionClass = node.getText();
    	  String connectionKey = CONNECTION_PREFIX + node.getName();
    	  String connectionScope = node.attributeValue( PentahoSystem.SCOPE );
        connectionScopeMap.put(connectionKey, connectionScope);
        connectionClassNameMap.put(connectionKey, connectionClass);
    	  if( PentahoSystem.SCOPE_GLOBAL.equals( connectionScope ) ) {
    	      Object obj = PentahoSystem.createObject(connectionClass);
          globalConnectionsMap.put(connectionKey, obj);
    	  }
      }
    }

    assert null != pentahoObjectFactory : "pentahoObjectFactory must be non-null"; //$NON-NLS-1$
    try {
      PentahoSystem.validateObjectFactory();
    } catch (PentahoSystemException e1) {
      throw new RuntimeException( e1 ); // this is fatal
    }
    PentahoSystem.loadGlobalObjects();
    PentahoSystem.sessionStartup( session, false, null );
    
    // store a list of the system listeners
    try {
      PentahoSystem.notifySystemListenersOfStartup(session);
    } catch (PentahoSystemException e) {
      String msg = e.getLocalizedMessage();
      Logger.error(PentahoSystem.class.getName(), msg, e);
      PentahoSystem.initializedStatus |= PentahoSystem.SYSTEM_LISTENERS_FAILED;
      PentahoSystem.addInitializationFailureMessage(PentahoSystem.SYSTEM_LISTENERS_FAILED, msg);
      return false;
    }

    return true;
  }

  private static void notifySystemListenersOfStartup(IPentahoSession session) throws PentahoSystemException {
	if(listeners != null) {
	    for (IPentahoSystemListener systemListener : listeners) {
	      try {
	        if (!systemListener.startup(session)) {
	          throw new PentahoSystemException(Messages.getErrorString(
	              "PentahoSystem.ERROR_0014_STARTUP_FAILURE", systemListener.getClass().getName())); //$NON-NLS-1$
	        }
	      } catch (Throwable e) {
	        throw new PentahoSystemException(Messages.getErrorString(
	            "PentahoSystem.ERROR_0014_STARTUP_FAILURE", systemListener.getClass().getName()), e); //$NON-NLS-1$
	      }
	    }
	}
  }
  

  /**
   * Using data in the systemSettings (this data typically originates in the pentaho.xml file), initialize 3 System properties to explicitly identify the Transformer, SAX, and DOM factory implementations. (i.e. Crimson, Xerces, Xalan,
   * Saxon, etc.)
   * 
   * For background on the purpose of this method, take a look at the notes/URLs below:
   * 
   * Java[tm] API for XML Processing (JAXP):Frequently Asked Questions http://java.sun.com/webservices/jaxp/reference/faqs/index.html
   * 
   * Plugging in a Transformer and XML parser http://xml.apache.org/xalan-j/usagepatterns.html#plug
   * 
   * http://marc2.theaimsgroup.com/?l=xml-apache-general&m=101344910514822&w=2 Q. How do I use a different JAXP compatible implementation?
   * 
   * The JAXP 1.1 API allows applications to plug in different JAXP compatible implementations of parsers or XSLT processors. For example, when an application wants to create a new JAXP DocumentBuilderFactory instance, it calls the staic
   * method DocumentBuilderFactory.newInstance(). This causes a search for the name of a concrete subclass of DocumentBuilderFactory using the following order: - The value of a system property like javax.xml.parsers.DocumentBuilderFactory
   * if it exists and is accessible. - The contents of the file $JAVA_HOME/jre/lib/jaxp.properties if it exists. - The Jar Service Provider mechanism specified in the Jar File Specification. A jar file can have a resource (i.e. an embedded
   * file) such as META-INF/javax/xml/parsers/DocumentBuilderFactory containing the name of the concrete class to instantiate. - The fallback platform default implementation.
   * 
   * Of the above ways to specify an implementation, perhaps the most useful is the jar service provider mechanism. To use this mechanism, place the implementation jar file on your classpath. For example, to use Xerces 1.4.4 instead of the
   * version of Crimson which is bundled with JDK 1.4 (Java Development Kit version 1.4), place xerces.jar in your classpath. This mechanism also works with older versions of the JDK which do not bundle JAXP. If you are using JDK 1.4 and
   * above, see the following question for potential problems. see http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
   * 
   */
  private static void initXMLFactories() {
    // assert systemSettings != null : "systemSettings property must be set
    // before calling initXMLFactories.";

    String xpathToXMLFactoryNodes = "xml-factories/factory-impl"; //$NON-NLS-1$
    List nds = PentahoSystem.systemSettings.getSystemSettings(xpathToXMLFactoryNodes);
    if (null != nds) {
      for (Iterator it = nds.iterator(); it.hasNext();) {
        Node nd = (Node) it.next();
        Node nameAttr = nd.selectSingleNode("@name"); //$NON-NLS-1$
        Node implAttr = nd.selectSingleNode("@implementation"); //$NON-NLS-1$
        if ((null != nameAttr) && (null != implAttr)) {
          String name = nameAttr.getText();
          String impl = implAttr.getText();
          System.setProperty(name, impl);
        } else {
          Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
              "PentahoSystem.ERROR_0025_LOAD_XML_FACTORY_PROPERTIES_FAILED", //$NON-NLS-1$ 
              xpathToXMLFactoryNodes));
        }
      }
    }
  }

  public static boolean getInitializedOK() {
    return PentahoSystem.initializedStatus == PentahoSystem.SYSTEM_INITIALIZED_OK;
  }

  public static int getInitializedStatus() {
    return PentahoSystem.initializedStatus;
  }

  private static List getAdditionalInitializationFailureMessages(final int failureBit) {
    List l = (List) PentahoSystem.initializationFailureDetailsMap.get(new Integer(failureBit));
    return l;
  }

  public static List getInitializationFailureMessages() {
    List rtn = new ArrayList();
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_SETTINGS_FAILED)) {
      rtn.add(Messages.getString(
          "PentahoSystem.USER_INITIALIZATION_SYSTEM_SETTINGS_FAILED", PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_SETTINGS_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_PUBLISHERS_FAILED)) {
      rtn.add(Messages.getString("PentahoSystem.USER_INITIALIZATION_SYSTEM_PUBLISHERS_FAILED"));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_PUBLISHERS_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_OBJECTS_FAILED)) {
      rtn.add(Messages.getString("PentahoSystem.USER_INITIALIZATION_SYSTEM_OBJECTS_FAILED"));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_OBJECTS_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_AUDIT_FAILED)) {
      rtn.add(Messages.getString("PentahoSystem.USER_INITIALIZATION_SYSTEM_AUDIT_FAILED"));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_AUDIT_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_LISTENERS_FAILED)) {
      rtn.add(Messages.getString("PentahoSystem.USER_INITIALIZATION_SYSTEM_LISTENERS_FAILED"));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_LISTENERS_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    if (PentahoSystem.hasFailed(PentahoSystem.SYSTEM_PENTAHOXML_FAILED)) {
      rtn.add(Messages.getString("PentahoSystem.USER_INITIALIZATION_SYSTEM_PENTAHOXML_FAILED"));//$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages(PentahoSystem.SYSTEM_PENTAHOXML_FAILED);
      if (l != null) {
        rtn.addAll(l);
      }
    }
    return rtn;
  }

  public static synchronized void addInitializationFailureMessage(final int failureBit, final String message) {
    Integer i = new Integer(failureBit);
    List l = (List) PentahoSystem.initializationFailureDetailsMap.get(i);
    if (l == null) {
      l = new ArrayList();
      PentahoSystem.initializationFailureDetailsMap.put(i, l);
    }
    l.add("&nbsp;&nbsp;&nbsp;" + message);//$NON-NLS-1$
  }

  private static final boolean hasFailed(final int errorToCheck) {
    return ((PentahoSystem.initializedStatus & errorToCheck) == errorToCheck);
  }

  protected static boolean checkClassExists(final String className) {
    try {
      Class.forName(className);
      return true;
    } catch (Throwable t) {
    }
    return false;
  }

  // TODO sbarkdull, creating the default version helper is handled in the factory
  // it no longer needs to be handled here
  public static IVersionHelper getVersionHelper(final IPentahoSession session) {
    return (IVersionHelper)PentahoSystem.getObject(session, "IVersionHelper"); //$NON-NLS-1$
  }

  public static IUITemplater getUITemplater(final IPentahoSession session) {
      try {
      return (IUITemplater)pentahoObjectFactory.getObject( "IUITemplater", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
        }
        }

  public static IBackgroundExecution getBackgroundExecutionHandler(final IPentahoSession session) {
    try {
      return (IBackgroundExecution)pentahoObjectFactory.getObject( "IBackgroundExecution", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
      }
    }

  public static IXMLComponent getUserFilesComponent(final IPentahoSession session) {
    try {
      return (IXMLComponent)pentahoObjectFactory.getObject( "IUserFilesComponent", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }

  public static IContentOutputHandler getOutputHandlerFromHandlerId(final String objectName,
      final IPentahoSession session) {
    try {
      return (IContentOutputHandler)pentahoObjectFactory.getObject( objectName, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }

  public static IContentOutputHandler getOutputDestinationFromContentRef(final String contentTag,
      final IPentahoSession session) {

    int pos = contentTag.indexOf(':');
    if (pos == -1) {
      Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
          "PentahoSystem.ERROR_0029_OUTPUT_HANDLER_NOT_SPECIFIED", contentTag)); //$NON-NLS-1$
      return null;
    }
    String handlerId = contentTag.substring(0, pos);
    String contentRef = contentTag.substring(pos + 1);
    IContentOutputHandler output = PentahoSystem.getOutputHandlerFromHandlerId(handlerId, session);
    if (output != null) {
      output.setHandlerId(handlerId);
      output.setSession(session);
      output.setContentRef(contentRef);
    }
    return output;
  }

  public static Object getObject(final IPentahoSession session, final String objectName) {
    try {
      return pentahoObjectFactory.getObject( objectName, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
    return null;
      }
    }

  /**
   * Identifies if the class that implements the interface (<code>intefaceName</code>) should be created. More generally, identifies if <code>interfaceName</code> is in any of the lists
   * <code>RequiredObjects, KnownOptionalObjects, or IgnoredObjects</code>.
   * 
   * @param interfaceName
   *          String identifying the name of an interface
   * @return boolean set to true if <code>interfaceName</code> is NOT in any of the lists <code>RequiredObjects, KnownOptionalObjects, or IgnoredObjects</code>.
   */
  private static boolean shouldLoad(final String interfaceName) {
    return ((!PentahoSystem.RequiredObjects.contains(interfaceName))
        && (!PentahoSystem.KnownOptionalObjects.contains(interfaceName)) && (!PentahoSystem.IgnoredObjects
        .contains(interfaceName)));
  }

  public static String getSystemName() {
    return Messages.getString("PentahoSystem.USER_SYSTEM_TITLE"); //$NON-NLS-1$;
  }

  public static IParameterProvider getGlobalParameters() {
    return PentahoSystem.globalParameters;
  }

  public static void sessionStartup(final IPentahoSession session, final IParameterProvider sessionParameters) {
	    PentahoSystem.sessionStartup(session, true, sessionParameters);
	  }

  public static void sessionStartup(final IPentahoSession session) {
	    PentahoSystem.sessionStartup(session, true, null);
	  }

  public static void clearGlobals() {
    PentahoSystem.globalAttributes.clear();
  }

  public static Object putInGlobalAttributesMap(final Object key, final Object value) {
    return PentahoSystem.globalAttributes.put(key, value);
  }

  public static Object removeFromGlobalAttributesMap(final Object key) {
    return PentahoSystem.globalAttributes.remove(key);
  }

  public static void sessionStartup(final IPentahoSession session, final boolean doSession ) {
	  PentahoSystem.sessionStartup( session, doSession, null );
	  
  }

  public static void sessionStartup(final IPentahoSession session, boolean doSession,
      IParameterProvider sessionParameters) {

    List<ISessionStartupAction> sessionStartupActions = PentahoSystem.getSessionStartupActions(session);
    if (sessionStartupActions == null) {
      // nothing to do...
      return;
    }

    if (!session.isAuthenticated() && doSession) {
      return;
    }
    
    boolean doGlobals = PentahoSystem.globalAttributes.size() == 0;
    // TODO this needs more validation
    if(sessionStartupActions != null) {
	    for (ISessionStartupAction sessionStartupAction : sessionStartupActions) {
	      if (PentahoSystem.SCOPE_GLOBAL.equals(sessionStartupAction.getActionOutputScope()) && !doGlobals) {
	        // see if this has been done already
	        continue;
	      } else if (SCOPE_SESSION.equals(sessionStartupAction.getActionOutputScope()) && !doSession) {
	        continue;
	      }
	      // parse the actionStr out to identify an action
	      ActionInfo actionInfo = ActionInfo.parseActionString(sessionStartupAction.getActionPath());
	      if (actionInfo != null) {
	        // now execute the action...
	
	        SimpleOutputHandler outputHandler = null;
	
	        String instanceId = null;
	
	        ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
	        solutionEngine.setLoggingLevel(PentahoSystem.loggingLevel);
	        solutionEngine.init(session);
	
	        String baseUrl = ""; //$NON-NLS-1$	
	        HashMap parameterProviderMap = new HashMap();
	        if( sessionParameters == null ) {
	            sessionParameters = new PentahoSessionParameterProvider(session);
	        }
	
	        parameterProviderMap.put(SCOPE_SESSION, sessionParameters);
	
	        IPentahoUrlFactory urlFactory = new SimpleUrlFactory(baseUrl);
	
	        ArrayList messages = new ArrayList();
	
	        IRuntimeContext context = null;
	        try {
	          context = solutionEngine
	              .execute(
	                  actionInfo.getSolutionName(),
	                  actionInfo.getPath(),
	                  actionInfo.getActionName(),
	                  "Session startup actions", false, true, instanceId, false, parameterProviderMap, outputHandler, null, urlFactory, messages); //$NON-NLS-1$
	
	          if (context.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
	            // now grab any outputs
	            Iterator outputNameIterator = context.getOutputNames().iterator();
	            while (outputNameIterator.hasNext()) {
	
	              String attributeName = (String) outputNameIterator.next();
	              IActionParameter output = context.getOutputParameter(attributeName);
	
	              Object data = output.getValue();
	              if (data != null) {
	                if (SCOPE_SESSION.equals(sessionStartupAction.getActionOutputScope())) {
	                  session.removeAttribute(attributeName);
	                  session.setAttribute(attributeName, data);
	                } else if (PentahoSystem.SCOPE_GLOBAL.equals(sessionStartupAction.getActionOutputScope())) {
	                  PentahoSystem.globalAttributes.remove(attributeName);
	                  PentahoSystem.globalAttributes.put(attributeName, data);
	                } else {
	                  Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
	                      "PentahoSystem.ERROR_0024_BAD_SCOPE_SYSTEM_ACTION", sessionStartupAction.getActionOutputScope())); //$NON-NLS-1$
	                }
	              }
	            }
	          }
	        } finally {
	          if (context != null) {
	            context.dispose();
	          }
	        }
	
	      } else {
	        Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
	            "PentahoSystem.ERROR_0016_COULD_NOT_PARSE_ACTION", sessionStartupAction.getActionPath())); //$NON-NLS-1$
	      }
	    }
    }
  }

  public static void shutdown() {
    if (LocaleHelper.getLocale() == null) {
      LocaleHelper.setLocale(Locale.getDefault());
    }
    if (PentahoSystem.listeners != null) {
      Iterator systemListenerIterator = PentahoSystem.listeners.iterator();
      while (systemListenerIterator.hasNext()) {
        IPentahoSystemListener listener = (IPentahoSystemListener) systemListenerIterator.next();
        if (listener != null) {
          try {
            listener.shutdown();
          } catch (Throwable e) {
            Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
                "PentahoSystem.ERROR_0015_SHUTDOWN_FAILURE", listener.getClass().getName()), e); //$NON-NLS-1$
          }
        }
      }
    }
  }

  public static IApplicationContext getApplicationContext() {
    return PentahoSystem.applicationContext;
  }

  public static ISolutionEngine getSolutionEngineInstance(final IPentahoSession session) {
    try {
      return (ISolutionEngine)pentahoObjectFactory.getObject( "ISolutionEngine", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
      }
      }

  public static IContentRepository getContentRepository(final IPentahoSession session) {
      try {
      return (IContentRepository)pentahoObjectFactory.getObject( "IContentRepository", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
    }
  }

  public static ISolutionRepository getSolutionRepository(final IPentahoSession session) {
    try {
      return (ISolutionRepository)pentahoObjectFactory.getObject( "ISolutionRepository", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
    }
  }

  public static IRuntimeRepository getRuntimeRepository(final IPentahoSession session) {
    try {
      return (IRuntimeRepository)pentahoObjectFactory.getObject( "IRuntimeRepository", session ); //$NON-NLS-1$
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
      }
      }

  public static IAclPublisher getAclPublisher(final IPentahoSession session) {
    try {
      return (IAclPublisher)pentahoObjectFactory.getObject( PentahoSystem.ACL_PUBLISHER, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
      }
    }

  public static IAclVoter getAclVoter(final IPentahoSession session) {
    try {
      return (IAclVoter)pentahoObjectFactory.getObject( PentahoSystem.ACL_VOTER, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
    return null;
  }
  }

  public static ISubscriptionRepository getSubscriptionRepository(final IPentahoSession session) {
    try {
      return (ISubscriptionRepository)pentahoObjectFactory.getObject( PentahoSystem.SUBSCRIPTION_REPOSITORY, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
  }
  }

  public static ISubscriptionScheduler getSubscriptionScheduler( final IPentahoSession session ) {
    try {
     return (ISubscriptionScheduler)pentahoObjectFactory.getObject( PentahoSystem.SUBSCRIPTION_SCHEDULER, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }

  public static IUserSettingService getUserSettingService(final IPentahoSession session) {
    try {
      return (IUserSettingService)pentahoObjectFactory.getObject( PentahoSystem.USERSETTING_SERVICE, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
        return null;
    }
  }  
  
  public static Object createObject(final String className, final ILogger logger) {

    Object object = null;
    try {

      Class componentClass = Class.forName(className.trim());
      object = componentClass.newInstance();

    } catch (Throwable t) {
      String msg = Messages.getErrorString("PentahoSystem.ERROR_0013_COULD_NOT_CREATE_OBEJCT", className); //$NON-NLS-1$
      if (null == logger) {
        Logger.fatal(PentahoSystem.class.getName(), msg, t);
      } else {
        logger.fatal(msg, t);
      }
    }
    return object;
  }

  public static void setUserDetailsRoleListService(final IUserDetailsRoleListService value) {
    PentahoSystem.userDetailsRoleListService = value;
  }

  public static IUserDetailsRoleListService getUserDetailsRoleListService() {
    return PentahoSystem.userDetailsRoleListService;
  }

  public static Object createObject(final String className) {

    return PentahoSystem.createObject(className, null);
  }

  public static String getSystemSetting(final String path, final String settingName, final String defaultValue) {
    return PentahoSystem.systemSettings.getSystemSetting(path, settingName, defaultValue);
  }

  public static String getSystemSetting(final String settingName, final String defaultValue) {
    // TODO make this more efficient using caching
    return PentahoSystem.systemSettings.getSystemSetting(settingName, defaultValue);
  }

  public static ISystemSettings getSystemSettings() {
    return PentahoSystem.systemSettings;
  }

  public static void refreshSettings() {
    PentahoSystem.systemSettings.resetSettingsCache();
  }

  public static String publish(final IPentahoSession session, final String className) {
    Iterator publisherIterator = PentahoSystem.publishers.iterator();
    // TODO: audit this
    while (publisherIterator.hasNext()) {
      IPentahoPublisher publisher = (IPentahoPublisher) publisherIterator.next();
      if ((publisher != null) && ((className == null) || className.equals(publisher.getClass().getName()))) {
        try {
          return publisher.publish(session, PentahoSystem.loggingLevel);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }
    return Messages.getErrorString("PentahoSystem.ERROR_0017_PUBLISHER_NOT_FOUND"); //$NON-NLS-1$
  }

  public static List getPublisherList() {
    return new ArrayList(PentahoSystem.publishers);
  }

  public static Document getPublishersDocument() {

    Document document = DocumentHelper.createDocument();
    Element root = document.addElement("publishers"); //$NON-NLS-1$
    if(publishers != null) {
	    Iterator publisherIterator = PentahoSystem.publishers.iterator();
	    // TODO: audit this
	    // refresh the system settings
	    while (publisherIterator.hasNext()) {
	      IPentahoPublisher publisher = (IPentahoPublisher) publisherIterator.next();
	      if (publisher != null) {
	        try {
	          Element publisherNode = root.addElement("publisher"); //$NON-NLS-1$
	          publisherNode.addElement("name").setText(publisher.getName()); //$NON-NLS-1$
	          publisherNode.addElement("description").setText(publisher.getDescription()); //$NON-NLS-1$
	          publisherNode.addElement("class").setText(publisher.getClass().getName()); //$NON-NLS-1$
	
	        } catch (Throwable e) {
	
	        }
	      }
	    }
    }
    return document;

  }

  public static void systemEntryPoint() {
    if (PentahoSystem.applicationContext != null) {
      PentahoSystem.applicationContext.invokeEntryPoints();
    }
  }

  public static void systemExitPoint() {
    if (PentahoSystem.applicationContext != null) {
      PentahoSystem.applicationContext.invokeExitPoints();
    }
  }

  private static void registerHostnameVerifier() {
    try {
      final String LOCALHOST = "localhost"; //$NON-NLS-1$
      String tmphost = "localhost"; //$NON-NLS-1$
      try {
        String baseURL = PentahoSystem.getApplicationContext().getBaseUrl();
        if (null == baseURL) {
          return;
        }
        URL url = new URL(baseURL);
        tmphost = url.getHost();
      } catch (MalformedURLException e) {
        // TODO sbarkdull, localize
        Logger.warn(PentahoSystem.class.getName(),
            Messages.getErrorString("PentahoSystem.ERROR_0030_VERIFIER_FAILED"), e); //$NON-NLS-1$

      }
      final String host = tmphost;

      javax.net.ssl.HostnameVerifier myHv = new javax.net.ssl.HostnameVerifier() {
        public boolean verify(String hostName, javax.net.ssl.SSLSession session) {
          if (hostName.equals(host) || hostName.equals(LOCALHOST)) {
            return true;
          }
          return false;
        }
      };
      javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(myHv);
    } catch (Throwable t) {
      Logger
          .warn(PentahoSystem.class.getName(), Messages.getErrorString("PentahoSystem.ERROR_0030_VERIFIER_FAILED"), t); //$NON-NLS-1$
    }
  }

  // Security Helpers

  public static ICacheManager getCacheManager( IPentahoSession session ) {
    try {
    // TODO get the SimpleMapCacheManager into the object map somehow
    	// we will try to use a simple map cache manager if one has not been configured
    	ICacheManager cacheManager = (ICacheManager)pentahoObjectFactory.getObject( PentahoSystem.CACHE_MANAGER, session );
      return cacheManager;
    } catch (ObjectFactoryException e) {
    	ICacheManager cacheManager = SimpleMapCacheManager.getInstance();
      Logger.warn( PentahoSystem.class.getName(), "Using default cache manager" );
      return cacheManager;
    }
  }

  public static List getACLFileExtensionList() {
    return PentahoSystem.UnmodifiableACLFileExtensionList;
  }

  // Stuff for the logout listener subsystem
  public static void addLogoutListener(final ILogoutListener listener) {
    // add items to vector of listeners
    if (PentahoSystem.logoutListeners.contains(listener)) {
      return;
    }
    PentahoSystem.logoutListeners.add(listener);
  }

  public static ILogoutListener remove(final ILogoutListener listener) {
    if (PentahoSystem.logoutListeners.remove(listener)) {
      return listener;
    }
    return null;
  }

  public static void invokeLogoutListeners(final IPentahoSession session) {
    Iterator iter = PentahoSystem.logoutListeners.iterator();
    while (iter.hasNext()) {
      ILogoutListener listener = (ILogoutListener) iter.next();
      listener.onLogout(session);
    }
  }

  public static IScheduler getScheduler(IPentahoSession session) {
    try {
      return (IScheduler)pentahoObjectFactory.getObject( PentahoSystem.SCHEDULER, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }

  public static IConditionalExecution getConditionalExecutionHandler(IPentahoSession session) {
    try {
      return (IConditionalExecution)pentahoObjectFactory.getObject( PentahoSystem.CONDITIONAL_EXECUTION, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }
  
  public static IMessageFormatter getMessageFormatter(IPentahoSession session) {
    try {
      return (IMessageFormatter)pentahoObjectFactory.getObject( PentahoSystem.MESSAGE_FORMATTER, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }
  
  public static INavigationComponent getNavigationComponent(IPentahoSession session) {
    try {
      return (INavigationComponent)pentahoObjectFactory.getObject( PentahoSystem.NAVIGATION_COMPONENT, session );
    } catch (ObjectFactoryException e) {
      Logger.error( PentahoSystem.class.getName(), e.getMessage() );
      return null;
    }
  }
  
  public static IPentahoConnection getConnection( String datasourceType, IPentahoSession session, ILogger logger ) {
	  String key = CONNECTION_PREFIX+datasourceType;
    String scope = (String) PentahoSystem.connectionScopeMap.get(key);
	    if ( SCOPE_GLOBAL.equalsIgnoreCase(scope)) {
      return (IPentahoConnection) PentahoSystem.globalConnectionsMap.get(key);
	    } else if ( SCOPE_SESSION.equalsIgnoreCase(scope)) {
	      if (session == null) {
        Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
            "PentahoSystem.ERROR_0026_COULD_NOT_CREATE_CONNECTION", datasourceType)); //$NON-NLS-1$
	        return null;
	      }
	      Object attribute = session.getAttribute( key );
	      if ((attribute != null) && (attribute instanceof IPentahoConnection)) {
	        // Set the session which is a threadlocal...
	        return (IPentahoConnection) attribute;
	      }
      String connectionClass = (String) PentahoSystem.connectionClassNameMap.get(key);
	      Object obj = PentahoSystem.createObject(connectionClass);
	      if ((obj == null) || !(obj instanceof IPentahoConnection)) {
        Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
            "PentahoSystem.ERROR_0026_COULD_NOT_CREATE_CONNECTION", connectionClass)); //$NON-NLS-1$
	        return null;
	      }
	      IPentahoConnection connection = (IPentahoConnection) obj;
	      session.setAttribute(key, connection);
	      if( connection instanceof IPentahoLoggingConnection ) {
		      ((IPentahoLoggingConnection)connection).setLogger(logger);
	      }
	      return connection;
	    } else if ( SCOPE_LOCAL.equalsIgnoreCase(scope)) {
      String connectionClass = (String) PentahoSystem.connectionClassNameMap.get(key);
		      Object obj = PentahoSystem.createObject(connectionClass);
		      if ((obj == null) || !(obj instanceof IPentahoConnection)) {
        Logger.error(PentahoSystem.class.getName(), Messages.getErrorString(
            "PentahoSystem.ERROR_0026_COULD_NOT_CREATE_CONNECTION", connectionClass)); //$NON-NLS-1$
		        return null;
		      }
		      IPentahoConnection connection = (IPentahoConnection) obj;
		      if( connection instanceof IPentahoLoggingConnection ) {
			      ((IPentahoLoggingConnection)connection).setLogger(logger);
		      }
		      return connection;
	    }
	    
	    
	    return null;
  }

  private static IPentahoObjectFactory pentahoObjectFactory = null;
  public static void setObjectFactory( IPentahoObjectFactory pentahoObjectFactory ) {
    PentahoSystem.pentahoObjectFactory = pentahoObjectFactory;
  }

  static List<IPentahoPublisher> getAdministrationPlugins() {
    return publishers;
  }

  static void setAdministrationPlugins(List<IPentahoPublisher> administrationPlugins) {
    publishers = administrationPlugins;
  }
  
	// All these methods are transitional.
  static List<IPentahoSystemListener> getSystemListeners() {
    return listeners;
  }

  static void setSystemListeners(List<IPentahoSystemListener> systemListeners) {
    listeners = systemListeners;
  }

  static List<ISessionStartupAction> getSessionStartupActions() {
    return sessionStartupActions;
  }
  
  static void setSessionStartupActions(List<ISessionStartupAction> actions) {
    sessionStartupActions = actions;
  }
  
  private static List<ISessionStartupAction> getSessionStartupActions(IPentahoSession session) {
    ArrayList<ISessionStartupAction> startupActions = new ArrayList<ISessionStartupAction>();
    String sessionClassName = session.getClass().getName();
    if(sessionStartupActions != null) {
	    for (ISessionStartupAction sessionStartupAction : sessionStartupActions) {
	      if (sessionStartupAction.getSessionType().equals(sessionClassName)) {
	        startupActions.add(sessionStartupAction);
	      }
	    }
    }
    return startupActions;
  }
  // End of transitional methods.
  
  /**
   * Make sure all required objects exist in the object factory. If not,
   * throw an exception. If any optional objects are missing, simply log it
   * to the logger.
   * 
   * @throws PentahoSystemException if a required object is missing.
   */
  private static void validateObjectFactory() throws PentahoSystemException {
    boolean isRequiredValid = true;
    for ( String interfaceName : PentahoSystem.RequiredObjects ) {
      boolean isValid = pentahoObjectFactory.hasObject( interfaceName );
      isRequiredValid &= isValid;
      if ( !isValid ) {
        Logger.fatal(PentahoSystem.class.getName(), Messages.getErrorString(
            "PentahoSystem.ERROR_0021_OBJECT_NOT_SPECIFIED", interfaceName )); //$NON-NLS-1$
      }
    }
    for ( String interfaceName : PentahoSystem.KnownOptionalObjects ) {
      boolean isValid = pentahoObjectFactory.hasObject( interfaceName );
      if ( !isValid ) {
        Logger.warn(PentahoSystem.class.getName(), Messages.getErrorString(
            "PentahoSystem.ERROR_0021_OBJECT_NOT_SPECIFIED", interfaceName )); //$NON-NLS-1$
      }
    }
    if ( !isRequiredValid ) {
      throw new PentahoSystemException( Messages.getErrorString("PentahoSystem.ERROR_0420_MISSING_REQUIRED_OBJECT") ); //$NON-NLS-1$
    }
  }
  
  private static void loadGlobalObjects() {
    
  }
  public static IPentahoObjectFactory getObjectFactory() {
	    return pentahoObjectFactory;
  }

}