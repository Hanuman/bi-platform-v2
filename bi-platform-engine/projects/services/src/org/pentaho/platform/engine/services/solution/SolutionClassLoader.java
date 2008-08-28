package org.pentaho.platform.engine.services.solution;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class SolutionClassLoader extends ClassLoader {

  protected static final Log logger = LogFactory.getLog(SolutionClassLoader.class);

  private static final Map<String,byte[]> resourceMap = new HashMap<String,byte[]>();

  private String path;
  
  private static final List<JarFile> jars  = new ArrayList<JarFile>();

  public SolutionClassLoader(final String inPath, Object parent ) {
	  super( parent.getClass().getClassLoader() );
    path = inPath;
    catalogJars();
  }

  private void catalogJars() {
	  File folder = new File( PentahoSystem.getApplicationContext().getSolutionPath( path ) );
	  if( folder.exists() && folder.isDirectory() ) {
		  // get a list of all the JAR files
		  FilenameFilter filter = new WildcardFileFilter( "*.jar" );
		  File jarFiles[] = folder.listFiles( filter );
		  if( jarFiles != null && jarFiles.length > 0 ) {
			  for( File file : jarFiles ) {
				  try {
					  JarFile jar = new JarFile( file, true );
					  addJar( jar );
				  } catch (Exception e) {
					  Logger.error( getClass().toString(), "Could not load jar from solution: "+file.getAbsolutePath(), e );
				  }
			  }
		  }
	  }
  }
  
  public static synchronized void addJar( JarFile jar ) {
	  String name = jar.getName();
	  List<JarFile> jarsToRemove  = new ArrayList<JarFile>();
	  // remove this from the jar list if it exists
	  for( JarFile existingJar: jars ) {
		  if( name.equals( existingJar.getName() ) ) {
			  jarsToRemove.add(existingJar);
		  }
		  
	  }
	  for( JarFile jarToRemove: jarsToRemove ) {
		  jars.remove( jarToRemove );
	  }
	  // now add the new jar
	  jars.add(jar);
  }
  
  public static synchronized void unloadClasses() {
	  resourceMap.clear();
	  jars.clear();
  }
  
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

	  byte bytes[] = getResourceAsBytes( name );
	  if( bytes == null ) {
		  throw new ClassNotFoundException(name);
	  }
	  return defineClass( name, bytes, 0, bytes.length );
	  
  }
  
  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An input stream for reading the resource, or <code>null</code> if the resource could not be found 
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
      try {
    	  for( JarFile jar: jars ) {
			  String entryName = name.replace('.', '/')+".class";
			  ZipEntry entry = jar.getEntry(entryName);
			  if( entry != null ) {
				  return jar.getInputStream( entry );
			  }
    	  }
      } catch (Exception ignored) {
          // This situation indicates the resource could not be found. This is a common and correct situation 
          // and this exception should be ignored.
          if (SolutionClassLoader.logger.isTraceEnabled()) {
          	SolutionClassLoader.logger.trace(Messages.getString("DbRepositoryClassLoader.RESOURCE_NOT_FOUND", name)); //$NON-NLS-1$
          }

        }
      // Return null to indicate that the resource could not be found (and this is ok) 
      return null;
  }

  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An byte array of the resource, or <code>null</code> if the resource could not be found 
   */
  protected byte[] getResourceAsBytes(final String name) {

	  byte[] classBytes = null;
      try {
		  
		  String key = path + ISolutionRepository.SEPARATOR + name;
		  classBytes = (byte[]) SolutionClassLoader.resourceMap.get(key);
		  if (classBytes == null) {
			  InputStream in = getResourceAsStream( name );
			  ByteArrayOutputStream bin = new ByteArrayOutputStream( );
			  byte bytes[] = new byte[4096];
			  int n = in.read( bytes );
			  while( n != -1 ) {
				  bin.write( bytes, 0, n);
				  n = in.read( bytes );
			  }
			  classBytes = bin.toByteArray();
			  SolutionClassLoader.resourceMap.put(key, classBytes);
		  }
      } catch (Exception ignored) {
          // This situation indicates the resource could not be found. This is a common and correct situation 
          // and this exception should be ignored.
          if (SolutionClassLoader.logger.isTraceEnabled()) {
          	SolutionClassLoader.logger.trace(Messages.getString("DbRepositoryClassLoader.RESOURCE_NOT_FOUND", name)); //$NON-NLS-1$
          }

          // Return null to indicate that the resource could not be found (and this is ok) 
          return null;
        }
    return classBytes;
  }
  public static void clearResourceCache() {
	  SolutionClassLoader.resourceMap.clear();
  }

}
