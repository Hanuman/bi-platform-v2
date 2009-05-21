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

import javax.servlet.http.HttpServletRequest;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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
}
