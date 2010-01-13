package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.solution.SolutionRepoLoadContentGenerator;
import org.pentaho.platform.engine.services.solution.SolutionRepoSaveContentGenerator;
import org.pentaho.test.platform.engine.core.BaseTest;

import junit.framework.TestCase;

@SuppressWarnings({"all"})
public class SolutionRepoLoadContentGeneratorTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
       return SOLUTION_PATH;  
  }

  public void testLogger() {
    SolutionRepoLoadContentGenerator cg = new SolutionRepoLoadContentGenerator();
    assertNotNull( "Logger is null", cg.getLogger() );
  }
  
  public void testMessages() {

    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0001_NO_FILEPATH").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0003_BAD_PATH","").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoLoadContentGenerator.ERROR_0001_LOAD_FAILED").startsWith("!") );
    
  }
  
  public void testNoFilepath() throws Exception {
    
    SolutionRepoLoadContentGenerator cg = new SolutionRepoLoadContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);

    assertEquals( "Mime type is wrong", "text/text", cg.getMimeType() );

    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0001_NO_FILEPATH") );
    
  }
  
  public void testBadPath() throws Exception {
    
    SolutionRepoLoadContentGenerator cg = new SolutionRepoLoadContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , "");
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0003_BAD_PATH", ""), message );
    
  }

  public void testGoodLoad() throws Exception {
    
    // first do a save
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator saver = new SolutionRepoSaveContentGenerator();
    saver.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "test/good/test1.state.mytype";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    request.setParameter( "replace" , "true");
    
    saver.setParameterProviders(parameterProviders);
    saver.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    assertTrue( "State is wrong", MockSolutionRepository.files.get(path).contains( state ) );
    
    // now try to load it
    SolutionRepoLoadContentGenerator loader = new SolutionRepoLoadContentGenerator();
    
    out = new ByteArrayOutputStream();
    
    request = new SimpleParameterProvider();
    parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    request.setParameter( "filepath" , "test/good/test1.state.mytype");
    
    loader.setParameterProviders(parameterProviders);
    loader.createContent(out);
    
    message = new String( out.toByteArray() );
    assertEquals( "State is wrong", state, message );

  }

  public void testMissingDocument() throws Exception {
    
    // first do a save
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String path = "test/bad/test1.state.mytype";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    
    SolutionRepoLoadContentGenerator loader = new SolutionRepoLoadContentGenerator();
    
    out = new ByteArrayOutputStream();
    
    loader.setParameterProviders(parameterProviders);
    loader.createContent(out);
    
    String message = new String( out.toByteArray() );
    
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoLoadContentGenerator.ERROR_0001_LOAD_FAILED", "test/bad/test1.state.mytype") );

  }

  public void testBadDocument() throws Exception {
    
    // first do a save
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();
    
    MockSolutionRepository.files.put("test/bad/test1.state.mytype", "<oops/>");
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String path = "test/bad/test1.state.mytype";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    
    SolutionRepoLoadContentGenerator loader = new SolutionRepoLoadContentGenerator();
    
    out = new ByteArrayOutputStream();
    
    loader.setParameterProviders(parameterProviders);
    loader.createContent(out);
    
    String message = new String( out.toByteArray() );
    
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoLoadContentGenerator.ERROR_0001_LOAD_FAILED", "test/bad/test1.state.mytype") );

  }
  
}
