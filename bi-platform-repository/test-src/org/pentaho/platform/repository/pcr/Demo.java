package org.pentaho.platform.repository.pcr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../bi-platform-sample-solution/system/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
public class Demo implements ApplicationContextAware {

  private IPentahoContentRepository pentahoContentRepository;

  @Test
  public void testCreateRunResultFile() throws Exception {
    // perform various startup operations such as creation of default folder hierarchy
    pentahoContentRepository.startup();
    // simulate interactive login
    login();
    // create the user's home folder if necessary
    pentahoContentRepository.createUserHomeFolderIfNecessary();
    // encoding of content
    String encoding = "UTF-8";
    // stream for Pentaho content repository to read from during file creation
    InputStream dataStream = new ByteArrayInputStream(
        "<b>Hello</b> <em>World</em><span style='font-size: 48px'>!</span>".getBytes(encoding));
    // MIME type of content
    String runResultMimeType = "text/html";
    // file name
    String fileName = "helloworld.xaction";
    // path to parent folder
    String parentFolderPath = "/pentaho/home/suzy";
    // run arguments 
    Map<String, String> runArguments = new HashMap<String, String>();
    runArguments.put("testKey", "testValue");
    // get the parent folder
    RepositoryFile parentFolder = pentahoContentRepository.getFile(parentFolderPath);
    // create the payload of the file
    RunResultRepositoryFileContent content = new RunResultRepositoryFileContent(dataStream, encoding,
        runResultMimeType, runArguments);
    // create the file
    pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(fileName).build(), content);
    // read the file using its absolute path
    RepositoryFile foundFile = pentahoContentRepository.getFile(parentFolderPath + RepositoryFile.SEPARATOR + fileName);
    // get file payload for purpose of displaying it
    RunResultRepositoryFileContent contentFromRepo = pentahoContentRepository.getContentForRead(foundFile,
        RunResultRepositoryFileContent.class);
    // print out the payload
    System.out.println(IOUtils.toString(contentFromRepo.getData()));
  }

  private void login() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
  }

  private static final Authentication AUTHENTICATION_SUZY;

  static {
    final String password = "password";
    final GrantedAuthority[] regularAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("Authenticated") };

    UserDetails suzy = new User("suzy", password, true, true, true, true, regularAuthorities);

    AUTHENTICATION_SUZY = new UsernamePasswordAuthenticationToken(suzy, password, regularAuthorities);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    pentahoContentRepository = (IPentahoContentRepository) applicationContext.getBean("pentahoContentRepository");
  }

  @BeforeClass
  public static void setUp() throws Exception {
    FileUtils.deleteQuietly(new File("/tmp/repository"));
    FileUtils.deleteQuietly(new File("/tmp/workspaces"));
    FileUtils.deleteQuietly(new File("/tmp/version"));
  }

  public static void main(String[] args) {
    org.junit.runner.JUnitCore.main(Demo.class.getName());
  }

}
