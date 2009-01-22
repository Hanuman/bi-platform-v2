/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
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
 * @created Jul 12, 2005 
 * @author James Dixon, Angelo Rodriguez, Steven Barkdull
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.jfreereport.castormodel.jfree.types.AlignmentEnum;
import org.pentaho.jfreereport.castormodel.jfree.types.PageFormats;
import org.pentaho.jfreereport.castormodel.reportspec.Field;
import org.pentaho.jfreereport.castormodel.reportspec.ReportSpec;
import org.pentaho.jfreereport.wizard.utility.CastorUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportGenerationUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportSpecUtility;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.services.WebServiceUtil;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.pms.factory.CwmSchemaFactoryInterface;
import org.pentaho.pms.mql.MQLQuery;
import org.pentaho.pms.mql.MQLQueryFactory;
import org.pentaho.pms.schema.BusinessColumn;
import org.pentaho.pms.schema.BusinessModel;
import org.pentaho.pms.schema.concept.ConceptInterface;
import org.pentaho.pms.schema.concept.ConceptPropertyInterface;
import org.pentaho.pms.schema.concept.DefaultPropertyID;
import org.pentaho.pms.schema.concept.types.ConceptPropertyType;
import org.pentaho.pms.schema.concept.types.alignment.AlignmentSettings;
import org.pentaho.pms.schema.concept.types.color.ColorSettings;
import org.pentaho.pms.schema.concept.types.columnwidth.ColumnWidth;
import org.pentaho.pms.schema.concept.types.datatype.DataTypeSettings;
import org.pentaho.pms.schema.concept.types.font.FontSettings;


/*
 * Refactoring notes:
 * Break out code into facade classes for SolutionRepository, WaqrRepository
 * For each of the methods in the switch statement in the dispatch method,
 * create a method that takes the parameter provider. These methods will
 * break the parameters out of the parameter provider, and call methods by
 * the same name
 */
/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction" web.servlet-mapping url-pattern="/ViewAction" web.servlet-init-param name="A parameter" value="A value"
 */
public class AdhocWebService extends ServletBase {

  /**
   * 
   * BISERVER-2735 update - moved logic into the MQLRelationalDataComponent because it needs to be
   * there anyway. So, I have reverted this to the same as the 34974 revision (except for this comment).
   * 
   */
  private static final long serialVersionUID = -2011812808062152707L;

  private static final Log logger = LogFactory.getLog(AdhocWebService.class);

  private static final String WAQR_EXTENSION = "waqr"; //$NON-NLS-1$
  private static final Field DEFAULT_FIELD_PROPS = new Field();

  // for the string bart.waqr.xreportspec, return "bart"
  private static final String RE_GET_BASE_WAQR_FILENAME = "^(.*)\\.waqr\\.xreportspec$"; //$NON-NLS-1$
  private static final Pattern BASE_WAQR_FILENAME_PATTERN = Pattern.compile(AdhocWebService.RE_GET_BASE_WAQR_FILENAME);
 
