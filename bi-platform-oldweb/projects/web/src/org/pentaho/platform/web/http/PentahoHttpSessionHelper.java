package org.pentaho.platform.web.http;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.session.PentahoHttpSession;

public class PentahoHttpSessionHelper {

  // list of the default locations to search for the solution repository
  private static final String DEFAULT_LOCATIONS[] = { "/eclipse/workspace/pentaho-solutions", //$NON-NLS-1$  Solution samples from CVS in the default eclipse workspace
      "/eclipse/workspace/pentaho-samples/solutions/test-solution" //$NON-NLS-1$  All Samples from CVS in the default eclipse workspace
  };
  
  public static IPentahoSession getPentahoSession(final HttpServletRequest request) {

    HttpSession session = request.getSession();
    IPentahoSession userSession = (IPentahoSession) session.getAttribute(IPentahoSession.PENTAHO_SESSION_KEY);
    LocaleHelper.setLocale(request.getLocale());
    if (userSession != null) {
      return userSession;
    }
    userSession = new PentahoHttpSession(request.getRemoteUser(), request.getSession(), request.getLocale(), userSession);
    // TODO sbarkdull delete following line?
    LocaleHelper.setLocale(request.getLocale());

    session.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession);
    return userSession;
  }

  public static String getSolutionPath(final ServletContext context) {
    File pentahoSolutions;

    // first try the web.xml setting
    String rootPath = context.getInitParameter("solution-path"); //$NON-NLS-1$
    if (StringUtils.isNotBlank(rootPath)) {
      pentahoSolutions = new File(rootPath);
      if (pentahoSolutions.exists() && pentahoSolutions.isDirectory()) {
        return rootPath;
      }
    }

    for (String element : DEFAULT_LOCATIONS) {
      pentahoSolutions = new File(element);
      if (pentahoSolutions.exists() && pentahoSolutions.isDirectory()) {
        try {
          return pentahoSolutions.getCanonicalPath();
        } catch (IOException e) {
          return pentahoSolutions.getAbsolutePath();
        }
      }
    }

    // now try the path to the WEB-INF to see if we find
    File file = new File(context.getRealPath("")); //$NON-NLS-1$
    while (file != null) {
      if (file.exists() && file.isDirectory()) {
        pentahoSolutions = new File(file.getAbsolutePath() + File.separator + "pentaho-solutions"); //$NON-NLS-1$
        if (pentahoSolutions.exists() && pentahoSolutions.isDirectory()) {
          try {
            return pentahoSolutions.getCanonicalPath();
          } catch (IOException e) {
            return pentahoSolutions.getAbsolutePath();
          }
        }
      }
      file = file.getParentFile();
    }
    return null;
  }
}
