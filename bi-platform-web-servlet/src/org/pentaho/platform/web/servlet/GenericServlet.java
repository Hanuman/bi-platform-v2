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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

public class GenericServlet extends ServletBase {

  private static final long serialVersionUID = 6713118348911206464L;

  private static final Log logger = LogFactory.getLog(GenericServlet.class);

  private String settingsPath = ISolutionRepository.SEPARATOR + "settings.xml"; //$NON-NLS-1$
  private static final String CACHE_FILE = "file"; //$NON-NLS-1$
  private static ICacheManager cache = PentahoSystem.getCacheManager(null);

  static {
    cache.addCacheRegion(CACHE_FILE);
  }

  @Override
  public Log getLogger() {
    return GenericServlet.logger;
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    PentahoSystem.systemEntryPoint();

    IOutputHandler outputHandler = null;
    // BISERVER-2767 - grabbing the current class loader so we can replace it at the end
    ClassLoader origContextClassloader = Thread.currentThread().getContextClassLoader();
    try {
      InputStream in = request.getInputStream();
      String servletPath = request.getServletPath();
      String pathInfo = request.getPathInfo();
      String contentGeneratorId = ""; //$NON-NLS-1$
      String urlPath = ""; //$NON-NLS-1$
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      if (pathInfo == null) {
        contentGeneratorId = servletPath.substring(1);
        urlPath = contentGeneratorId;
      } else {
        String path = pathInfo.substring(1);
        int slashPos = path.indexOf('/');
        if (slashPos != -1) {
          pathParams.setParameter("path", pathInfo.substring(slashPos + 1)); //$NON-NLS-1$
          contentGeneratorId = path.substring(0, slashPos);
        } else {
          contentGeneratorId = path;
        }
        urlPath = "content/" + contentGeneratorId; //$NON-NLS-1$
      }
      pathParams.setParameter("query", request.getQueryString()); //$NON-NLS-1$
      pathParams.setParameter("contentType", request.getContentType()); //$NON-NLS-1$
      pathParams.setParameter("inputstream", in); //$NON-NLS-1$
      pathParams.setParameter("httpresponse", response); //$NON-NLS-1$
      pathParams.setParameter("httprequest", request); //$NON-NLS-1$
      pathParams.setParameter("remoteaddr", request.getRemoteAddr()); //$NON-NLS-1$
      if (PentahoSystem.debug) {
        debug("GenericServlet contentGeneratorId=" + contentGeneratorId); //$NON-NLS-1$
        debug("GenericServlet urlPath=" + urlPath); //$NON-NLS-1$
      }
      IPentahoSession session = getPentahoSession(request);
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, session);
      if (pluginManager == null) {
        OutputStream out = response.getOutputStream();
        String message = Messages.getErrorString("GenericServlet.ERROR_0001_BAD_OBJECT", IPluginManager.class.getSimpleName()); //$NON-NLS-1$
        error(message);
        out.write(message.getBytes());
        return;
      }

      // TODO make doing the HTTP headers configurable per content generator
      SimpleParameterProvider headerParams = new SimpleParameterProvider();
      Enumeration names = request.getHeaderNames();
      while (names.hasMoreElements()) {
        String name = (String) names.nextElement();
        String value = request.getHeader(name);
        headerParams.setParameter(name, value);
      }


      if (pathInfo != null) {
        String pluginId = pluginManager.getServicePlugin(pathInfo);
        if (pluginId != null && pluginManager.isStaticResource(pathInfo)) {
          boolean cacheOn = "true".equals(pluginManager.getPluginSetting(pluginId, "settings/cache", "false"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
          String maxAge = (String)pluginManager.getPluginSetting(pluginId, "settings/max-age", null); //$NON-NLS-1$
          allowBrowserCache(maxAge, pathParams);
          
          String mimeType = MimeHelper.getMimeTypeFromFileName(pathInfo);
          response.setContentType(mimeType);
          OutputStream out = response.getOutputStream();
  
          // do we have this resource cached?
          ByteArrayOutputStream byteStream = null;
  
          if (cacheOn) {
            byteStream = (ByteArrayOutputStream) cache.getFromRegionCache(CACHE_FILE, pathInfo);
          }
  
          if (byteStream != null) {
            IOUtils.write(byteStream.toByteArray(), out);
            return;
          }
          InputStream resourceStream = pluginManager.getStaticResource(pathInfo);
          if (resourceStream != null) {
            byteStream = new ByteArrayOutputStream();
            IOUtils.copy(resourceStream, byteStream);
  
            // if cache is enabled, drop file in cache
            if (cacheOn) {
              cache.putInRegionCache(CACHE_FILE, pathInfo, byteStream);
            }
  
            // write it out
            IOUtils.write(byteStream.toByteArray(), out);
            return;
          }
          logger.error(Messages.getErrorString("GenericServlet.ERROR_0004_RESOURCE_NOT_FOUND", pluginId, pathInfo)); //$NON-NLS-1$
          response.sendError(404);
          return;
        }
      }

      IContentGenerator contentGenerator = pluginManager.getContentGenerator(contentGeneratorId, session);
      if (contentGenerator == null) {
        OutputStream out = response.getOutputStream();
        String message = Messages.getErrorString("GenericServlet.ERROR_0002_BAD_GENERATOR", contentGeneratorId); //$NON-NLS-1$
        error(message);
        out.write(message.getBytes());
        return;
      }

      // set the classloader of the current thread to the class loader of
      // the plugin so that it can load its libraries
      Thread.currentThread().setContextClassLoader(contentGenerator.getClass().getClassLoader());

      // String proxyClass = PentahoSystem.getSystemSetting( module+"/plugin.xml" , "plugin/content-generators/"+contentGeneratorId,
      // "content generator not found");
      IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
      // see if this is an upload
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      if (isMultipart) {
        requestParameters = new SimpleParameterProvider();
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        List<?> /* FileItem */items = upload.parseRequest(request);
        Iterator<?> iter = items.iterator();
        while (iter.hasNext()) {
          FileItem item = (FileItem) iter.next();

          if (item.isFormField()) {
            ((SimpleParameterProvider) requestParameters).setParameter(item.getFieldName(), item.getString());
          } else {
            String name = item.getName();
            ((SimpleParameterProvider) requestParameters).setParameter(name, item.getInputStream());
          }
        }
      }

      response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

      IMimeTypeListener listener = new HttpMimeTypeListener(request, response);

      outputHandler = getOutputHandler(response, true);
      outputHandler.setMimeTypeListener(listener);

      IParameterProvider sessionParameters = new HttpSessionParameterProvider(session);

      Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
      parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
      parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
      parameterProviders.put("headers", headerParams); //$NON-NLS-1$
      parameterProviders.put("path", pathParams); //$NON-NLS-1$
      SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl() + urlPath + "?"); //$NON-NLS-1$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler(outputHandler);
      contentGenerator.setMessagesList(messages);
      contentGenerator.setParameterProviders(parameterProviders);
      contentGenerator.setSession(session);
      contentGenerator.setUrlFactory(urlFactory);
      // String contentType = request.getContentType();
      //	    	SimpleStreamSource input = new SimpleStreamSource( "input", contentType, in, null ); //$NON-NLS-1$
      // contentGenerator.setInput(input);
      contentGenerator.createContent();
      if (PentahoSystem.debug) {
        debug("Generic Servlet content generate successfully"); //$NON-NLS-1$
      }

    } catch (Exception e) {
      StringBuffer buffer = new StringBuffer();
      error(Messages.getErrorString("GenericServlet.ERROR_0002_BAD_GENERATOR", request.getQueryString()), e); //$NON-NLS-1$
      List errorList = new ArrayList();
      String msg = e.getMessage();
      errorList.add(msg);
      PentahoSystem.get(IMessageFormatter.class, PentahoHttpSessionHelper.getPentahoSession(request)).formatFailureMessage(
          "text/html", null, buffer, errorList); //$NON-NLS-1$
      response.getOutputStream().write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
      
    } finally {
      // reset the classloader of the current thread
      Thread.currentThread().setContextClassLoader(origContextClassloader);
      PentahoSystem.systemExitPoint();
    }
  }

  protected void allowBrowserCache(String maxAge, IParameterProvider pathParams) {
    if (maxAge == null || "0".equals(maxAge)) { //$NON-NLS-1$
      return;
    }
    if (maxAge != null) {
      HttpServletResponse response = (HttpServletResponse) pathParams.getParameter("httpresponse"); //$NON-NLS-1$
      if (response != null) {
        response.setHeader("Cache-Control", "max-age=" + maxAge); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  protected IOutputHandler getOutputHandler(HttpServletResponse response, boolean allowFeedback) throws IOException {
    OutputStream out = response.getOutputStream();
    HttpOutputHandler handler = new HttpOutputHandler(response, out, allowFeedback);
    return handler;
  }
}