  private static final String SOLUTION_NAVIGATION_DOCUMENT_MAP = "AdhocWebService.SOLUTION_NAVIGATION_DOCUMENT_MAP"; //$NON-NLS-1$
  private static final String FULL_SOLUTION_DOC = "AdhocWebService.FULL_SOLUTION_DOC"; //$NON-NLS-1$
  // attributes received from the waqr UI that have their values set to "not-set"
  // will be overridden by the metadata, the template, or the default value
  // see the javascript variable WaqrWizard.NOT_SET_VALUE for the client instance of this value
  private static final String NOT_SET_VALUE = "not-set"; //$NON-NLS-1$
  private static final String METADATA_PROPERTY_ID_VERTICAL_ALIGNMENT = "vertical-alignment"; //$NON-NLS-1$
  private static final String WAQR_REPOSITORY_PATH = "/system/waqr"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return AdhocWebService.logger;
  }

  /**
   * 
   */
  public AdhocWebService() {
    super();
  }

  public String getPayloadAsString(final HttpServletRequest request) throws IOException {
    InputStream is = request.getInputStream();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    String content = null;

    byte buffer[] = new byte[2048];
    int b = is.read(buffer);
    while (b > 0) {
      os.write(buffer, 0, b);
      b = is.read(buffer);
    }
    content = os.toString(LocaleHelper.getSystemEncoding());

    return content;
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    PentahoSystem.systemEntryPoint();
    OutputStream outputStream = response.getOutputStream();
    try {

      boolean wrapWithSoap = "false".equals(request.getParameter("ajax")); //$NON-NLS-1$ //$NON-NLS-2$
      String solutionName = request.getParameter("solution"); //$NON-NLS-1$
      String actionPath = request.getParameter("path"); //$NON-NLS-1$
      String actionName = request.getParameter("action"); //$NON-NLS-1$
      String component = request.getParameter("component"); //$NON-NLS-1$
      String content = null;
      try {
        content = getPayloadAsString(request);
      } catch (IOException ioEx) {
        String msg = Messages.getErrorString("AdhocWebService.ERROR_0006_FAILED_TO_GET_PAYLOAD_FROM_REQUEST"); //$NON-NLS-1$
        error(msg, ioEx);
        WebServiceUtil.writeString(outputStream,
            WebServiceUtil.getErrorXml(msg + " " + ioEx.getLocalizedMessage()), false); //$NON-NLS-1$
      }
      IParameterProvider parameterProvider = null;
      HashMap parameters = new HashMap();

      if (!StringUtils.isEmpty(content)) {
        Document doc = null;
        try {
          doc = XmlDom4JHelper.getDocFromString(content, new PentahoEntityResolver() );  
        } catch (XmlParseException e) {
          String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
          error(msg, e);
          WebServiceUtil.writeString(response.getOutputStream(), WebServiceUtil.getErrorXml(msg), false);
        }
        List parameterNodes = doc.selectNodes("//SOAP-ENV:Body/*/*"); //$NON-NLS-1$
        for (int i = 0; i < parameterNodes.size(); i++) {
          Node parameterNode = (Node) parameterNodes.get(i);
          String parameterName = parameterNode.getName();
          String parameterValue = parameterNode.getText();
          // String type = parameterNode.selectSingleNode( "@type" );
          // if( "xml-data".equalsIgnoreCase( ) )
          if ("action".equals(parameterName)) { //$NON-NLS-1$
            ActionInfo info = ActionInfo.parseActionString(parameterValue);
            solutionName = info.getSolutionName();
            actionPath = info.getPath();
            actionName = info.getActionName();
            parameters.put("solution", solutionName); //$NON-NLS-1$
            parameters.put("path", actionPath); //$NON-NLS-1$
            parameters.put("name", actionName); //$NON-NLS-1$         
          } else if ("component".equals(parameterName)) {  //$NON-NLS-1$  
            component = parameterValue;
          } else {
            parameters.put(parameterName, parameterValue);
          }
        }
        parameterProvider = new SimpleParameterProvider(parameters);
      } else {
        parameterProvider = new HttpRequestParameterProvider(request);
      }

      if (!"generatePreview".equals(component)) { //$NON-NLS-1$
        response.setContentType("text/xml"); //$NON-NLS-1$
        response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
      }

      // PentahoHttpSession userSession = new PentahoHttpSession(
      // request.getRemoteUser(), request.getSession(),
      // request.getLocale() );
      IPentahoSession userSession = getPentahoSession(request);

      // send the header of the message to prevent time-outs while we are working
      response.setHeader("expires", "0"); //$NON-NLS-1$ //$NON-NLS-2$

      dispatch(request, response, component, parameterProvider, outputStream, userSession, wrapWithSoap);

    } catch (IOException ioEx) {
      String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
      error(msg, ioEx);
      WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
    } catch (AdhocWebServiceException ex) {
      String msg = ex.getLocalizedMessage();
      error(msg, ex);
      WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
    } catch (PentahoMetadataException ex) {
      String msg = ex.getLocalizedMessage();
      error(msg, ex);
      WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
    } catch (PentahoAccessControlException ex) {
      String msg = ex.getLocalizedMessage();
      error(msg, ex);
      WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
    } finally {
      PentahoSystem.systemExitPoint();
    }
    if (ServletBase.debug) {
      debug(Messages.getString("HttpWebService.DEBUG_WEB_SERVICE_END")); //$NON-NLS-1$
    }
  }

  protected void dispatch(final HttpServletRequest request, final HttpServletResponse response, final String component,
      final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession, final boolean wrapWithSoap)
      throws IOException, AdhocWebServiceException, PentahoMetadataException, PentahoAccessControlException {
    if ("listbusinessmodels".equals(component)) { //$NON-NLS-1$
      listBusinessModels(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else if ("getbusinessmodel".equals(component)) { //$NON-NLS-1$
      getBusinessModel(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else if ("generatePreview".equals(component)) { //$NON-NLS-1$
      generatePreview( request, response, parameterProvider, outputStream, userSession, wrapWithSoap );
      
    } else if ("saveFile".equals(component)) { //$NON-NLS-1$
      saveFile(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else if ("searchTable".equals(component)) { //$NON-NLS-1$
      searchTable(parameterProvider, outputStream, userSession, wrapWithSoap);
      
    } else if ("getWaqrReportSpecDoc".equals(component)) { //$NON-NLS-1$
      getWaqrReportSpecDoc(parameterProvider, outputStream, userSession, wrapWithSoap);
      
    } else if ("getTemplateReportSpec".equals(component)) { //$NON-NLS-1$
      getTemplateReportSpec(parameterProvider, outputStream, userSession, wrapWithSoap);
      
    } else if ("getSolutionRepositoryDoc".equals(component)) { //$NON-NLS-1$
      String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$
      //path = StringUtils.isEmpty( path ) ? null : path;
      String solutionName = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$
      //solutionName = StringUtils.isEmpty( solutionName ) ? null : solutionName;
      Document doc = getSolutionRepositoryDoc( solutionName, path, userSession );
      WebServiceUtil.writeDocument(outputStream, doc, wrapWithSoap);
      
    } else if ("getWaqrRepositoryDoc".equals(component)) { //$NON-NLS-1$
      String folderPath = parameterProvider.getStringParameter("folderPath", null); //$NON-NLS-1$
      Document doc = getWaqrRepositoryDoc( folderPath, userSession );
      WebServiceUtil.writeDocument(outputStream, doc, wrapWithSoap);

    } else if ("getWaqrRepositoryIndexDoc".equals(component)) { //$NON-NLS-1$
      getWaqrRepositoryIndexDoc(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else if ("deleteWaqrReport".equals(component)) { //$NON-NLS-1$
      deleteWaqrReport(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else if ("getJFreePaperSizes".equals(component)) { //$NON-NLS-1$
      getJFreePaperSizes(parameterProvider, outputStream, userSession, wrapWithSoap);
    } else {
      throw new RuntimeException(Messages.getErrorString("HttpWebService.UNRECOGNIZED_COMPONENT_REQUEST", component)); //$NON-NLS-1$
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    doGet(request, response);

  }

  /**
   * Creates an XML string containing a list of the supported JFree paper sizes.
   * The attribute accessed by the XPath: /pageFormats/pageFormat[@name] contains the 
   * friendly name of the paper size
   * The attribute accessed by the XPath: /pageFormats/pageFormat[@value] contains the 
   * name of the paper size used by the JFree engine.
   * The XML String is written to the outputStream, which is typically the response.outputStream.
   * 
   * @param parameterProvider
   * @param outputStream
   * @param userSession
   * @param wrapWithSoap
   * @throws IOException
   */
  private void getJFreePaperSizes(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException
  {
    StringBuffer xmlStr = new StringBuffer();
    Enumeration pageFormatsEnum = PageFormats.enumerate();
    xmlStr.append( "<pageFormats>\n" ); //$NON-NLS-1$
    while ( pageFormatsEnum.hasMoreElements() )
    {
      PageFormats pf = (PageFormats)pageFormatsEnum.nextElement();
      xmlStr.append( "\t<pageFormat name='"  //$NON-NLS-1$
          + AdhocWebService.toFriendlyName( pf.toString() )
          + "' value='" + pf.toString() + "'/>\n" );  //$NON-NLS-1$//$NON-NLS-2$
    }
    xmlStr.append( "</pageFormats>\n" ); //$NON-NLS-1$
    
    WebServiceUtil.writeString( outputStream, xmlStr.toString(), wrapWithSoap );
  }
  
  // for a really friendly name, see:
  // http://bonsai.igalia.com/cgi-bin/bonsai/cvsblame.cgi?file=gtk%2B/gtk/paper_names.c&rev=&root=/var/publiccvs
  /**
   * Simply split the string on "_", and replace "_" with " ".
   * @param name String the string to convert into a "friendly" name
   * @return String the friendly name
   */
  private static String toFriendlyName( final String name )
  {
    StringBuffer friendlyName = new StringBuffer();
    String[] components = name.split( "_" ); //$NON-NLS-1$
    for ( int ii=0; ii<components.length; ++ii )
    {
      String component = components[ii];
      friendlyName.append( component );
      if ( ii < components.length-1 )
      {
        friendlyName.append( " " ); //$NON-NLS-1$
      }
    }
    return friendlyName.toString();
  }
  
  private void generatePreview(final HttpServletRequest request, final HttpServletResponse response,
      final IParameterProvider parameterProvider, final OutputStream outputStream, 
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException, AdhocWebServiceException, PentahoMetadataException 
  {
    String outputType = parameterProvider.getStringParameter("outputType", null); //$NON-NLS-1$
    String mimeType = MimeHelper.getMimeTypeFromExtension("." + outputType); //$NON-NLS-1$
    HttpMimeTypeListener listener = new HttpMimeTypeListener(request, response ); //$NON-NLS-1$
    listener.setMimeType(mimeType);
    listener.setName( Messages.getString("AdhocWebService.USER_REPORT_PREVIEW") );
    String reportXML = parameterProvider.getStringParameter("reportXml", null); //$NON-NLS-1$   
    String templatePath = parameterProvider.getStringParameter("templatePath", null); //$NON-NLS-1$
    try {
      boolean interactive = "true".equals( parameterProvider.getStringParameter( "interactive", null ) );
      createJFreeReportAsStream(reportXML, templatePath, outputType, outputStream, userSession, "debug", wrapWithSoap, interactive);
    } catch (Exception e) {
      response.setContentType( "text/html" ); //$NON-NLS-1$
      String msg = Messages.getString( "AdhocWebService.ERROR_0012_FAILED_TO_GENERATE_PREVIEW" ); //$NON-NLS-1$
      outputStream.write( AdhocWebService.getErrorHtml( request, e, msg ).getBytes() );
      error(msg, e);
    }
  }
  
  /**
   * TODO sbarkdull, method is poorly named and needs to be refactored, execution of action
   * resource should happen outside the method.
   * 
   * @param reportXML
   * @param templatePath
   * @param outputType
   * @param outputStream OutputStream (output parameter) the preview report's output will be written
   * to this stream.
   * @param userSession
   * @param isAjax
   * @throws AdhocWebServiceException
   * @throws IOException
   * @throws PentahoMetadataException
   */
  private void createJFreeReportAsStream(final String reportXML, final String templatePath, final String outputType,
      final OutputStream outputStream, final IPentahoSession userSession, final String loggingLevel, final boolean wrapWithSoap, final boolean interactive)
      throws AdhocWebServiceException, IOException, PentahoMetadataException {
    
    if ( StringUtil.doesPathContainParentPathSegment( templatePath ) ) {
      String msg = Messages.getString( "AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME" ); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
    
    Document reportSpecDoc = null;
    try {
      reportSpecDoc = XmlDom4JHelper.getDocFromString(reportXML, new PentahoEntityResolver() );  
    } catch (XmlParseException e) {
      String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
      error(msg, e);
      throw new AdhocWebServiceException(msg, e);
    }
    
    Element mqlNode = (Element) reportSpecDoc.selectSingleNode("/report-spec/query/mql"); //$NON-NLS-1$
    mqlNode.detach();
    
    Node reportNameNd = reportSpecDoc.selectSingleNode( "/report-spec/report-name" ); //$NON-NLS-1$
    String reportName = null != reportNameNd ? reportNameNd.getText() : ""; //$NON-NLS-1$
    Node reportDescNd = reportSpecDoc.selectSingleNode( "/report-spec/report-desc" ); //$NON-NLS-1$
    String reportDesc = reportDescNd.getText();
    
    String[] outputTypeList = { outputType };

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);

    String xactionFilename = userSession.getId() + "_wqr_preview.xaction"; //$NON-NLS-1$

    ByteArrayOutputStream jfreeOutputStream = new ByteArrayOutputStream();
    createJFreeReportDefinitionAsStream( reportXML, templatePath, mqlNode, repository,
        userSession, jfreeOutputStream );

    // create .xaction to run
    ByteArrayOutputStream xactionOutputStream = createMQLReportActionSequenceAsStream( reportName,
        reportDesc, mqlNode, outputTypeList,
        xactionFilename, jfreeOutputStream.toString( LocaleHelper.getSystemEncoding() ), 
        /*jfreeReportFileName*/null, loggingLevel, userSession);
    SimpleParameterProvider actionSequenceParameterProvider = new SimpleParameterProvider();
    actionSequenceParameterProvider.setParameter("type", outputType); //$NON-NLS-1$
    actionSequenceParameterProvider.setParameter("logging-level", "error"); //$NON-NLS-1$ //$NON-NLS-2$
    actionSequenceParameterProvider.setParameter("level", "error"); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream out;
    if( interactive ) {
      out = new ByteArrayOutputStream();
    } else {
      out = outputStream;
    }
    
    IRuntimeContext runtimeContext = AdhocWebService.executeActionSequence(xactionOutputStream.toString(LocaleHelper.getSystemEncoding()),
        "preview.xaction", actionSequenceParameterProvider, userSession, out); //$NON-NLS-1$
    
    if (runtimeContext != null) {
      runtimeContext.dispose();
    }
    if( interactive ) {
      // handle XML for interactive ad-hoc
      AdhocWebServiceInteract.interactiveOutput( out.toString(), outputStream, userSession );
    }

  }

  // TODO, isn't there a utility class or something this method can be moved to?
  private static String getJFreeColorString(final ColorSettings color) {
    if (color == null) {
      return "#FF0000"; //$NON-NLS-1$
    }
    String r = Integer.toHexString(color.getRed());
    if (r.length() == 1) {
      r = "0" + r; //$NON-NLS-1$
    }
    String g = Integer.toHexString(color.getGreen());
    if (g.length() == 1) {
      g = "0" + g; //$NON-NLS-1$
    }
    String b = Integer.toHexString(color.getBlue());
    if (b.length() == 1) {
      b = "0" + b; //$NON-NLS-1$
    }
    return "#" + r + g + b; //$NON-NLS-1$
  }

  private static void startup(final String solutionRootPath, final String baseURL) {

    LocaleHelper.setLocale(Locale.getDefault());

    PentahoSystem.loggingLevel = ILogger.ERROR;

    if (PentahoSystem.getApplicationContext() == null) {

      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(solutionRootPath, ""); //$NON-NLS-1$

      // set the base url assuming there is a running server on port 8080
      applicationContext.setBaseUrl(baseURL);

      // Setup simple-jndi for datasources
      System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty("org.osjava.sj.root", solutionRootPath + "/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      PentahoSystem.init(applicationContext);
    }
  }

  public static IRuntimeContext executeActionSequence(final String xactionStr, final String xActionName,
      final IParameterProvider parameterProvider, final IPentahoSession session, final OutputStream outputStream) {
    IRuntimeContext runtimeContext = null;

    AdhocWebService.startup(null, null);
    List messages = new ArrayList();
    String instanceId = null;
    ISolutionEngine solutionEngine = PentahoSystem.get(ISolutionEngine.class, session);
    solutionEngine.setLoggingLevel(ILogger.ERROR);
    solutionEngine.init(session);
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    HashMap parameterProviderMap = new HashMap();
    parameterProviderMap.put(HttpRequestParameterProvider.SCOPE_REQUEST, parameterProvider);
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(baseUrl);
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);

    solutionEngine.setSession(session);
    runtimeContext = solutionEngine
        .execute(
            xactionStr,
            "preview.xaction", "Adhoc Reporting", false, true, instanceId, false, parameterProviderMap, outputHandler, null, urlFactory, messages); //$NON-NLS-1$ //$NON-NLS-2$

    if ( IRuntimeContext.RUNTIME_STATUS_SUCCESS != solutionEngine.getStatus() ) {
      try {
        outputStream.write(Messages.getErrorString("AdhocWebService.ERROR_0012_FAILED_TO_GENERATE_PREVIEW").getBytes());
      } catch (IOException e) {
        logger.error(e);
      }
    }
    return runtimeContext;
  }

  private static final Map<Integer,String> METADATA_TYPE_TO_REPORT_SPEC_TYPE = new HashMap<Integer,String>();
  static {
    AdhocWebService.METADATA_TYPE_TO_REPORT_SPEC_TYPE.put(new Integer(DataTypeSettings.DATA_TYPE_NUMERIC),
        ReportSpecUtility.NUMBER_FIELD);
    AdhocWebService.METADATA_TYPE_TO_REPORT_SPEC_TYPE.put(new Integer(DataTypeSettings.DATA_TYPE_DATE), ReportSpecUtility.DATE_FIELD);
    AdhocWebService.METADATA_TYPE_TO_REPORT_SPEC_TYPE.put(new Integer(DataTypeSettings.DATA_TYPE_STRING),
        ReportSpecUtility.STRING_FIELD);
  }

  private static boolean isDefaultStringProperty( final String property )
  {
    return StringUtils.isEmpty( property );
  }

  private static boolean isNotSetStringProperty( final String property )
  {
    return AdhocWebService.NOT_SET_VALUE.equals( property );
  }
  
  /**
   * Create the JFreeReport file.
   * 
   * NOTE on the merge precedence: this method should use properties set by the 
   * WAQR UI. If the waqr UI did not set the property, then the property 
   * should be set by the metadata. If the metadata did not set a property,
   * then the property should be set by the template. If the template
   * did not set the property, then use the default.
   * 
   * NOTE on the merge algorithm: 
   * For each of the attributes in the fields (aka columns) of the reportspec,
   * if the attribute is present in the reportspec that comes in from the client
   * (ie reportXML param), and the attribute has a non-default value, do not change it.
   * If the attribute is not present or has a default value, and if there is metadata
   * for that attribute, merge the metadata attribute. This take place inline in the 
   * main loop of this method. If after the metadata merge the attribute 
   * still does not have a value, merge the template value. This takes place
   * in the AdhocWebService.applyTemplate() method.
   * If the template does not have a value, do nothing (the default will be used).
   * 
   * @param reportDoc
   * @param reportXML
   * @param templatePath
   * @param mqlNode
   * @param repository
   * @param userSession
   * @return
   * @throws IOException
   * @throws AdhocWebServiceException
   * @throws PentahoMetadataException
   */
  public void createJFreeReportDefinitionAsStream( String reportXML, String templatePath, Element mqlNode,
      ISolutionRepository repository, IPentahoSession userSession, OutputStream jfreeMergedOutputStream )
      throws IOException, AdhocWebServiceException, PentahoMetadataException {

    Map reportSpecTypeToElement = null;
    Document templateDoc = null;
    Element templateItems = null;
    boolean bUseTemplate = !StringUtils.isEmpty(templatePath);
    
    if (bUseTemplate) {
      templatePath = AdhocWebService.WAQR_REPOSITORY_PATH + templatePath;
      templateDoc = repository.getResourceAsDocument(templatePath);

      templateItems = (Element) templateDoc.selectSingleNode("/report/items"); //$NON-NLS-1$
      List nodes = templateItems.elements();
      Iterator it = nodes.iterator();
      reportSpecTypeToElement = new HashMap();
      while (it.hasNext()) {
        Element element = (Element) it.next();
        reportSpecTypeToElement.put(element.getName(), element);
      }
    }
    MetadataPublisher.loadAllMetadata(userSession, false);
    CwmSchemaFactoryInterface cwmSchemaFactory = (CwmSchemaFactoryInterface) PentahoSystem.getObject(userSession,
        "ICwmSchemaFactory"); //$NON-NLS-1$

    MQLQuery mql = MQLQueryFactory.getMQLQuery(mqlNode.asXML(), null, LocaleHelper.getLocale().toString(), cwmSchemaFactory);

    String reportXMLEncoding = XmlHelper.getEncoding(reportXML);
    ByteArrayInputStream reportSpecInputStream = new ByteArrayInputStream(reportXML.getBytes( reportXMLEncoding ));
    ReportSpec reportSpec = (ReportSpec) CastorUtility.getInstance().readCastorObject(reportSpecInputStream,
        ReportSpec.class, reportXMLEncoding );
    if (reportSpec == null) {
      throw new AdhocWebServiceException(Messages.getErrorString("AdhocWebService.ERROR_0002_REPORT_INVALID")); //$NON-NLS-1$
    }

    BusinessModel model = mql.getModel();
    if (model == null) {
      throw new AdhocWebServiceException(Messages.getErrorString("AdhocWebService.ERROR_0003_BUSINESS_VIEW_INVALID")); //$NON-NLS-1$
    }
    
    // ========== begin column width stuff
    
    // make copies of the business columns; in the next step we're going to fill out any missing column widths
    BusinessColumn[] columns = new BusinessColumn[reportSpec.getField().length]; 
    for (int i = 0; i < reportSpec.getField().length; i++) {
      Field field = reportSpec.getField()[i];
      String name = field.getName();
      BusinessColumn column = model.findBusinessColumn(name);
      columns[i] = (BusinessColumn) column.clone();
    }
    
    boolean columnWidthUnitsConsistent = AdhocWebService.areMetadataColumnUnitsConsistent( reportSpec, model );

    if ( !columnWidthUnitsConsistent ) {
      logger.error( Messages.getErrorString( "AdhocWebService.ERROR_0013_INCONSISTENT_COLUMN_WIDTH_UNITS") ); //$NON-NLS-1$
    } else {
      double columnWidthScaleFactor = 1.0;
      int missingColumnWidthCount = 0;
      int columnWidthSumOfPercents;
      double defaultWidth;
      columnWidthSumOfPercents = AdhocWebService.getSumOfMetadataColumnWidths( reportSpec, model );
      missingColumnWidthCount = AdhocWebService.getMissingColumnWidthCount(reportSpec, model);
      
      // if there are columns with no column width specified, figure out what percent we should use for them
      if (missingColumnWidthCount > 0) {
        if (columnWidthSumOfPercents < 100) {
          int remainingPercent = 100 - columnWidthSumOfPercents;
          defaultWidth = remainingPercent / missingColumnWidthCount;
          columnWidthSumOfPercents = 100;
        } else {
          defaultWidth = 10;
          columnWidthSumOfPercents += (missingColumnWidthCount * 10);
        }
        
        
        // fill in columns without column widths
        for (int i = 0; i < columns.length; i++) {
          ConceptInterface metadataConcept = columns[i].getConcept();
          ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
          if (property == null) {
            ConceptPropertyInterface newProperty = DefaultPropertyID.getDefaultEmptyProperty(ConceptPropertyType.COLUMN_WIDTH, DefaultPropertyID.COLUMN_WIDTH.getId()); 
            newProperty.setValue(new ColumnWidth(ColumnWidth.TYPE_WIDTH_PERCENT, defaultWidth));
            metadataConcept.addProperty(newProperty);
          }
        }
      }
      
      if (columnWidthSumOfPercents > 100) {
        columnWidthScaleFactor = 100.0 / (double)columnWidthSumOfPercents;
      }
      
      // now scale down if necessary
      if (columnWidthScaleFactor < 1.0) {
        for (int i = 0; i < columns.length; i++) {
          ConceptInterface metadataConcept = columns[i].getConcept();
          ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
          ((ColumnWidth) property.getValue()).setWidth(new BigDecimal(columnWidthScaleFactor * ((ColumnWidth) property.getValue()).getWidth().doubleValue()));
        }
      }
      
    }
    
    // ========== end column width stuff
    
    for (int i = 0; i < reportSpec.getField().length; i++) {
      Field field = reportSpec.getField()[i];
      String name = field.getName();
      BusinessColumn column = columns[i];
      
      AdhocWebService.applyMetadata( field, column, columnWidthUnitsConsistent);
      
      // Template properties have the lowest priority, merge them last
      if (bUseTemplate) {
        Element templateDefaults = (Element) reportSpecTypeToElement.get(AdhocWebService.METADATA_TYPE_TO_REPORT_SPEC_TYPE
            .get(new Integer(column.getDataType().getType()))); // sorry, this is ugly as hell
        /*
         * NOTE: this merge of the template with the node's properties only sets the following properties:
         * format, fontname, fontsize, color, alignment, vertical-alignment, 
         */
        AdhocWebService.applyTemplate(field, templateDefaults);
      }
      AdhocWebService.applyDefaults( field );
    } // end for
    
    /*
     * Create the xml document (generatedJFreeDoc) containing the jfreereport definition using
     * the reportSpec as input. generatedJFreeDoc will have the metadata merged with it,
     * and some of the template.
     */
    ByteArrayOutputStream jfreeOutputStream = new ByteArrayOutputStream();
    ReportGenerationUtility.createJFreeReportXMLAsStream( reportSpec, reportXMLEncoding, (OutputStream)jfreeOutputStream );
    String jfreeXml = jfreeOutputStream.toString( reportXMLEncoding );
    Document generatedJFreeDoc = null;
    try {
      generatedJFreeDoc = XmlDom4JHelper.getDocFromString(jfreeXml, new PentahoEntityResolver());  
    } catch (XmlParseException e) {
      String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
      error(msg, e);
      throw new AdhocWebServiceException(msg, e);
    }


    /*
     * Merge template's /report/items element's attributes into the
     * generated document's /report/items element's attributes. There should not be any
     * conflict with the metadata, or the xreportspec in the reportXML param
     */
    if (bUseTemplate) {
      Element reportItems = (Element) generatedJFreeDoc.selectSingleNode("/report/items"); //$NON-NLS-1$
      List templateAttrs = templateItems.attributes();  // from the template's /report/items element
      Iterator templateAttrsIt = templateAttrs.iterator();

      while (templateAttrsIt.hasNext()) {
        Attribute attr = (Attribute) templateAttrsIt.next();
        String name = attr.getName();
        String value = attr.getText();

        Node node = reportItems.selectSingleNode("@" + name); //$NON-NLS-1$
        if (node != null) {
          node.setText(value);
        } else {
          reportItems.addAttribute(name, value);
        }
      }
      List templateElements = templateItems.elements();
      Iterator templateElementsIt = templateElements.iterator();
      while (templateElementsIt.hasNext()) {
        Element element = (Element) templateElementsIt.next();
        element.detach();
        reportItems.add(element);
      }
      /*
       * NOTE: this merging of the template (ReportGenerationUtility.mergeTemplate())
       * with the generated document can wait until last because none of the 
       * properties involved in this merge are settable by the metadata or the WAQR UI
       */
      ReportGenerationUtility.mergeTemplateAsStream(templateDoc, generatedJFreeDoc, jfreeMergedOutputStream );
    } else {
      OutputFormat format = OutputFormat.createPrettyPrint();
      format.setEncoding( reportXMLEncoding );
      XMLWriter writer = new XMLWriter(jfreeMergedOutputStream, format);
      writer.write(generatedJFreeDoc);
      writer.close();
    }
  }

  /**
   * If the property has not been set by the UI, the metadata, or the template, then
   * set it with the default value, where the default value is defined in the
   * report-spec.xsd.
   * For the alignment attribute, if the template document's /report/items node
   * has an alignment attribute, then this attribute will be used for the default 
   * horizontal-alignment property for children of /report/items. In this
   * case, do not set the attribute to the default, but set it to the empty string
   * so that the report processor can use the default from the /report/items/@alignment
   * 
   * @param targetField
   * @param templateItemsElement Element the element in the template document
   * identified by the xpath /report/items
   */
  private static void applyDefaults( final Field targetField )
  {
    if ( AdhocWebService.isNotSetStringProperty( targetField.getHorizontalAlignment() ) )
    {
      // get the default horizontal alignment for the field based on the type
//      targetField.setHorizontalAlignment( AdhocWebService.DEFAULT_FIELD_PROPS.getHorizontalAlignment() );
      targetField.setHorizontalAlignment(getDefaultHorizontalAlignment(targetField));
    }
    if ( AdhocWebService.isNotSetStringProperty( targetField.getVerticalAlignment() ) )
    {
      targetField.setVerticalAlignment( AdhocWebService.DEFAULT_FIELD_PROPS.getVerticalAlignment() );
    }
  }

  /**
   * Returns the default horizontal alignment for the specified field.
   * <p/>
   * If the field is defined as numeric (and yes, the report spec uses the constants from
   * <code>java.sql.Types</code>), then it should be right justified. Otherwise it should
   * be left justified. 
   * @param targetField the field for which the default alignment should be computed
   * @return the default alignment based on the type of the field
   */
  private static String getDefaultHorizontalAlignment(final Field targetField) {
    switch (targetField.getType()) {
      case java.sql.Types.NUMERIC:
        return AlignmentEnum.RIGHT.toString();
      default:
        return AlignmentEnum.LEFT.toString();
    }
  }

  private static final Set<Integer> PERCENT_SET;
  static {
    Set<Integer> tmp = new HashSet<Integer>();
    tmp.add(ColumnWidth.TYPE_WIDTH_PERCENT);
    PERCENT_SET = Collections.unmodifiableSet( tmp );
  }
  
  private static int getSumOfMetadataColumnWidths( ReportSpec reportSpec, BusinessModel model ) {
    int sum = 0;
    
    for (int i = 0; i < reportSpec.getField().length; i++) {
      Field field = reportSpec.getField()[i];
      String name = field.getName();
      BusinessColumn column = model.findBusinessColumn(name);
      ConceptInterface metadataConcept = column.getConcept();

      ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
      if ( property != null) {
        ColumnWidth columnWidth = (ColumnWidth)property.getValue();
        sum += columnWidth.getWidth().intValue();
      }
    }

    return sum;
  }
  
  /**
   * Returns the number of columns for which no column width is specified.
   */
  private static int getMissingColumnWidthCount( ReportSpec reportSpec, BusinessModel model ) {
    int count = 0;
    
    for (int i = 0; i < reportSpec.getField().length; i++) {
      Field field = reportSpec.getField()[i];
      String name = field.getName();
      BusinessColumn column = model.findBusinessColumn(name);
      ConceptInterface metadataConcept = column.getConcept();

      ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
      if ( property == null) {
        count += 1;
      }
    }

    return count;
  }
  
  /**
   * columnWidth properties for the specified reportspec are valid if units 
   * for column widths for all columns in the reportspec are one of:
   *    percent or unspecified (PERCENT_SET)
   *    unspecified
   * @param reportSpec
   * @param model
   * @return
   */
  private static boolean areMetadataColumnUnitsConsistent( ReportSpec reportSpec, BusinessModel model ) {
    Set<Integer> unitsSet = new HashSet<Integer>();
    
    for (int i = 0; i < reportSpec.getField().length; i++) {
      Field field = reportSpec.getField()[i];
      String name = field.getName();
      BusinessColumn column = model.findBusinessColumn(name);
      ConceptInterface metadataConcept = column.getConcept();

      ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
      if ( property != null) {
        ColumnWidth columnWidth = (ColumnWidth)property.getValue();
        unitsSet.add( columnWidth.getType() );
      }
    }

    return isSetConsistent( unitsSet );
  }
  
  private static boolean isSetConsistent( Set<Integer> unitsSet ) {
    return unitsSet.isEmpty() 
    || CollectionUtils.isSubCollection( unitsSet, PERCENT_SET );
  }
  
  /**
   * 
   * @param field
   * @param column
   * @param columnWidthUnitsConsistent this value should always be the result of a call to areMetadataColumnUnitsConsistent()
   */
  private static void applyMetadata( final Field field, final BusinessColumn column, 
      boolean columnWidthUnitsConsistent ) {
    ConceptInterface metadataConcept = column.getConcept();

    if ( columnWidthUnitsConsistent ) {
      // get column width from metadata, and add to report def
      ConceptPropertyInterface property = metadataConcept.getProperty(DefaultPropertyID.COLUMN_WIDTH.getId());
      if ( property != null) {
        double width = ((ColumnWidth)property.getValue()).getWidth().doubleValue();
        field.setWidth( new BigDecimal( width ) );
        field.setIsWidthPercent( ((ColumnWidth)property.getValue()).getType() == ColumnWidth.TYPE_WIDTH_PERCENT );
        field.setWidthLocked( true );
      }
    }
    
    // WAQR doesn't set font properties, so if metadata font properties are available, they win
    FontSettings font = null;
    if (metadataConcept.getProperty(DefaultPropertyID.FONT.getId()) != null) {
      font = (FontSettings) metadataConcept.getProperty(DefaultPropertyID.FONT.getId()).getValue();
    }

    // set font size, name, and style
    if (font != null) {
      if (font.getHeight() > 0) {
        field.setFontSize(font.getHeight());
      }
      field.setFontName(font.getName());
      int fontStyle = 0;
      if (font.isBold()) {
        fontStyle = ReportSpecUtility.addFontStyleBold( fontStyle );
      }
      if (font.isItalic()) {
        fontStyle = ReportSpecUtility.addFontStyleItalic( fontStyle );
      }
      field.setFontStyle(fontStyle);
    }
    // else use the default values in the Field class
    
    if ( AdhocWebService.isNotSetStringProperty( field.getHorizontalAlignment() ))
    {
      AlignmentSettings alignment = null;
      ConceptPropertyInterface horizontalAlignProp = metadataConcept.getProperty(DefaultPropertyID.ALIGNMENT.getId());
      if (horizontalAlignProp != null) {
        alignment = (AlignmentSettings) horizontalAlignProp.getValue();

        if (alignment != null) {
          if (alignment.getType() == AlignmentSettings.TYPE_ALIGNMENT_LEFT) {
            field.setHorizontalAlignment("left"); //$NON-NLS-1$
          } else if (alignment.getType() == AlignmentSettings.TYPE_ALIGNMENT_RIGHT) {
            field.setHorizontalAlignment("right"); //$NON-NLS-1$
          } else if (alignment.getType() == AlignmentSettings.TYPE_ALIGNMENT_CENTERED) {
            field.setHorizontalAlignment("center"); //$NON-NLS-1$
          }
        }
      }
    }
    if ( AdhocWebService.isNotSetStringProperty( field.getVerticalAlignment() ))
    {
      String valignSetting = null;
      ConceptPropertyInterface valignProp = metadataConcept.getProperty( AdhocWebService.METADATA_PROPERTY_ID_VERTICAL_ALIGNMENT );
      if ( valignProp != null) {
        valignSetting = (String)valignProp.getValue();
        field.setVerticalAlignment( valignSetting );
      }
    }

    // WAQR doesn't set color properties, so if metadata font properties are available, they win
    ColorSettings color = null;
    if (metadataConcept.getProperty(DefaultPropertyID.COLOR_BG.getId()) != null) {
      color = (ColorSettings) metadataConcept.getProperty(DefaultPropertyID.COLOR_BG.getId()).getValue();
    }

    if (color != null) {
      String htmlColor = AdhocWebService.getJFreeColorString(color);
      field.setBackgroundColor(htmlColor);
      field.setUseBackgroundColor(true);
    }

    color = null;
    if (metadataConcept.getProperty(DefaultPropertyID.COLOR_FG.getId()) != null) {
      color = (ColorSettings) metadataConcept.getProperty(DefaultPropertyID.COLOR_FG.getId()).getValue();
    }

    if (color != null) {
      String htmlColor = AdhocWebService.getJFreeColorString(color);
      field.setFontColor(htmlColor);
    }

    String metaDataDisplayName = column.getDisplayName(LocaleHelper.getLocale().toString());

    if (field.getIsDetail()) {
      field.setDisplayName(metaDataDisplayName);
    } else {
      String reportSpecDisplayName = field.getDisplayName();
      if (AdhocWebService.isDefaultStringProperty(reportSpecDisplayName)) {
        String displayName = column.getDisplayName(LocaleHelper.getLocale().toString());
        field.setDisplayName(displayName + ": $(" + field.getName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$           
      }
    }
    if ( AdhocWebService.isDefaultStringProperty( field.getFormat() )) {
      ConceptPropertyInterface cpi = metadataConcept.getProperty(DefaultPropertyID.MASK.getId());
      if (cpi != null ) {
        String format = (String)cpi.getValue();
        if ( !StringUtils.isEmpty( format ) )
        { 
          field.setFormat( format );
        }
      }
    }
  }
  
  private static void applyTemplate(final Field field, final Element templateDefaults) {
    if (templateDefaults != null) {
      Node node = null;
      if ( AdhocWebService.isNotSetStringProperty( field.getHorizontalAlignment() ) ) {
        node = templateDefaults.selectSingleNode("@alignment"); //$NON-NLS-1$
        if (node != null) {
          field.setHorizontalAlignment(node.getText());
        }
      }
      if ( AdhocWebService.isNotSetStringProperty( field.getVerticalAlignment() ) ) {
        node = templateDefaults.selectSingleNode("@vertical-alignment");//$NON-NLS-1$
        if (node != null) {
          field.setVerticalAlignment(node.getText());
        }
      }
      if ( AdhocWebService.isDefaultStringProperty( field.getFormat() ) ) {
        node = templateDefaults.selectSingleNode("@format");//$NON-NLS-1$
        if (node != null) {
          field.setFormat(node.getText());
        }
      }
      if ( AdhocWebService.isDefaultStringProperty( field.getFontName() ) ) {
        node = templateDefaults.selectSingleNode("@fontname");//$NON-NLS-1$
        if (node != null) {
          field.setFontName(node.getText());
        }
      }
      if ( AdhocWebService.isDefaultStringProperty( field.getFontColor() ) ) {
        node = templateDefaults.selectSingleNode("@color");//$NON-NLS-1$
        if (node != null) {
          field.setFontColor(node.getText());
        }
      }
      if ( !field.hasFontSize() ) {
        node = templateDefaults.selectSingleNode("@fontsize"); //$NON-NLS-1$
        if (node != null) {
          field.setFontSize(Integer.parseInt(node.getText()));
        }
      }
    }
  }

  // TODO sbarkdull, this has been upgraded to new naming convention, delete comment
  private void addMQLQueryXml(final Element elem, final String domainId, final String modelId, final String tableId, final String columnId,
      final String searchStr) {

    Element mqlElement = elem.addElement("mql"); //$NON-NLS-1$
    mqlElement.addElement("domain_type").setText("relational"); //$NON-NLS-1$ //$NON-NLS-2$
    mqlElement.addElement("domain_id").setText(domainId); //$NON-NLS-1$
    mqlElement.addElement("model_id").setText(modelId); //$NON-NLS-1$
    Element selectionElement = mqlElement.addElement("selections"); //$NON-NLS-1$
    selectionElement = selectionElement.addElement("selection"); //$NON-NLS-1$
    selectionElement.addElement("table").setText(tableId); //$NON-NLS-1$
    selectionElement.addElement("column").setText(columnId); //$NON-NLS-1$

    if (!StringUtils.isEmpty(searchStr)) {
      Element constraintElement = mqlElement.addElement("constraints"); //$NON-NLS-1$
      constraintElement = constraintElement.addElement("constraint"); //$NON-NLS-1$
      constraintElement.addElement("table_id").setText(tableId); //$NON-NLS-1$
      constraintElement.addElement("condition").setText(searchStr); //$NON-NLS-1$
    }
  }

  // TODO sbarkdull, this has been upgraded to new naming convention, delete comment
  public void createMQLQueryActionSequence(final String domainId, final String modelId, final String tableId, final String columnId,
      final String searchStr, final OutputStream outputStream, final String userSessionName) throws IOException {

    Document document = DOMDocumentFactory.getInstance().createDocument();
    document.setXMLEncoding(LocaleHelper.getSystemEncoding());
    Element actionSeqElement = document.addElement("action-sequence"); //$NON-NLS-1$
    actionSeqElement.addElement("version").setText("1"); //$NON-NLS-1$ //$NON-NLS-2$
    actionSeqElement.addElement("title").setText("MQL Query"); //$NON-NLS-1$ //$NON-NLS-2$
    actionSeqElement.addElement("logging-level").setText("ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
    Element documentationElement = actionSeqElement.addElement("documentation"); //$NON-NLS-1$
    Element authorElement = documentationElement.addElement("author"); //$NON-NLS-1$
    if (userSessionName != null) {
      authorElement.setText(userSessionName);
    } else {
      authorElement.setText("Web Query & Reporting"); //$NON-NLS-1$
    }
    documentationElement.addElement("description").setText("Temporary action sequence used by Web Query & Reporting"); //$NON-NLS-1$ //$NON-NLS-2$
    documentationElement.addElement("result-type").setText("result-set"); //$NON-NLS-1$ //$NON-NLS-2$
    Element outputsElement = actionSeqElement.addElement("outputs"); //$NON-NLS-1$
    Element reportTypeElement = outputsElement.addElement("query_result"); //$NON-NLS-1$
    reportTypeElement.addAttribute("type", "result-set"); //$NON-NLS-1$ //$NON-NLS-2$
    Element destinationsElement = reportTypeElement.addElement("destinations"); //$NON-NLS-1$
    destinationsElement.addElement("response").setText("query_result"); //$NON-NLS-1$ //$NON-NLS-2$

    Element actionsElement = actionSeqElement.addElement("actions"); //$NON-NLS-1$
    Element actionDefinitionElement = actionsElement.addElement("action-definition"); //$NON-NLS-1$
    Element actionOutputsElement = actionDefinitionElement.addElement("action-outputs"); //$NON-NLS-1$
    Element ruleResultElement = actionOutputsElement.addElement("query-result"); //$NON-NLS-1$
    ruleResultElement.addAttribute("type", "result-set"); //$NON-NLS-1$ //$NON-NLS-2$
    ruleResultElement.addAttribute("mapping", "query_result"); //$NON-NLS-1$ //$NON-NLS-2$
    actionDefinitionElement.addElement("component-name").setText("MQLRelationalDataComponent"); //$NON-NLS-1$ //$NON-NLS-2$
    actionDefinitionElement.addElement("action-type").setText("MQL Query"); //$NON-NLS-1$ //$NON-NLS-2$
    Element componentDefinitionElement = actionDefinitionElement.addElement("component-definition"); //$NON-NLS-1$

    // componentDefinitionElement.addElement("query").addCDATA(createMQLQueryXml(domainId, modelId, tableId, columnId, searchStr)); //$NON-NLS-1$
    addMQLQueryXml(componentDefinitionElement, domainId, modelId, tableId, columnId, searchStr);

    // end action-definition for JFreeReportComponent
    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding( LocaleHelper.getSystemEncoding() );
    XMLWriter writer = new XMLWriter(outputStream, format);
    writer.write(document);
    writer.close();
  }

  // TODO sbarkdull, method needs to be renamed
  // TODO sbarkdull, method needs to be refactored so that instead of constructing the
  // document using the DOM, an xml template with no values is read in at initialization
  // and then this method uses the template to create a new document, and adds the
  // report specific information to the new document. It will make this code MUCH
  // easier to read and maintain. Other similar methods in this class should likely
  // be refactored in a similar way
  public ByteArrayOutputStream createMQLReportActionSequenceAsStream( final String reportName, final String reportDescription, final Element mqlNode,
      final String[] outputTypeList, final String xactionName, final String jfreeReportXML, final String jfreeReportFilename,
      final String loggingLevel, final IPentahoSession userSession) throws IOException , AdhocWebServiceException{
    
    boolean bIsMultipleOutputType = outputTypeList.length > 1;
    ByteArrayOutputStream xactionOutputStream = new ByteArrayOutputStream();    
    
    Document document = DOMDocumentFactory.getInstance().createDocument();
    document.setXMLEncoding( LocaleHelper.getSystemEncoding() );
    Element actionSeqElement = document.addElement("action-sequence"); //$NON-NLS-1$
    Element actionSeqNameElement = actionSeqElement.addElement("name"); //$NON-NLS-1$
    actionSeqNameElement.setText(xactionName);
    Element actionSeqVersionElement = actionSeqElement.addElement("version"); //$NON-NLS-1$
    actionSeqVersionElement.setText("1"); //$NON-NLS-1$
    
    Element actionSeqTitleElement = actionSeqElement.addElement("title"); //$NON-NLS-1$
    String reportTitle = AdhocWebService.getBaseFilename( reportName ); // remove ".waqr.xreportspec" if it is there
    actionSeqTitleElement.setText(reportTitle);
    
    Element loggingLevelElement = actionSeqElement.addElement("logging-level"); //$NON-NLS-1$
    loggingLevelElement.setText( loggingLevel );
    
    Element documentationElement = actionSeqElement.addElement("documentation"); //$NON-NLS-1$
    Element authorElement = documentationElement.addElement("author"); //$NON-NLS-1$
    if (userSession.getName() != null) {
      authorElement.setText(userSession.getName());
    } else {
      authorElement.setText("Web Query & Reporting"); //$NON-NLS-1$
    }
    Element descElement = documentationElement.addElement("description"); //$NON-NLS-1$
    descElement.setText(reportDescription);
    Element iconElement = documentationElement.addElement("icon"); //$NON-NLS-1$
    iconElement.setText("PentahoReporting.png"); //$NON-NLS-1$
    Element helpElement = documentationElement.addElement("help"); //$NON-NLS-1$
    helpElement.setText("Auto-generated action-sequence for WAQR."); //$NON-NLS-1$
    Element resultTypeElement = documentationElement.addElement("result-type"); //$NON-NLS-1$
    resultTypeElement.setText("report"); //$NON-NLS-1$
    // inputs
    Element inputsElement = actionSeqElement.addElement("inputs"); //$NON-NLS-1$
    Element outputTypeElement = inputsElement.addElement("output-type"); //$NON-NLS-1$
    outputTypeElement.addAttribute("type", "string"); //$NON-NLS-1$ //$NON-NLS-2$
    Element defaultValueElement = outputTypeElement.addElement("default-value"); //$NON-NLS-1$
    defaultValueElement.setText(outputTypeList[0]);
    Element sourcesElement = outputTypeElement.addElement("sources"); //$NON-NLS-1$
    Element requestElement = sourcesElement.addElement(HttpRequestParameterProvider.SCOPE_REQUEST);
    requestElement.setText("type"); //$NON-NLS-1$

    if (bIsMultipleOutputType) {
      // define list of report output-file extensions (html, pdf, xls, csv)
      /*
       * <mimeTypes type="string-list"> <sources> <request>mimeTypes</request> </sources> <default-value type="string-list"> <list-item>html</list-item> <list-item>pdf</list-item> <list-item>xls</list-item> <list-item>csv</list-item>
       * </default-value> </mimeTypes>
       */
      Element mimeTypes = inputsElement.addElement("mimeTypes");//$NON-NLS-1$ 
      mimeTypes.addAttribute("type", "string-list"); //$NON-NLS-1$ //$NON-NLS-2$
      Element sources = mimeTypes.addElement("sources");//$NON-NLS-1$ 
      requestElement = sources.addElement("request"); //$NON-NLS-1$
      requestElement.setText("mimeTypes"); //$NON-NLS-1$
      Element defaultValue = mimeTypes.addElement("default-value"); //$NON-NLS-1$
      defaultValue.addAttribute("type", "string-list"); //$NON-NLS-1$ //$NON-NLS-2$
      //$NON-NLS-1$
      Element listItem = null;
      for (String outputType : outputTypeList) {
        listItem = defaultValue.addElement("list-item"); //$NON-NLS-1$
        listItem.setText(outputType);
      }
    }

    // outputs
    Element outputsElement = actionSeqElement.addElement("outputs"); //$NON-NLS-1$
    Element reportTypeElement = outputsElement.addElement("report"); //$NON-NLS-1$
    reportTypeElement.addAttribute("type", "content"); //$NON-NLS-1$ //$NON-NLS-2$
    Element destinationsElement = reportTypeElement.addElement("destinations"); //$NON-NLS-1$
    Element responseElement = destinationsElement.addElement("response"); //$NON-NLS-1$
    responseElement.setText("content"); //$NON-NLS-1$
    // resources
    Element resourcesElement = actionSeqElement.addElement("resources"); //$NON-NLS-1$
    Element reportDefinitionElement = resourcesElement.addElement("report-definition"); //$NON-NLS-1$

    Element solutionFileElement = null;
    if ( null == jfreeReportFilename ) {
      // likely they are running a preview
      solutionFileElement = reportDefinitionElement.addElement("xml"); //$NON-NLS-1$
      Element locationElement = solutionFileElement.addElement("location"); //$NON-NLS-1$ 
      Document jfreeReportDoc = null;
      try {
        jfreeReportDoc = XmlDom4JHelper.getDocFromString(jfreeReportXML, new PentahoEntityResolver());  
      } catch (XmlParseException e) {
        String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
        error(msg, e);
        throw new AdhocWebServiceException(msg, e);
      }

      Node reportNode = jfreeReportDoc.selectSingleNode("/report"); //$NON-NLS-1$
      locationElement.add(reportNode);
    } else {
      // likely they are saving the report
      solutionFileElement = reportDefinitionElement.addElement("solution-file"); //$NON-NLS-1$
      Element locationElement = solutionFileElement.addElement("location"); //$NON-NLS-1$
      locationElement.setText(jfreeReportFilename);
    }
    
    Element mimeTypeElement = solutionFileElement.addElement("mime-type"); //$NON-NLS-1$
    mimeTypeElement.setText("text/xml"); //$NON-NLS-1$
    Element actionsElement = actionSeqElement.addElement("actions"); //$NON-NLS-1$
    Element actionDefinitionElement = null;
    if (bIsMultipleOutputType) // do secure filter
    {
      // begin action-definition for Secure Filter
      /*
       * <action-definition> <component-name>SecureFilterComponent</component-name> <action-type>Prompt/Secure Filter</action-type> <action-inputs> <output-type type="string"/> <mimeTypes type="string-list"/> </action-inputs>
       * <component-definition> <selections> <output-type prompt-if-one-value="true"> <title>Select output type:</title> <filter>mimeTypes</filter> </output-type> </selections> </component-definition> </action-definition>
       */
      actionDefinitionElement = actionsElement.addElement("action-definition"); //$NON-NLS-1$
      Element componentName = actionDefinitionElement.addElement("component-name"); //$NON-NLS-1$
      componentName.setText("SecureFilterComponent"); //$NON-NLS-1$
      Element actionType = actionDefinitionElement.addElement("action-type"); //$NON-NLS-1$
      actionType.setText("Prompt/Secure Filter"); //$NON-NLS-1$

      Element actionInputs = actionDefinitionElement.addElement("action-inputs"); //$NON-NLS-1$
      Element outputType = actionInputs.addElement("output-type"); //$NON-NLS-1$
      outputType.addAttribute("type", "string"); //$NON-NLS-1$ //$NON-NLS-2$
      Element mimeTypes = actionInputs.addElement("mimeTypes"); //$NON-NLS-1$
      mimeTypes.addAttribute("type", "string-list"); //$NON-NLS-1$ //$NON-NLS-2$

      Element componentDefinition = actionDefinitionElement.addElement("component-definition"); //$NON-NLS-1$
      Element selections = componentDefinition.addElement("selections"); //$NON-NLS-1$
      outputType = selections.addElement("output-type"); //$NON-NLS-1$
      outputType.addAttribute("prompt-if-one-value", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      Element title = outputType.addElement("title"); //$NON-NLS-1$
      String prompt = Messages.getString("AdhocWebService.SELECT_OUTPUT_TYPE");//$NON-NLS-1$
      title.setText(prompt);
      Element filter = outputType.addElement("filter"); //$NON-NLS-1$
      filter.setText("mimeTypes"); //$NON-NLS-1$
    }

    // begin action-definition for SQLLookupRule
    actionDefinitionElement = actionsElement.addElement("action-definition"); //$NON-NLS-1$
    Element actionOutputsElement = actionDefinitionElement.addElement("action-outputs"); //$NON-NLS-1$
    Element ruleResultElement = actionOutputsElement.addElement("rule-result"); //$NON-NLS-1$
    ruleResultElement.addAttribute("type", "result-set"); //$NON-NLS-1$ //$NON-NLS-2$
    Element componentNameElement = actionDefinitionElement.addElement("component-name"); //$NON-NLS-1$
    componentNameElement.setText("MQLRelationalDataComponent"); //$NON-NLS-1$
    Element actionTypeElement = actionDefinitionElement.addElement("action-type"); //$NON-NLS-1$
    actionTypeElement.setText("rule"); //$NON-NLS-1$
    Element componentDefinitionElement = actionDefinitionElement.addElement("component-definition"); //$NON-NLS-1$
    componentDefinitionElement.add(mqlNode);
    componentDefinitionElement.addElement("live").setText("true"); //$NON-NLS-1$ //$NON-NLS-2$
    componentDefinitionElement.addElement("display-names").setText("false"); //$NON-NLS-1$ //$NON-NLS-2$
    // end action-definition for SQLLookupRule
    // begin action-definition for JFreeReportComponent
    actionDefinitionElement = actionsElement.addElement("action-definition"); //$NON-NLS-1$
    actionOutputsElement = actionDefinitionElement.addElement("action-outputs"); //$NON-NLS-1$

    Element actionInputsElement = actionDefinitionElement.addElement("action-inputs"); //$NON-NLS-1$
    outputTypeElement = actionInputsElement.addElement("output-type"); //$NON-NLS-1$
    outputTypeElement.addAttribute("type", "string"); //$NON-NLS-1$ //$NON-NLS-2$
    Element dataElement = actionInputsElement.addElement("data"); //$NON-NLS-1$

    Element actionResourcesElement = actionDefinitionElement.addElement("action-resources"); //$NON-NLS-1$
    Element reportDefinition = actionResourcesElement.addElement("report-definition"); //$NON-NLS-1$
    reportDefinition.addAttribute("type", "resource"); //$NON-NLS-1$ //$NON-NLS-2$

    dataElement.addAttribute("type", "result-set"); //$NON-NLS-1$ //$NON-NLS-2$
    dataElement.addAttribute("mapping", "rule-result"); //$NON-NLS-1$ //$NON-NLS-2$
    Element reportOutputElement = actionOutputsElement.addElement("report"); //$NON-NLS-1$
    reportOutputElement.addAttribute("type", "content"); //$NON-NLS-1$ //$NON-NLS-2$
    componentNameElement = actionDefinitionElement.addElement("component-name"); //$NON-NLS-1$
    componentNameElement.setText("JFreeReportComponent"); //$NON-NLS-1$
    actionTypeElement = actionDefinitionElement.addElement("action-type"); //$NON-NLS-1$
    actionTypeElement.setText("report"); //$NON-NLS-1$
    componentDefinitionElement = actionDefinitionElement.addElement("component-definition"); //$NON-NLS-1$
    componentDefinitionElement.addElement("output-type").setText(outputTypeList[0]); //$NON-NLS-1$

    // end action-definition for JFreeReportComponent
    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding( LocaleHelper.getSystemEncoding() );
    XMLWriter writer = new XMLWriter(xactionOutputStream, format);
    writer.write(document);
    writer.close();
    
    return xactionOutputStream;
  }

  public void lookupValues(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException {

    String domainName = parameterProvider.getStringParameter("model", null); //$NON-NLS-1$
    String viewId = parameterProvider.getStringParameter("view", null); //$NON-NLS-1$
    String columnId = parameterProvider.getStringParameter("column", null); //$NON-NLS-1$

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    component.validate(userSession, null);
    component.setAction(PMDUIComponent.ACTION_LOOKUP);
    component.setDomainName(domainName);
    component.setModelId(viewId);
    component.setColumnId(columnId);

    Document doc = component.getXmlContent();
    WebServiceUtil.writeDocument(outputStream, doc, wrapWithSoap);
  }
  
  private void getWaqrRepositoryIndexDoc(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException, AdhocWebServiceException {

    String templateFolderPath = parameterProvider.getStringParameter("templateFolderPath", null); //$NON-NLS-1$
    if ( StringUtil.doesPathContainParentPathSegment( templateFolderPath )) {
      String msg = Messages.getString( "AdhocWebService.ERROR_0010_OPEN_INDEX_DOC_FAILED", templateFolderPath ); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
    
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String templateFilename = "system/waqr" + templateFolderPath + "/" + ISolutionRepository.INDEX_FILENAME; //$NON-NLS-1$ //$NON-NLS-2$
    try {
      InputStream inStrm = repository.getResourceInputStream(templateFilename, false);
      Document indexDoc = XmlDom4JHelper.getDocFromStream(inStrm, new PentahoEntityResolver());
      ISolutionFile templateFile = repository.getFileByPath(templateFilename);
      repository.localizeDoc(indexDoc, templateFile);
      
      WebServiceUtil.writeDocument(outputStream, indexDoc, wrapWithSoap);
    } catch (Exception e) {
      String msg = Messages.getString("AdhocWebService.ERROR_0010_OPEN_INDEX_DOC_FAILED"); //$NON-NLS-1$
      msg = msg + " " + e.getLocalizedMessage(); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
  }

  private void deleteWaqrReport(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException, AdhocWebServiceException {
    
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    // NOTE: sbarkdull, shouldn't have to place the "/" on the front of the path segments.
    String solution = "/" + parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$ //$NON-NLS-2$
    String path = "/" + parameterProvider.getStringParameter("path", null); //$NON-NLS-1$ //$NON-NLS-2$
    String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$
    String baseFilename = AdhocWebService.getBaseFilename( filename );

    if (StringUtil.doesPathContainParentPathSegment( path ) )
    {
      String msg = Messages.getString("AdhocWebService.ERROR_0007_FAILED_TO_DELETE_FILES", filename ); //$NON-NLS-1$
      throw new  AdhocWebServiceException( msg );
    }
    
    String msg = ""; //$NON-NLS-1$
    String xactionFile = "/" + baseFilename + "." + AdhocWebService.WAQR_EXTENSION + ".xaction"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    boolean success = SolutionRepositoryService.delete(userSession, solution, path, xactionFile);
    // if we fail to delete the protected xaction file, don't delete the xml or reportspec files
    if (success) {
      String jfreeFile = "/" + baseFilename + "." + AdhocWebService.WAQR_EXTENSION + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (!SolutionRepositoryService.delete(userSession, solution, path, jfreeFile)) {
        msg = jfreeFile + " "; //$NON-NLS-1$
      }

      String reportSpecFile = "/" + baseFilename + "." + AdhocWebService.WAQR_EXTENSION + ".xreportspec"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (!SolutionRepositoryService.delete(userSession, solution, path, reportSpecFile)) {
        msg += reportSpecFile + " "; //$NON-NLS-1$
      }
    } else {
      msg += xactionFile + " "; //$NON-NLS-1$
    }

    if (msg.length() == 0) {
      msg = WebServiceUtil.getStatusXml(Messages.getString("AdhocWebService.USER_DELETE_SUCCESSFUL")); //$NON-NLS-1$
    } else {
      msg = Messages.getString("AdhocWebService.ERROR_0007_FAILED_TO_DELETE_FILES", msg); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }

    WebServiceUtil.writeString(outputStream, msg, wrapWithSoap);
  }

  private void saveFile(final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession,
      final boolean wrapWithSoap) throws AdhocWebServiceException, IOException, PentahoMetadataException, PentahoAccessControlException {

  if ("true".equals(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) {
    throw new AdhocWebServiceException("Save is disabled.");
  }
    
    String fileName = parameterProvider.getStringParameter("name", null); //$NON-NLS-1$
    if (AdhocWebService.isWaqrFilename(fileName)) {
      saveReportSpec(fileName, parameterProvider, outputStream, userSession, wrapWithSoap);
    } else {
      throw new RuntimeException("saveFile is only implemented for WAQR files, stay tuned."); //$NON-NLS-1$
    }
  }

  /**
   * 
   * @param fileName
   * @param parameterProvider
   * @param outputStream
   * @param userSession
   * @param wrapWithSoap
   * @throws AdhocWebServiceException
   * @throws IOException
   * @throws PentahoMetadataException 
   * @throws PentahoAccessControlException 
   */
  protected void saveReportSpec(final String fileName, final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws AdhocWebServiceException, IOException,
      PentahoMetadataException, PentahoAccessControlException {

    // TODO sbarkdull, all parameters coming in from the client need to be validated, and error msgs returned to the client when a parameter does not validate
    String reportXML = parameterProvider.getStringParameter("content", null); //$NON-NLS-1$    
    String solutionName = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$
    String solutionPath = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$
    String templatePath = parameterProvider.getStringParameter("templatePath", null); //$NON-NLS-1$ 
    boolean overwrite = parameterProvider.getStringParameter("overwrite", "false").equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    String outputType = parameterProvider.getStringParameter("outputType", null); //$NON-NLS-1$
 
    if ( StringUtil.doesPathContainParentPathSegment( solutionPath )
        || StringUtil.doesPathContainParentPathSegment( templatePath ) ) {
      String msg = Messages.getString( "AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME" ); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String baseUrl = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$

    Document reportSpecDoc = null;
    try {
      reportSpecDoc = XmlDom4JHelper.getDocFromString(reportXML, new PentahoEntityResolver());  
    } catch (XmlParseException e) {
      String msg = Messages.getErrorString("HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE"); //$NON-NLS-1$
      error(msg, e);
      throw new AdhocWebServiceException(msg, e);
    }

    if ( null == reportSpecDoc )
    {
      String msg = Messages.getString("AdhocWebService.ERROR_0009_SAVE_FAILED") //$NON-NLS-1$
        + " " + Messages.getString("AdhocWebService.INVALID_CLIENT_XML" );  //$NON-NLS-1$//$NON-NLS-2$
        
      throw new AdhocWebServiceException( msg );
    }
    Element mqlNode = (Element) reportSpecDoc.selectSingleNode("/report-spec/query/mql"); //$NON-NLS-1$
    mqlNode.detach();
    
    Node reportNameNd = reportSpecDoc.selectSingleNode( "/report-spec/report-name" ); //$NON-NLS-1$
    String reportName = reportNameNd.getText();
    Node reportDescNd = reportSpecDoc.selectSingleNode( "/report-spec/report-desc" ); //$NON-NLS-1$
    String reportDesc = reportDescNd.getText();

    String[] outputTypeList = outputType.split(","); //$NON-NLS-1$
    
    String baseName = AdhocWebService.getBaseFilename( fileName );
    if (null == baseName) {
      String msg = Messages.getString("AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME");//$NON-NLS-1$
      throw new AdhocWebServiceException(msg);
    }
    baseName += "." + AdhocWebService.WAQR_EXTENSION; //$NON-NLS-1$
    String xactionFilename = baseName + ".xaction"; //$NON-NLS-1$ 
    String jfreeFilename = baseName + ".xml"; //$NON-NLS-1$ 
    String xreportSpecFilename = baseName + ".xreportspec"; //$NON-NLS-1$ 

    ByteArrayOutputStream jfreeOutputStream = new ByteArrayOutputStream();
    createJFreeReportDefinitionAsStream( reportXML, templatePath, mqlNode, repository,
        userSession, jfreeOutputStream );
    String jfreeString = jfreeOutputStream.toString( LocaleHelper.getSystemEncoding() );
    
    // create .xaction to save
    ByteArrayOutputStream xactionOutputStream = createMQLReportActionSequenceAsStream( reportName,
        reportDesc, mqlNode, outputTypeList, 
        xactionFilename, jfreeString, jfreeFilename, "warn", userSession);
    solutionPath = StringUtils.isEmpty(solutionPath) ? "" : "/" + solutionPath; //$NON-NLS-1$ //$NON-NLS-2$
    String path = solutionName + solutionPath + "/"; //$NON-NLS-1$ 

    int jfreeSaveStatus = ISolutionRepository.FILE_ADD_FAILED;
    int xreportSpecSaveStatus = ISolutionRepository.FILE_ADD_FAILED;

    int xactionSaveStatus = repository.publish(baseUrl, path, xactionFilename,
        xactionOutputStream.toString(LocaleHelper.getSystemEncoding()).getBytes(), overwrite);
    if (xactionSaveStatus == ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      // add jfree report xml
      jfreeSaveStatus = repository.publish(baseUrl, path, jfreeFilename,
          jfreeString.getBytes( LocaleHelper.getSystemEncoding() ), overwrite);
      if (jfreeSaveStatus == ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        xreportSpecSaveStatus = repository.publish(baseUrl, path, xreportSpecFilename, reportXML
            .getBytes(LocaleHelper.getSystemEncoding()), overwrite);
      }
    }
    int[] errorStatusAr = { xactionSaveStatus, jfreeSaveStatus, xreportSpecSaveStatus };
    int errorStatus = AdhocWebService.getSaveErrorStatus(errorStatusAr);
    if (errorStatus == ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      String msg = WebServiceUtil.getStatusXml(Messages.getString("AdhocWebService.USER_REPORT_SAVED")); //$NON-NLS-1$
      WebServiceUtil.writeString(outputStream, msg, wrapWithSoap);

      // Force a refresh of the cached solution tree. This will cause the
      // newly saved files to show up in the next directory listing.
      // if overwrite is true, they are saving over an existing file, so that file's name
      // will already be in the cached sol. repos. tree.
      if ( !overwrite ) {
        invalidateSolutionRepositoryTree( userSession );
        repository.reloadSolutionRepository( userSession, repository.getLoggingLevel() );
      }
      
    } else {
      // TODO sbarkdull, if any of the saves fails, remove the saved files
      // that succeeded (simulate a transaction)
      String[] FILE_STATUS_MSG = { "", // item 0 does not exist//$NON-NLS-1$ 
        "AdhocWebService.ERROR_0001_FILE_EXISTS",//$NON-NLS-1$ 
        "AdhocWebService.ERROR_0002_FILE_ADD_FAILED",//$NON-NLS-1$ 
        "AdhocWebService.STATUS_0003_FILE_ADD_SUCCESSFUL",//$NON-NLS-1$ 
        "AdhocWebService.ERROR_0004_FILE_ADD_INVALID_PUBLISH_PASSWORD",//$NON-NLS-1$ 
        "AdhocWebService.ERROR_0005_FILE_ADD_INVALID_USER_CREDENTIALS"//$NON-NLS-1$ 
      };
      
      String msg = Messages.getString("AdhocWebService.ERROR_0009_SAVE_FAILED") + " " + Messages.getString( FILE_STATUS_MSG[errorStatus] ); //$NON-NLS-1$ //$NON-NLS-2$
      if (ISolutionRepository.FILE_EXISTS == errorStatus) {
        msg += " (" + xreportSpecFilename + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      throw new AdhocWebServiceException( msg );
    }
  }

  public void listBusinessModels(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException {

    String domainName = parameterProvider.getStringParameter("domain", null); //$NON-NLS-1$

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    component.validate(userSession, null);
    component.setAction(PMDUIComponent.ACTION_LIST_MODELS);
    component.setDomainName(domainName);

    Document doc = component.getXmlContent();

    WebServiceUtil.writeDocument(outputStream, doc, wrapWithSoap);
  }

  public void getBusinessModel(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws IOException {

    String domainName = parameterProvider.getStringParameter("domain", null); //$NON-NLS-1$
    String modelId = parameterProvider.getStringParameter("model", null); //$NON-NLS-1$

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$

    PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
    component.validate(userSession, null);
    component.setAction(PMDUIComponent.ACTION_LOAD_MODEL);
    component.setDomainName(domainName);
    component.setModelId(modelId);

    Document doc = component.getXmlContent();
    WebServiceUtil.writeDocument(outputStream, doc, wrapWithSoap);
  }

  // TODO sbarkdull, this has been partially upgraded to new naming convention, delete comment
  public void searchTable(final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession,
      final boolean wrapWithSoap) throws IOException {
    String domainId = (String) parameterProvider.getParameter("modelId"); //$NON-NLS-1$
    String modelId = (String) parameterProvider.getParameter("viewId"); //$NON-NLS-1$
    String tableId = (String) parameterProvider.getParameter("tableId"); //$NON-NLS-1$
    String columnId = (String) parameterProvider.getParameter("columnId"); //$NON-NLS-1$
    String searchStr = (String) parameterProvider.getParameter("searchStr"); //$NON-NLS-1$

    ByteArrayOutputStream xactionOutputStream = new ByteArrayOutputStream();
    createMQLQueryActionSequence(domainId, modelId, tableId, columnId, searchStr, xactionOutputStream, userSession
        .getName());
    Document document = DocumentHelper.createDocument();
    document.setXMLEncoding(LocaleHelper.getSystemEncoding());
    Element resultsElement = document.addElement("results"); //$NON-NLS-1$

    IRuntimeContext runtimeContext = AdhocWebService.executeActionSequence(xactionOutputStream.toString(),
        "mqlQuery.xaction", new SimpleParameterProvider(), userSession, new ByteArrayOutputStream()); //$NON-NLS-1$
    if (runtimeContext.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS) {
      IActionParameter actionParameter = runtimeContext.getOutputParameter("query_result"); //$NON-NLS-1$
      IPentahoResultSet pentahoResultSet = actionParameter.getValueAsResultSet();
      TreeSet treeSet = new TreeSet();
      for (int i = 0; i < pentahoResultSet.getRowCount(); i++) {
        Object[] rowValues = pentahoResultSet.getDataRow(i);
        if (rowValues[0] != null) {
          treeSet.add(rowValues[0]);
        }
      }
      for (Iterator iterator = treeSet.iterator(); iterator.hasNext();) {
        resultsElement.addElement("row").setText(iterator.next().toString()); //$NON-NLS-1$
      }
      runtimeContext.dispose();
    }
    WebServiceUtil.writeString(outputStream, document.asXML(), wrapWithSoap);
  }

  public void getTemplateReportSpec(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws AdhocWebServiceException, IOException {
    
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String reportSpecName = parameterProvider.getStringParameter("reportSpecPath", null); //$NON-NLS-1$
    Document reportSpecDoc = null;
    if ( !StringUtils.isEmpty( reportSpecName ) ) {
      try {
        reportSpecName = AdhocWebService.WAQR_REPOSITORY_PATH + reportSpecName;
        reportSpecDoc = repository.getResourceAsDocument(reportSpecName);
      } catch (IOException ex) {
        String msg = Messages.getString("AdhocWebService.ERROR_0004_FAILED_TO_LOAD_REPORTSPEC", reportSpecName);//$NON-NLS-1$
        throw new AdhocWebServiceException(msg, ex);
      }
    } else {
      String msg = Messages.getString("AdhocWebService.ERROR_0005_MISSING_REPORTSPEC_NAME"); //$NON-NLS-1$
      throw new AdhocWebServiceException(msg);
    }
    WebServiceUtil.writeDocument(outputStream, reportSpecDoc, wrapWithSoap);
  }

  public void getWaqrReportSpecDoc(final IParameterProvider parameterProvider, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSoap) throws AdhocWebServiceException, IOException {
    
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String solution = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$
    String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$
    String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$
    
    Document reportSpecDoc = null;
    if ( !StringUtils.isEmpty( solution ) && (null != path)
        && !StringUtils.isEmpty( filename ) ) {
      try {
        String filePath = ActionInfo.buildSolutionPath(solution, path, filename);
        reportSpecDoc = repository.getResourceAsDocument(filePath);
      } catch (IOException ex) {
        String msg = Messages.getString("AdhocWebService.ERROR_0004_FAILED_TO_LOAD_REPORTSPEC",//$NON-NLS-1$
            filename );
        throw new AdhocWebServiceException(msg, ex);
      }
    } else {
      String msg = Messages.getString("AdhocWebService.ERROR_0005_MISSING_REPORTSPEC_NAME"); //$NON-NLS-1$
      throw new AdhocWebServiceException(msg);
    }
    WebServiceUtil.writeDocument(outputStream, reportSpecDoc, wrapWithSoap);
  }
  
  private static String getErrorHtml(HttpServletRequest request, final Exception e, String errorMsg) {
    errorMsg = StringEscapeUtils.escapeXml(errorMsg);
    
    StringBuffer b = new StringBuffer();
    List<String> msgList = new LinkedList<String>();
    msgList.add( errorMsg );
    msgList.add( Messages.getString( "AdhocWebService.REASON") ); //$NON-NLS-1$
    if ( null != e.getMessage() )
    {
      msgList.add( e.getMessage() );
    }
    else
    {
      msgList.add( e.getClass().getName() );
    }
    PentahoSystem.getMessageFormatter(PentahoHttpSessionHelper.getPentahoSession(request)).formatErrorMessage( "text/html", Messages.getString( "AdhocWebService.HEADER_ERROR_PAGE"),//$NON-NLS-1$//$NON-NLS-2$
        msgList, b );
    
    return b.toString();
  }

  private static int getSaveErrorStatus(final int[] statusAr) {
    for (int status : statusAr) {
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        return status;
      }
    }
    return ISolutionRepository.FILE_ADD_SUCCESSFUL;
  }

  private static boolean isWaqrFilename(final String filename) {
    return filename.matches(".*\\.waqr\\..*"); //$NON-NLS-1$
  }

  public Document getSolutionRepositoryDoc( final String solutionName, final String path, final IPentahoSession userSession ) {

    Document solutionRepositoryDoc = null;
    ICacheManager cacheManager = PentahoSystem.getCacheManager( userSession );
    if ( null != cacheManager )
    {
      Map solutionRepositoryFolderMap = (Map)cacheManager.getFromSessionCache(userSession,
          AdhocWebService.SOLUTION_NAVIGATION_DOCUMENT_MAP );
      if ( solutionRepositoryFolderMap == null ) {
        solutionRepositoryFolderMap = new HashMap();
        cacheManager.putInSessionCache(userSession, AdhocWebService.SOLUTION_NAVIGATION_DOCUMENT_MAP, solutionRepositoryFolderMap );
      }
      String cacheKey = solutionName + "/" + path; //$NON-NLS-1$
      solutionRepositoryDoc = (Document)solutionRepositoryFolderMap.get( cacheKey );
      if ( solutionRepositoryDoc == null ) {
        solutionRepositoryDoc = createSolutionRepositoryDoc( solutionName, path, userSession );
        solutionRepositoryFolderMap.put( cacheKey, solutionRepositoryDoc );
      }
    }
    else
    {
      solutionRepositoryDoc = createSolutionRepositoryDoc( solutionName, path, userSession );
    }
    // TODO sbarkdull, clean up
    //String strXml = solutionRepositoryDoc.asXML();

    return solutionRepositoryDoc;
  }

  /**
   * Used in conjunction with the xml document returned by getFullSolutionDoc()
   * @param element
   */
  private static void removeChildElements( final Element element ) {
    if (element.getName().equals("leaf")) { //$NON-NLS-1$
      List childElements = element.elements();
      for (Iterator iter = childElements.iterator(); iter.hasNext();) {
        Element childElement = (Element) iter.next();
        if (!childElement.getName().equals("leafText") //$NON-NLS-1$
            && !childElement.getName().equals("path")) { //$NON-NLS-1$
          childElement.detach();
        }
      }
    } else {
      List childElements = element.elements();
      for (Iterator iter = childElements.iterator(); iter.hasNext();) {
        Element childElement = (Element) iter.next();
        if (childElement.getName().equals("leaf")) { //$NON-NLS-1$
          List grandChildren = childElement.elements();
          for (Iterator iter2 = grandChildren.iterator(); iter2.hasNext();) {
            Element grandChildElement = (Element) iter2.next();
            if (!grandChildElement.getName().equals("leafText") //$NON-NLS-1$
                && !grandChildElement.getName().equals("path")) { //$NON-NLS-1$
              grandChildElement.detach();
            }
          }
        } else if (childElement.getName().equals("branch")) { //$NON-NLS-1$
          List grandChildren = childElement.elements();
          for (Iterator iter2 = grandChildren.iterator(); iter2.hasNext();) {
            Element grandChildElement = (Element) iter2.next();
            if (!grandChildElement.getName().equals("branchText")) { //$NON-NLS-1$
              grandChildElement.detach();
            }
          }
        } else if (!childElement.getName().equals("branchText")) { //$NON-NLS-1$            
          childElement.detach();
        }
      }
    }
  }
  
  private static Element getFolderElement( final Document doc, final String folderPath ) {
    
    // looks like: //branch[@id='/stuff/stuff2' and @isDir='true']
    String folderXPath = "//" + SolutionReposHelper.BRANCH_NODE_NAME //$NON-NLS-1$
      + "[@" + SolutionReposHelper.ID_ATTR_NAME + "='" + folderPath + "' and @" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      + SolutionReposHelper.IS_DIR_ATTR_NAME + "='true']"; //$NON-NLS-1$
    Element folderElement = (Element)doc.selectSingleNode( folderXPath );
    return folderElement;
  }

  private Document getFullSolutionDoc( final IPentahoSession userSession ) {
    
    Document fullDoc = null;
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    ICacheManager cacheManager = PentahoSystem.getCacheManager( userSession );

    if ( null != cacheManager )
    {
      fullDoc = (Document) cacheManager.getFromSessionCache(userSession, AdhocWebService.FULL_SOLUTION_DOC );
      if (fullDoc == null) {
        fullDoc = repository.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, null);
        cacheManager.putInSessionCache(userSession, AdhocWebService.FULL_SOLUTION_DOC, fullDoc);
      }
    }
    else
    {
      fullDoc = repository.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, null);
    }
    return fullDoc;
  }

  private Document getWaqrRepositoryDoc( final String folderPath, final IPentahoSession userSession ) throws AdhocWebServiceException {

    if ( StringUtil.doesPathContainParentPathSegment( folderPath )) {
      String msg = Messages.getString( "AdhocWebService.ERROR_0011_FAILED_TO_LOCATE_PATH", folderPath ); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
    String solutionRepositoryName = AdhocWebService.getSolutionRepositoryName( userSession );

    String path = "/" + solutionRepositoryName + AdhocWebService.WAQR_REPOSITORY_PATH; //$NON-NLS-1$
    if (!folderPath.equals("/")) { //$NON-NLS-1$
      path += folderPath;
    }

    Document fullDoc = getFullSolutionDoc( userSession );
    
    Element folderElement = AdhocWebService.getFolderElement( fullDoc, path );
    Document systemDoc = null;
    if ( folderElement != null ) {
      Element clonedFolderElement = (Element)folderElement.clone();
      AdhocWebService.removeChildElements( clonedFolderElement );
      systemDoc = DocumentHelper.createDocument((Element) clonedFolderElement.detach() );
      systemDoc.setXMLEncoding(LocaleHelper.getSystemEncoding());
    } else {
      String msg = Messages.getString( "AdhocWebService.ERROR_0011_FAILED_TO_LOCATE_PATH", folderPath ); //$NON-NLS-1$
      throw new AdhocWebServiceException( msg );
    }
    // TODO sbarkdull
    //String tmp = systemDoc.asXML();
    return systemDoc;
  }
  
  /**
   * @param IPentahoSession userSession
   */
  private Document createSolutionRepositoryDoc( final String solutionName, final String path, final IPentahoSession userSession ) {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    Document document = repository.getNavigationUIDocument( solutionName, path, ISolutionRepository.ACTION_EXECUTE );
    
    return document;
  }

  /**
   * @param IPentahoSession userSession
   */
  private void invalidateSolutionRepositoryTree(final IPentahoSession userSession)
  {
    ICacheManager cacheManager = PentahoSystem.getCacheManager( userSession );
    if ( null != cacheManager )
    {
      cacheManager.removeFromSessionCache( userSession, AdhocWebService.SOLUTION_NAVIGATION_DOCUMENT_MAP );
    }
  }

  /**
   * Get the solution repository name, for instance, "pentaho-solutions".
   * @param userSession
   * @return String containing the name of the solution repository
   */
  private static String getSolutionRepositoryName(final IPentahoSession userSession)
  {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    ISolutionFile rootFolder = repository.getRootFolder();
    return rootFolder.getSolution();
  }
  
  /**
   * If fullFilename has ".waqr.xreportspec" on the end of it,
   * remove it. Otherwise just return fullFilename
   * @param fullFilename
   * @return
   */
  private static String getBaseFilename( final String fullFilename )
  {
    Matcher m = AdhocWebService.BASE_WAQR_FILENAME_PATTERN.matcher( fullFilename );
    String baseName = fullFilename;
    if (m.matches()) {
      baseName = m.group(1);
    }
    return baseName;
  }

  // should be moved to AdhocWebServiceTest, but the solvable problem of calling
  // private methods needs to be addressed. don't have time at the moment, dude.
  // to run this, you need to have -ea on the JVM command line
  public static void main( String[] args ) {
    Set<Integer> invalid = new HashSet<Integer>();
    invalid.add( ColumnWidth.TYPE_WIDTH_PERCENT );
    invalid.add( ColumnWidth.TYPE_WIDTH_CM );
    invalid.add( ColumnWidth.TYPE_WIDTH_INCHES );
    invalid.add( ColumnWidth.TYPE_WIDTH_POINTS );
    assert !isSetConsistent( invalid ) : "invalid set should fail"; //$NON-NLS-1$

    Set<Integer> invalid2 = new HashSet<Integer>();
    invalid2.add( ColumnWidth.TYPE_WIDTH_PERCENT );
    invalid2.add( ColumnWidth.TYPE_WIDTH_CM );
    invalid2.add( ColumnWidth.TYPE_WIDTH_POINTS );
    assert !isSetConsistent( invalid2 ) : "invalid2 set should fail"; //$NON-NLS-1$
    
    Set<Integer> validPercent = new HashSet<Integer>();
    validPercent.add( ColumnWidth.TYPE_WIDTH_PERCENT );
    assert isSetConsistent( validPercent ) : "validPercent set should succeed"; //$NON-NLS-1$

    Set<Integer> validMeasures = new HashSet<Integer>();
    validMeasures.add( ColumnWidth.TYPE_WIDTH_CM );
    validMeasures.add( ColumnWidth.TYPE_WIDTH_INCHES );
    validMeasures.add( ColumnWidth.TYPE_WIDTH_POINTS );
    assert isSetConsistent( validMeasures ) : "validMeasures set should succeed"; //$NON-NLS-1$

    Set<Integer> validMeasures2    = new HashSet<Integer>();
    validMeasures2.add( ColumnWidth.TYPE_WIDTH_INCHES );
    validMeasures2.add( ColumnWidth.TYPE_WIDTH_POINTS );
    assert isSetConsistent( validMeasures2 ) : "validMeasures2 set should succeed"; //$NON-NLS-1$

    Set<Integer> validMeasures3    = new HashSet<Integer>();
    validMeasures3.add( ColumnWidth.TYPE_WIDTH_POINTS );
    assert isSetConsistent( validMeasures3 ) : "validMeasures3 set should succeed"; //$NON-NLS-1$

    Set<Integer> validEmtpy = new HashSet<Integer>();
    assert isSetConsistent( validEmtpy ) : "validEmtpy set should succeed"; //$NON-NLS-1$
    
    System.out.println( "looks like it worked." );

  }
}