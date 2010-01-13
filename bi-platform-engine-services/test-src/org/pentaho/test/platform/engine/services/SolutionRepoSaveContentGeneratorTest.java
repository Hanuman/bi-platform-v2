package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.solution.SolutionRepoSaveContentGenerator;
import org.pentaho.test.platform.engine.core.BaseTest;

import junit.framework.TestCase;

@SuppressWarnings({"all"})
public class SolutionRepoSaveContentGeneratorTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
       return SOLUTION_PATH;  
  }

  public void testLogger() {
    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    assertNotNull( "Logger is null", cg.getLogger() );
  }
  
  public void testMessages() {

    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0001_NO_FILEPATH").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0002_NO_STATE").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0003_BAD_PATH","").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0004_CANNOT_REPLACE").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0005_CREDENTIALS").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.ERROR_0006_SAVE_FAILED").startsWith("!") );
    assertFalse( Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE").startsWith("!") );
    
  }
  
  public void testNoFilepath() throws Exception {
    
    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    
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
  
  public void testNoState() throws Exception {
    
    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    request.setParameter( "filepath" , "test-state/save1.ext");
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0002_NO_STATE") );
    
  }
  
  public void testNoType() throws Exception {
    
    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    request.setParameter( "filepath" , "");
    request.setParameter( "state" , "state = {};");
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0007_NO_TYPE"), message );
    
  }

  public void testBadPath() throws Exception {
    
    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , "");
    request.setParameter( "state" , "state = {};");
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0003_BAD_PATH", ""), message );
    
  }

  public void testGoodSave() throws Exception {
    
    IPentahoSession session = new StandaloneSession( "test user" );
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();
    ContentInfo info = new ContentInfo();
    info.setIconUrl("test icon url");
    MockPluginManager.contentInfoByType.put("mytype", info);

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "test/good/test1.state.mytype";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    request.setParameter( "title" , "test title");
    request.setParameter( "description" , "test description");
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    
    String savedFile = MockSolutionRepository.files.get(path);
    
    assertNotNull( "file not saved", savedFile );
    assertTrue( "State is wrong", savedFile.contains( state ) );
    
    assertTrue( "author is wrong", savedFile.contains( "<author><![CDATA[test user]]></author>" ) );
    assertTrue( "title is wrong", savedFile.contains( "<title><![CDATA[test title]]></title>" ) );
    assertTrue( "description is wrong", savedFile.contains( "<description><![CDATA[test description]]></description>" ) );
    assertTrue( "icon is wrong", savedFile.contains( "<icon><![CDATA[test icon url]]></icon>" ) );
  }
  
  public void testGoodSave2() throws Exception {
    
    IPentahoSession session = new StandaloneSession( "test user" );
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "test/good/test1.state.mytype";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    // don't set tile and description, they should default to empty strings
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    
    String savedFile = MockSolutionRepository.files.get(path);
    
    assertNotNull( "file not saved", savedFile );
    assertTrue( "State is wrong", savedFile.contains( state ) );
    
    assertTrue( "author is wrong", savedFile.contains( "<author><![CDATA[test user]]></author>" ) );
    assertTrue( "title is wrong", savedFile.contains( "<title><![CDATA[]]></title>" ) );
    assertTrue( "description is wrong", savedFile.contains( "<description><![CDATA[]]></description>" ) );
    
  }
  
  public void testBadReplace() throws Exception {
    
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "test/good/test1.state";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    assertTrue( "State is wrong", MockSolutionRepository.files.get(path+".mytype").contains( state ) );
    
    // try to save over it
    String state2 = "state = { message : 'new state' };";
    request.setParameter( "state" , state2);
    out = new ByteArrayOutputStream();
    cg.createContent(out);
    
    message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0004_CANNOT_REPLACE") );
    assertTrue( "State is wrong", MockSolutionRepository.files.get(path+".mytype").contains( state ) );

  }
  
  public void testGoodReplace() throws Exception {
    
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    request.setParameter( "type" , "mytype");
    String state = "state = { message : 'test state' };";
    String path = "test/good/test1.state.mytype";
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    assertTrue( "State is wrong", MockSolutionRepository.files.get(path).contains( state ) );
    
    // try to save over it
    String state2 = "state = { message : 'new state' };";
    request.setParameter( "state" , state2);
    request.setParameter( "replace" , "true");
    out = new ByteArrayOutputStream();
    cg.createContent(out);
    
    message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getString("SolutionRepoSaveContentGenerator.USER_FILE_SAVE") );
    assertTrue( "State is wrong", MockSolutionRepository.files.get(path).contains( state2 ) );

  }

  public void testBadCredential() throws Exception {
    
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "baduser/good/test1.state";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "Message is wrong", message, Messages.getInstance().getErrorString("SolutionRepoSaveContentGenerator.ERROR_0005_CREDENTIALS") );
    assertEquals( "State is wrong", null, MockSolutionRepository.files.get(path) );
    
  }
  
  public void testFailedSave() throws Exception {
    
    IPentahoSession session = new StandaloneSession();
    PentahoSessionHolder.setSession(session);
    
    MockSolutionRepository.files.clear();

    SolutionRepoSaveContentGenerator cg = new SolutionRepoSaveContentGenerator();
    cg.setSession(session);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    SimpleParameterProvider request = new SimpleParameterProvider();
    Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
    parameterProviders.put( "request" , request ); //$NON-NLS-1$
    
    String state = "state = { message : 'test state' };";
    String path = "bogus/good/test1.state";
    request.setParameter( "type" , "mytype");
    request.setParameter( "filepath" , path);
    request.setParameter( "state" , state);
    
    cg.setParameterProviders(parameterProviders);
    cg.createContent(out);
    
    String message = new String( out.toByteArray() );
    assertEquals( "State is wrong", null, MockSolutionRepository.files.get(path) );
    
  }
  


}
