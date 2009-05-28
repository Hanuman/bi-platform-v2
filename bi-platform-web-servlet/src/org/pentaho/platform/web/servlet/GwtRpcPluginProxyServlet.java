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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

public class GwtRpcPluginProxyServlet extends RemoteServiceServlet {

  private static final long serialVersionUID = -7652708468110168124L;

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
    if(requestPathInfo.startsWith("/")) {
      requestPathInfo = requestPathInfo.substring(1);
    }
    return requestPathInfo;
  }

  @Override
  public String processCall(String payload) throws SerializationException {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, null);
    
    String dispatchKey = getDispatchKey();
    if(!pluginManager.isBeanRegistered(dispatchKey)) {
      log("Could not locate a plugin bean to service gwt rpc request with key: "+dispatchKey);
      return RPC.encodeResponseForFailure(null, new Throwable("Could not locate a plugin bean to service gwt rpc request with key "+dispatchKey));
    }
    
    Object targetBean = null;
    try {
      targetBean = pluginManager.getBean(dispatchKey);
    } catch (PluginBeanException e) {
      log("Failed to get a reference to plugin service bean "+dispatchKey, e);
      return RPC.encodeResponseForFailure(null, e);
    }

    ClassLoader origLoader = Thread.currentThread().getContextClassLoader();

    try {
      //temporarily swap out the context classloader to the plugin's classloader,
      //so the RPC class can do a Class.forName and find the service class specified
      //in the request
      Thread.currentThread().setContextClassLoader(targetBean.getClass().getClassLoader());

      RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this);
      onAfterRequestDeserialized(rpcRequest);
      return RPC.invokeAndEncodeResponse(targetBean, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest
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
  protected SerializationPolicy doGetSerializationPolicy(
      HttpServletRequest request, String moduleBaseURL, String strongName) {
    
    
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

    SerializationPolicy serializationPolicy = null;

    /*
     * Check that the module path must be in the same web app as the servlet
     * itself. If you need to implement a scheme different than this, override
     * this method.
     */
    if (modulePath == null || !modulePath.startsWith(contextPath)) {
      String message = "ERROR: The module path requested, "
          + modulePath
          + ", is not in the same web application as this servlet, "
          + contextPath
          + ".  Your module may not be properly configured or your client and server code maybe out of date.";
      log(message, null);
    } else {
      // Strip off the context path from the module base URL. It should be a
      // strict prefix.
      String contextRelativePath = modulePath.substring(contextPath.length());

      String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(contextRelativePath
          + strongName);

      // Open the RPC resource file read its contents.
      InputStream is = getServletContext().getResourceAsStream(serializationPolicyFilePath);
      if(is == null) {
        log(serializationPolicyFilePath + " didn't work");
        is = getServletContext().getResourceAsStream(".."+serializationPolicyFilePath);
      }
      if(is == null) {
        log(".."+serializationPolicyFilePath + " didn't work");
        is = getServletContext().getResourceAsStream(serializationPolicyFilePath.substring(1));
      }
      if(is == null) {
        log(serializationPolicyFilePath.substring(1) + " didn't work");
        is = getServletContext().getResourceAsStream(contextPath+serializationPolicyFilePath);
      }
      if(is == null) {
        log(contextPath+serializationPolicyFilePath + " didn't work, giving up");
      }
      try {
        if (is != null) {
          try {
            serializationPolicy = SerializationPolicyLoader.loadFromStream(is,
                null);
          } catch (ParseException e) {
            log("ERROR: Failed to parse the policy file '"
                + serializationPolicyFilePath + "'", e);
          } catch (IOException e) {
            log("ERROR: Could not read the policy file '"
                + serializationPolicyFilePath + "'", e);
          }
        } else {
          String message = "ERROR: The serialization policy file '"
              + serializationPolicyFilePath
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
    }

    return serializationPolicy;
  }
}
