package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.logging.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Framework for Spring-based object factories.  Subclasses are required only to implement
 * the init method, which is responsible for setting the {@link ApplicationContext}.
 * 
 * TODO remove the custom logic in {@link #retreiveObject(String, IPentahoSession)} and use
 * a custom Spring scope to handle any session types that Spring does not handle out-of-the-box,
 * such as {@link StandaloneSession}.  In order to do this, we need a way to access the
 * current {@link IPentahoSession} from a static context (perhaps a ThreadLocal).
 * 
 * @author Aaron Phillips
 */
public abstract class AbstractSpringPentahoObjectFactory implements IPentahoObjectFactory {

  protected ApplicationContext beanFactory;

  public <T> T get(Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException {
    return get(interfaceClass, interfaceClass.getSimpleName(), session);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> interfaceClass, String key, final IPentahoSession session) throws ObjectFactoryException {
    return (T) retreiveObject(key, session);
  }

  public Object getObject(String key, final IPentahoSession session) throws ObjectFactoryException {
    return retreiveObject(key, session);
  }

  protected Object instanceClass(String key) throws ObjectFactoryException {
    Object object = null;
    try {
      object = beanFactory.getType(key).newInstance();
    } catch (Exception e) {
      throw new ObjectFactoryException("Could not create an instance of object with key ["+key+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return object;
  }

  protected Object retrieveViaSpring(String beanId) throws ObjectFactoryException {
    Object object = null;
    try {
      object = beanFactory.getBean(beanId);
    } catch (Throwable t) {
      throw new ObjectFactoryException("Could not retrieve object with key ["+beanId+"]",t); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return object;
  }

  protected Object retreiveObject(String key, final IPentahoSession session) throws ObjectFactoryException {
    //cannot access logger here since this object factory provides the logger
    Logger.debug(this, "Attempting to get an instance of [" + key + "] while in session [" + session + "]");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    Object object = null;

    if (session != null && session instanceof StandaloneSession) {
      //first ask Spring for the object, if it is session scoped it will fail
      //since Spring doesn't know about StandaloneSessions
      try {
        object = beanFactory.getBean(key);
      } catch (Throwable t) {
        //Spring could not create the object, perhaps due to session scoping, let's try
        //retrieving it from our internal session map
        Logger.debug(this, "Retrieving object from Pentaho session map (not Spring).");   //$NON-NLS-1$

        object = session.getAttribute(key);

        if ((object == null)) {
          //our internal session map doesn't have it, let's create it
          object = instanceClass(key);
          session.setAttribute(key, object);
        }
      }
    } else {
      //Spring can handle the object retrieval since we are not dealing with StandaloneSession
      object = retrieveViaSpring(key);
    }

    //FIXME: what is this doing here??
    if (object instanceof IPentahoInitializer) {
      ((IPentahoInitializer) object).init(session);
    }
    //FIXME: hack to support null IPluginSetting's
    if (object instanceof String) {
      object = null;
    }

    Logger.debug(this, " got an instance of [" + key + "]: " + object);   //$NON-NLS-1$ //$NON-NLS-2$
    return object;
  }

  public boolean objectDefined(String key) {
    return beanFactory.containsBean(key);
  }

  @SuppressWarnings("unchecked")
  public Class getImplementingClass(String key) {
    return beanFactory.getType(key);
  }
}