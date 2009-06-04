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

import javax.servlet.http.HttpServletRequest;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.webservices.PentahoSessionHolder;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

public class GwtRpcPluginProxyServlet extends RemoteServiceServlet {

  private static final long serialVersionUID = -7652708468110168124L;

  private static final ThreadLocal<Object> perThreadTargetBean = new ThreadLocal<Object>();

  /**
   * Returns the dispatch key for this request.  This name is the last part of the request path
   * beyond the servlet base path.  I.e. if the GwtRpcPluginProxyServlet is mapped to the "/gwtrpc"
   * context in web.xml, then this method will return "testservice" given a request to 
   * "http://localhost:8080/pentaho/gwtrpc/testservice".
   * @return the part of the request url used to dispatch the request
   */
  public String getDispatchKey() {
    HttpServletRequest request = getThreadLocalRequest();
    //path info will give us what we want with 
    String requestPathInfo = request.getPathInfo();
    if (requestPathInfo.startsWith("/")) {
      requestPathInfo = requestPathInfo.substring(1);
    }
    return requestPathInfo;
  }

  @Override
  public String processCall(String payload) throws SerializationException {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, null);

    String dispatchKey = getDispatchKey();
    if (!pluginManager.isBeanRegistered(dispatchKey)) {
      log("Could not locate a plugin bean to service gwt rpc request with key: " + dispatchKey);
      return RPC.encodeResponseForFailure(null, new Throwable(
          "Could not locate a plugin bean to service gwt rpc request with key " + dispatchKey));
    }

    Object targetBean = null;
    try {
      targetBean = pluginManager.getBean(dispatchKey);
    } catch (PluginBeanException e) {
      log("Failed to get a reference to plugin service bean " + dispatchKey, e);
      return RPC.encodeResponseForFailure(null, e);
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
      } catch (Exception e ) {
    	  e.printStackTrace();
      }
      return RPC.invokeAndEncodeResponse(targetBean, method, rpcRequest.getParameters(), rpcRequest
          .getSerializationPolicy());
    } catch (IncompatibleRemoteServiceException ex) {
      log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
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

    // The request can tell you the path of the web app relative to the
    // container root.
    String contextPath = request.getContextPath();

    String modulePath = null;
    if (moduleBaseURL != null) {
      try {
        modulePath = new URL(moduleBaseURL).getPath();
      } catch (MalformedURLException ex) {
        // log the information, we will default
        log("Malformed moduleBaseURL: " + moduleBaseURL, ex);
      }
    }
    
    String contextRelativePath = modulePath.substring(contextPath.length());

    SerializationPolicy serializationPolicy = null;
    
    //get the content generator id from the request so we can track down the plugin related to the client
    //NOTE: this technique requires all clients of gwtrpc plugin beans to be plugins themselves, otherwise we cannot
    //locate their serialization policy files
    String relativePath = contextRelativePath.substring(contextRelativePath.indexOf('/', 1)); //remove '/content' from the beginning of the path
    String contentGeneratorId = relativePath.substring(1, relativePath.indexOf('/', 1)); //exact the first element of the path
    //now lookup the plugin by the content generator id
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, null);
    IContentGenerator aPluginClass = null;
    try {
      // TODO: fix me
      // this assumes that the path maps to a content generator, not a static type.
      aPluginClass = pluginManager.getContentGenerator(contentGeneratorId, PentahoSessionHolder.getSession());
      if (aPluginClass == null && contentGeneratorId.endsWith("-res")) {
    	  aPluginClass = pluginManager.getContentGenerator(contentGeneratorId.substring(0, contentGeneratorId.length() - 4), PentahoSessionHolder.getSession());
      }
    } catch (ObjectFactoryException e1) {
      log("could not find a content generator by id '"+contentGeneratorId+"'", e1);
    }
    
    //now that we have the plugin class, we can load the resources in that plugin
    String resourceDir = relativePath.substring(contentGeneratorId.length()+2); // leaves us with e.g. 'resources/gwt/'
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, PentahoSessionHolder.getSession());
    String serializationPolicyPath = SerializationPolicyLoader.getSerializationPolicyFileName(resourceDir + strongName);
    InputStream is = resLoader.getResourceAsStream(aPluginClass.getClass(), serializationPolicyPath);
    if (is == null) {
      log("failed to get serialization policy file '" + serializationPolicyPath + "' from the plugin resource loader");
      // look in the resources folder
      is = resLoader.getResourceAsStream(aPluginClass.getClass(), "resources/" + serializationPolicyPath);
    }

    try {
      if (is != null) {
        try {
          serializationPolicy = SerializationPolicyLoader.loadFromStream(is, null);
        } catch (ParseException e) {
          log("PTO ERROR: Failed to parse the policy file '" + serializationPolicyPath + "'", e);
        } catch (IOException e) {
          log("PTO ERROR: Could not read the policy file '" + serializationPolicyPath + "'", e);
        }
      } else {
        String message = "PTO ERROR: The serialization policy file '" + serializationPolicyPath
            + "' was not found; did you forget to include it in this deployment?";
        log(message, null);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Ignore this error
        }
      }
    }

    return serializationPolicy;
  }
}
