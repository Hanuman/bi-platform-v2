/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.google.gwt.user.server.rpc.RPCServletUtils;

public class DebugMantleServlet extends DebugRemoteServiceServlet {

  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    // use HTTPClient to forward on the data to whatever server we want
    // eg. http://localhost:8080/pentaho/MantleService
    // 1. set the contentType
    // 2. add the data
    // 3. tack the response onto our response
    try {
      HttpClient client = new HttpClient();

      // If server userid/password was supplied, use basic authentication to
      // authenticate with the server.
      Credentials creds = new UsernamePasswordCredentials("joe", "password");
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);

//      Enumeration attributes = req.getAttributeNames();
//      while (attributes.hasMoreElements()) {
//        System.out.println("Attribute: " + attributes.nextElement());
//      }
//
//      Enumeration params = req.getParameterNames();
//      while (params.hasMoreElements()) {
//        System.out.println("Parameter: " + params.nextElement());
//      }
//
//      Enumeration headers = req.getHeaderNames();
//      while (headers.hasMoreElements()) {
//        String headerName = (String) headers.nextElement();
//        String headerValue = req.getHeader(headerName).replaceAll("8888", "8080");
//        System.out.println("Header: " + headerName + "=" + headerValue);
//        if (!headerName.equals("accept-encoding") && !headerName.equals("content-type") && !"content-length".equals(headerName)) {
//          postMethod.setRequestHeader(headerName, headerValue);
//        }
//      }

      
      String requestPayload = RPCServletUtils.readContentAsUtf8(req);
      System.out.println("INCOMING: " + requestPayload);
      requestPayload = requestPayload.replaceAll("8888", "8080");
      
      PostMethod postMethod = null;
      if (requestPayload.indexOf("MantleLoginService") != -1) {
        postMethod = new PostMethod("http://localhost:8080/pentaho/mantleLogin/MantleLoginService");
      } else if (requestPayload.indexOf("MantleService") != -1) {
        postMethod = new PostMethod("http://localhost:8080/pentaho/mantle/MantleService");
      }
      requestPayload = requestPayload.replaceAll("org.pentaho.mantle.MantleApplication", "pentaho/mantle");
      requestPayload = requestPayload.replaceAll("org.pentaho.mantle.login.MantleLogin", "pentaho/mantleLogin");
      
      StringRequestEntity stringEntity = new StringRequestEntity(requestPayload, "text/x-gwt-rpc", "UTF-8");
      postMethod.setRequestEntity(stringEntity);

      try {
        int status = client.executeMethod(postMethod);
        String postResult = postMethod.getResponseBodyAsString();
        resp.getOutputStream().write(postResult.getBytes("UTF-8"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // use HTTPClient to forward on the data to whatever server we want
    // eg. http://localhost:8080/pentaho/MantleService
    // 1. set the contentType
    // 2. add the data
    // 3. tack the response onto our response
    try {
      HttpClient client = new HttpClient();
      GetMethod getMethod = null;

      String passthru = req.getParameter("passthru");

      if (passthru.equals("SolutionRepositoryService")) {
        getMethod = new GetMethod("http://localhost:8080/pentaho/SolutionRepositoryService");
        getMethod.setQueryString(req.getQueryString());
      } else {
        // not known
        resp.setStatus(404);
        return;
      }

      // If server userid/password was supplied, use basic authentication to
      // authenticate with the server.
      Credentials creds = new UsernamePasswordCredentials("joe", "password");
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);

      try {
        int status = client.executeMethod(getMethod);
        String postResult = getMethod.getResponseBodyAsString();
        resp.getOutputStream().write(postResult.getBytes("UTF-8"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
    }

  }
}