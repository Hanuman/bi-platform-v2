package org.pentaho.test.platform.web;


import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.pentaho.platform.util.web.HttpUtil;
import org.pentaho.test.platform.engine.core.BaseTest;

public class HttpUtilTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/web/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if(file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;  
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
      return ALT_SOLUTION_PATH;
    }
    
  }

    public void testUtil() {
        startTest();
        // This should succeed
        String url = "http://www.pentaho.org/demo/news.html"; //$NON-NLS-1$
        String queryString = "http://www.pentaho.com/pentaho/ViewAction?solution=samples&path=analysis&action=query1.xaction"; //$NON-NLS-1$
        HttpClient client = HttpUtil.getClient();
        String urlContent = HttpUtil.getURLContent(url);
        HttpClientParams params = client.getParams();
        params.setBooleanParameter("isDone", true); //$NON-NLS-1$

        System.out.println("Content of the URL : " + urlContent); //$NON-NLS-1$
        try {
          StringBuffer nsb = new StringBuffer();
          if(HttpUtil.getURLContent(url, nsb));  
          System.out.println("String buffer has : " + nsb); //$NON-NLS-1$
        }
        catch(Exception e)
        {
          e.printStackTrace();          
          System.out.println("Exception caught"); //$NON-NLS-1$
        }

        try {
          InputStream is = HttpUtil.getURLInputStream(url);
          Reader reader = HttpUtil.getURLReader(url);
          is.close();
          reader.close();
        }
        catch(Exception e) {
          e.printStackTrace();          
          System.out.println("Exception caught"); //$NON-NLS-1$
        }
        
        Map map = HttpUtil.parseQueryString(queryString);
        
        System.out.println("Map's Contents are : " + map.toString()); //$NON-NLS-1$
        try {
          StringBuffer nsb = new StringBuffer();
          HttpUtil.getURLContent_old(url, nsb);  
          System.out.println("Old String buffer has : " + nsb); //$NON-NLS-1$
        }
        catch(Exception e)  {
          e.printStackTrace();
          System.out.println("Exception caught" ); //$NON-NLS-1$
        }        

        finishTest();
    }


    public static void main(String[] args) {
      HttpUtilTest test = new HttpUtilTest();
        test.setUp();
        try {
            test.testUtil();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }

}
