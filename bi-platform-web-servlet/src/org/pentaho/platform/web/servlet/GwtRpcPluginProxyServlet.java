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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 21, 2009
 * @author Aaron Phillips
 */

package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.WebServiceConfig.ServiceType;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginUtil;
import org.pentaho.platform.web.servlet.messages.Messages;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;

public class GwtRpcPluginProxyServlet extends RemoteServiceServlet {

  private static final long serialVersionUID = -7652708468110168124L;

  private static final Log logger = LogFactory.getLog(GwtRpcPluginProxyServlet.class);

  private static final ThreadLocal<Object> perThreadTargetBean = new ThreadLocal<Object>();

  /**
   * Returns the dispatch key for this request.  This name is the part of the request path
   * beyond the servlet base path.  I.e. if the GwtRpcPluginProxyServlet is mapped to the "/gwtrpc"
   * context in web.xml, then this method will return "testservice" given a request to 
   * "http://localhost:8080/pentaho/gwtrpc/testservice".
   * @return the part of the request url used to dispatch the request
   */
  public String getDispatchKey() {
    HttpServletRequest request = getThreadLocalRequest();
    //path info will give us what we want with 
    String requestPathInfo = request.getPathInfo();
    if (requestPathInfo.startsWith("/")) { //$NON-NLS-1$
      requestPathInfo = requestPathInfo.substring(1);
    }
    return requestPathInfo;
  }

  @Override
  public String processCall(String payload) throws SerializationException {
    Map<Class<?>, Boolean> whitelist = new HashMap<Class<?>, Boolean>();
    whitelist.put(ServiceException.class, Boolean.TRUE);
    StandardSerializationPolicy policy = new StandardSerializationPolicy(whitelist);
    
    IServiceManager serviceManager = PentahoSystem.get(IServiceManager.class, PentahoSessionHolder.getSession());

    String serviceId = getDispatchKey();
    if (null == serviceManager.getServiceConfig(ServiceType.GWT, serviceId)) {
      String errMsg = Messages.getErrorString("GwtRpcPluginProxyServlet.ERROR_0001_SERVICE_NOT_FOUND", serviceId); //$NON-NLS-1$
      logger.error(errMsg);
      return RPC.encodeResponseForFailure(null, new ServiceException(errMsg), policy);
    }

    Object targetBean = null;
    try {
      targetBean = serviceManager.getServiceBean(ServiceType.GWT, serviceId);
    } catch (ServiceException e) {
      logger.error(
          Messages.getErrorString("GwtRpcPluginProxyServlet.ERROR_0002_FAILED_TO_GET_BEAN_REFERENCE", serviceId), e); //$NON-NLS-1$
      return RPC.encodeResponseForFailure(null, e, policy);
    }

    perThreadTargetBean.set(targetBean);

    ClassLoader origLoader = Thread.currentThread().getContextClassLoader();

    try {
      //temporarily swap out the context classloader to the plugin's classloader,
      //so the RPC class can do a Class.forName and find the service class specified
      //in the request
      Thread.currentThread().setContextClassLoader(targetBean.getClass().getClassLoader());

      RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this);
      onAfterRequestDeserialized(rpcRequest);
      // don't require the server side to implement the service interface
      Method method = rpcRequest.getMethod();
      try {
        Method m = targetBean.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (m != null) {
          method = m;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return RPC.invokeAndEncodeResponse(targetBean, method, rpcRequest.getParameters(), rpcRequest
          .getSerializationPolicy());
    } catch (IncompatibleRemoteServiceException ex) {
      logger.error(Messages.getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0003_RPC_INVOCATION_FAILED", targetBean.getClass().getName()), ex); //$NON-NLS-1$
      return RPC.encodeResponseForFailure(null, ex);
    } finally {
      //reset the context classloader
      if (origLoader != null) {
        Thread.currentThread().setContextClassLoader(origLoader);
      }
    }
  }

  @Override
  protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
      String strongName) {
    /*
     * The request path is broken up for processing like so:
     * Ex: given a request for serialization file at '/pentaho/content/data-access/resources/gwt/{strongName}'
     * 
     *  * appContextPath = '/pentaho'
     *  * servletContextPath = '/content/data-access/resources/gwt/{strongName}'
     *  * pluginContextPath = '/data-access/resources/gwt/{strongName}'
     */

    SerializationPolicy serializationPolicy = null;

    String appContextPath = request.getContextPath();

    String modulePath = null;
    if (moduleBaseURL != null) {
      try {
        modulePath = new URL(moduleBaseURL).getPath();
      } catch (MalformedURLException ex) {
        logger.error(Messages.getErrorString("GwtRpcPluginProxyServlet.ERROR_0004_MALFORMED_URL", moduleBaseURL), ex); //$NON-NLS-1$
        //cannot proceed, default serialization policy will apply
        return null;
      }
    }

    String servletContextPath = modulePath.substring(appContextPath.length());

    //We will use the pluginContextPath to determine the service plugin for the serialization policy file
    //
    String pluginServiceContextPath = servletContextPath.substring(servletContextPath.indexOf('/', 1));

    ClassLoader serviceClassloader = PluginUtil.getClassLoaderForService(ServiceType.GWT, pluginServiceContextPath);
    if (serviceClassloader == null) {
      //if we get here, then the service is not supplied by a plugin and thus we cannot hope to find
      //the appropriate serialization policy
      logger.error(Messages.getErrorString("GwtRpcPluginProxyServlet.ERROR_0005_FAILED_TO_FIND_PLUGIN", appContextPath)); //$NON-NLS-1$
    }

    String serializationPolicyFile = SerializationPolicyLoader.getSerializationPolicyFileName(strongName);

    InputStream rpcFileInputStream = null;

    //We know what plugin is supposed to have the serialization policy file, now go find it
    //in the plugin's filesystem
    //
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, PentahoSessionHolder.getSession());
    List<URL> urls = resLoader.findResources(serviceClassloader, serializationPolicyFile);
    if (urls.size() < 1) {
      logger.error(Messages.getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0006_FAILED_TO_FIND_FILE", serializationPolicyFile)); //$NON-NLS-1$
    } else if (urls.size() > 1) {
      logger
          .warn(Messages.getString("GwtRpcPluginProxyServlet.WARN_MULTIPLE_RESOURCES_FOUND", serializationPolicyFile)); //$NON-NLS-1$
    } else {
      try {
        rpcFileInputStream = urls.get(0).openStream();

        if (rpcFileInputStream != null) {
          serializationPolicy = SerializationPolicyLoader.loadFromStream(rpcFileInputStream, null);
        }

      } catch (IOException e) {
        logger.error(Messages.getErrorString(
            "GwtRpcPluginProxyServlet.ERROR_0007_FAILED_TO_OPEN_FILE", serializationPolicyFile), e); //$NON-NLS-1$
      } catch (ParseException e) {
        logger.error(Messages.getErrorString(
            "GwtRpcPluginProxyServlet.ERROR_0008_FAILED_TO_PARSE_FILE", serializationPolicyFile), e); //$NON-NLS-1$
      } finally {
        if (rpcFileInputStream != null) {
          try {
            rpcFileInputStream.close();
          } catch (IOException e) { //do nothing }
          }
        }
      }
    }

    //if null, the default serialization policy will apply
    //Note: caching is handled by the parent class
    return serializationPolicy;
  }
}
