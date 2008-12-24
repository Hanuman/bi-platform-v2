package org.pentaho.platform.engine.services.solution;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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

  private static final Map<String,List<String>> loadedFrom = new HashMap<String,List<String>>();

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
		  FilenameFilter filter = new WildcardFileFilter( "*.jar" ); //$NON-NLS-1$
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
  
  public static List<String> listLoadedJars() {
    List<String> jarList = new ArrayList<String>();
    jarList.addAll( loadedFrom.keySet() );
    return jarList;
  }
  
  public static boolean unloadJar( String jarLocalName ) {
    List<String> classList = loadedFrom.get( jarLocalName );
    if( classList == null || classList.size() == 0 ) {
      // we didn't load any classes from this jar so there is nothing to do
      return false;
    }
    for( String className : classList ) {
      String key = jarLocalName + ISolutionRepository.SEPARATOR + className;
      System.out.println( "removing class: "+key );
      SolutionClassLoader.resourceMap.remove(key);
    }
    loadedFrom.remove( jarLocalName );
    int idx = 0;
    int jarIdx = -1;
    for( JarFile jar : jars ) {
      if( jar.getName().endsWith( jarLocalName ) ) {
        jarIdx = idx;
        break;
      }
      idx++;
    }
    if( jarIdx != -1 ) {
      JarFile file = jars.remove( jarIdx );
      try {
        file.close();
      } catch (IOException e) {
        // ignore this, we tried...
      }
      file = null;
    }
    return true;
  }
  
  private String getJarLocalName( String jarName ) {
    int idx = jarName.indexOf( path );
    if( idx != -1 ) {
      return jarName.substring( idx );
    } else {
      return jarName;
    }
  }
  
  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An input stream for reading the resource, or <code>null</code> if the resource could not be found 
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
      try {
        String entryName = name;
        String extension = ""; //$NON-NLS-1$
        if( entryName.endsWith( ".class" ) ) { //$NON-NLS-1$
          extension = ".class"; //$NON-NLS-1$
        }
        else if( entryName.endsWith( ".xml" ) ) { //$NON-NLS-1$
          extension = ".xml"; //$NON-NLS-1$
        }
        else if( entryName.endsWith( ".properties" ) ) { //$NON-NLS-1$
          extension = ".properties"; //$NON-NLS-1$
        }
        entryName = entryName.substring( 0, entryName.length() - extension.length() );
        entryName = entryName.replace('.', '/');
        if( "".equals( extension) ) { //$NON-NLS-1$
          entryName += ".class"; //$NON-NLS-1$
        } else {
          entryName += extension;
        }
    	  for( JarFile jar: jars ) {
			  ZipEntry entry = jar.getEntry(entryName);
			  if( entry != null ) {
			    String jarKey = getJarLocalName( jar.getName() );
			    List<String> classList = loadedFrom.get( jarKey );
			    if( classList == null ) {
			      classList = new ArrayList<String>();
			      loadedFrom.put( jarKey, classList );
			    }
			    classList.add( name );
		      System.out.println( "adding class: "+jarKey + ISolutionRepository.SEPARATOR + name );
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
    InputStream in = null;
    try {
      try {
        
        String key = path + ISolutionRepository.SEPARATOR + name;
        classBytes = SolutionClassLoader.resourceMap.get(key);
        if (classBytes == null) {
          in = getResourceAsStream( name );
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
    } finally {
      if( in != null ) {
        try {
          in.close();
        } catch (IOException e) {
          
        }
      }
    }
  }

  public static void clearResourceCache() {
	  SolutionClassLoader.resourceMap.clear();
  }

}
