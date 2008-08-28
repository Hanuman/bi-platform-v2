package org.pentaho.test.platform.plugin;

import java.io.File;
import java.util.Locale;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionPublisher;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

public class PublishTest extends BaseTest {

  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }

  }

  public void testSolutionPublish() {
    startTest();

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel(getLoggingLevel());
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    publisher.publish(session, getLoggingLevel());
    assertTrue(publisher != null);
    finishTest();
  }

  public void testSolutionPublishI18N() {
    startTest();

    Locale tmpLocale = LocaleHelper.getLocale();
    // Try a different locale from the default
    String localeLanguage = "fr"; //$NON-NLS-1$
    String localeCountry = "FR"; //$NON-NLS-1$
    if (localeLanguage != null && !"".equals(localeLanguage) && localeCountry != null && !"".equals(localeCountry)) { //$NON-NLS-1$ //$NON-NLS-2$
      Locale locales[] = Locale.getAvailableLocales();
      if (locales != null) {
        for (int i = 0; i < locales.length; i++) {
          if (locales[i].getLanguage().equals(localeLanguage) && locales[i].getCountry().equals(localeCountry)) {
            LocaleHelper.setLocale(locales[i]);
            break;
          }
        }
      }
    }

    SolutionPublisher publisher = new SolutionPublisher();
    publisher.setLoggingLevel(getLoggingLevel());
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    publisher.publish(session, getLoggingLevel());
    assertTrue(publisher != null);
    // now set the locale back again
    LocaleHelper.setLocale(tmpLocale);
    finishTest();
  }

  /*    public void testWorkflowPublish() {
          startTest();
          SharkPublisher publisher = new SharkPublisher();
          publisher.setLoggingLevel(getLoggingLevel());
          StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
          publisher.setLoggingLevel(getLoggingLevel());
          publisher.publish(session);
          assertTrue(publisher != null);
          finishTest();
      }
  */
  public static void main(String[] args) {
    PublishTest test = new PublishTest();
    test.setUp();
    try {
      test.testSolutionPublish();
      test.testSolutionPublishI18N();
      // test.testWorkflowPublish();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
