package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.solution.SolutionUrlContentGenerator;
import org.pentaho.test.platform.engine.core.BaseTest;

import junit.framework.TestCase;

@SuppressWarnings({"all"})
public class SolutionUrlContentGeneratorTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
       return SOLUTION_PATH;  
  }

  public void testLogger() {
    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    assertNotNull( "Logger is null", cg.getLogger() );
  }
  
  public void testMessages() {

    assertFalse( Messages.getInstance().getString("SolutionURLContentGenerator.ERROR_0001_NO_FILEPATH").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionURLContentGenerator.ERROR_0002_CANNOT_HANDLE_TYPE").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionURLContentGenerator.ERROR_0003_RESOURCE_NOT_FOUND","").startsWith("!") );
    
  }

  public void testNoOutput() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    pathParams.setParameter( "path" , "solution/web/test.txt");
    cg.setOutputHandler(null);
    cg.setParameterProviders(parameterProviders);
    try {
      cg.createContent();
      assertFalse("Expected exception did not happen",true);
    } catch (InvalidParameterException e) {
      assertTrue(true);
    }

    String content = new String( out.toByteArray() );
    assertEquals( "Content is wrong", content, "" );
    
  }

  public void testNoStream() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    pathParams.setParameter( "path" , "solution/web/test.txt");
    IOutputHandler handler = new SimpleOutputHandler((OutputStream) null, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    try {
      cg.createContent();
      assertFalse("Expected exception did not happen",true);
    } catch (InvalidParameterException e) {
      assertTrue(true);
    }

    String content = new String( out.toByteArray() );
    assertEquals( "Content is wrong", content, "" );
    
  }

  public void testNoContentItem() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    pathParams.setParameter( "path" , "solution/web/test.txt");
    IOutputHandler handler = new SimpleOutputHandler((IContentItem) null, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    try {
      cg.createContent();
      assertFalse("Expected exception did not happen",true);
    } catch (InvalidParameterException e) {
      assertTrue(true);
    }

    String content = new String( out.toByteArray() );
    assertEquals( "Content is wrong", content, "" );
    
  }

  public void testNoPath() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();

    String content = new String( out.toByteArray() );
    assertEquals( "Content is wrong", content, "" );
    
  }
  
  public void testMissingFile() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    pathParams.setParameter( "path" , "/web/badpath/img.png");
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, "" );
    
  }

  public void testBadStaticType() throws Exception {
    
    MockSolutionRepository.files.clear();

    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    pathParams.setParameter( "path" , "/web/badpath/file");
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, "" );
    
  }
  
  public void testGoodStaticType() throws Exception {
    
    MockSolutionRepository.files.clear();
    String testContents = "test file contents";
    MockSolutionRepository.files.put( "solution/web/test.txt", testContents);
    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    pathParams.setParameter( "path" , "solution/web/test.txt");
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, testContents );
    
  }  
  public void testNonWebStaticType() throws Exception {
    
    MockSolutionRepository.files.clear();
    String testContents = "test file contents";
    String filepath = "solution/notweb/test.txt";
    MockSolutionRepository.files.put( filepath, testContents);
    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    pathParams.setParameter( "path" , filepath);
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, "" );
    
  }

  public void testXactionType() throws Exception {
    
    MockSolutionRepository.files.clear();
    String testContents = "test file contents";
    String filepath = "solution/web/test.xaction";
    MockSolutionRepository.files.put( filepath, testContents);
    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    
    pathParams.setParameter( "path" , filepath);
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, "" );
    
  }
  

  public void testContentGenerator() throws Exception {
    
    MockPluginManager.contentGeneratorByType.put("testgen", new MockContentGenerator() );
    MockSolutionRepository.files.clear();
    String testContents = "test file contents";
    String filepath = "solution/test.testgen";
    MockSolutionRepository.files.put( filepath, testContents);
    SolutionUrlContentGenerator cg = new SolutionUrlContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "path" , pathParams ); //$NON-NLS-1$
    parameterProviders.put( "request" , requestParams ); //$NON-NLS-1$
    pathParams.setParameter( "path" , filepath);
    
    IOutputHandler handler = new SimpleOutputHandler(out, false);
    cg.setOutputHandler(handler);
    cg.setParameterProviders(parameterProviders);
    cg.createContent();
    
    String content = new String( out.toByteArray() );
  
    assertEquals( "Content is wrong", content, "MockContentGenerator content" );
    
  }
}
