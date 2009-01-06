/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.collections.IteratorUtils;
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
  /*
  public static synchronized void unloadClasses() {
	  resourceMap.clear();
	  jars.clear();
  }
  */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

    try {
      byte bytes[] = getResourceAsBytes( name );
      if( bytes == null ) {
        throw new ClassNotFoundException(name);
      }
      return defineClass( name, bytes, 0, bytes.length );
    } catch (IOException e ) {
      throw new ClassNotFoundException(name, e);
    }
	  
  }
  
  public static List<String> listLoadedJars() {
    List<String> jarList = new ArrayList<String>();
    jarList.addAll( loadedFrom.keySet() );
    return jarList;
  }
/*  
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
*/  
  private String getJarLocalName( String jarName ) {
    String name = jarName.replace('\\', ISolutionRepository.SEPARATOR);
    int idx = name.indexOf( path );
    return name.substring( idx );
  }
  
  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An input stream for reading the resource, or <code>null</code> if the resource could not be found 
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
      try {
        String entryName = prepareEntryName( name );
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
        // This situation indicates the resource was found but could not be
        // opened.
          if (SolutionClassLoader.logger.isTraceEnabled()) {
          	SolutionClassLoader.logger.trace(Messages.getString("DbRepositoryClassLoader.RESOURCE_NOT_FOUND", name)); //$NON-NLS-1$
          }

        }
      // Return null to indicate that the resource could not be found (and this is ok) 
      return null;
  }

  @Override
  protected Enumeration<URL> findResources( String name) throws IOException {
    
    List<URL> urls = getResourceList( name, true );
    @SuppressWarnings({"unchecked"})
    Enumeration<URL> enumer = IteratorUtils.asEnumeration(urls.iterator());
    return enumer;
  }

  @Override
  protected URL findResource( String name) {
    
    try {
      List<URL> urls = getResourceList( name, false );
      if( urls.size() > 0 ) {
        return urls.get( 0 );
      }
    } catch ( MalformedURLException e ) {
      // ignored
    }
    return null;
  }

  private String prepareEntryName( String name ) {
    String entryName = name;
    String extension = ""; //$NON-NLS-1$
    if( entryName.endsWith( ".xml" ) ) { //$NON-NLS-1$
      extension = ".xml"; //$NON-NLS-1$
    }
    else if( entryName.endsWith( ".class" ) ) { //$NON-NLS-1$
      extension = ".class"; //$NON-NLS-1$
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
    return entryName;
  }
  
  private List<URL> getResourceList(final String name, boolean multiple) throws MalformedURLException {
    List<URL> urls = new ArrayList<URL>();
    String entryName = prepareEntryName( name );
    for( JarFile jar: jars ) {
      ZipEntry entry = jar.getEntry(entryName);
      if( entry != null ) {
        String urlPath = "jar:file:"+jar.getName()+"!/"+name; //$NON-NLS-1$ //$NON-NLS-2$
        URL url = new URL( urlPath );
        urls.add( url );
        if( !multiple ) {
          return urls;
        }
      }
    }
    return urls;
  }

  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An byte array of the resource, or <code>null</code> if the resource could not be found 
   */
  protected byte[] getResourceAsBytes(final String name) throws IOException {

	  byte[] classBytes = null;
    InputStream in = null;
    try {
      String key = path + ISolutionRepository.SEPARATOR + name;
      classBytes = SolutionClassLoader.resourceMap.get(key);
      if (classBytes == null) {
        in = getResourceAsStream( name );
        if( in == null ) {
          return null;
        }
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
    } finally {
      if( in != null ) {
        try {
          in.close();
        } catch (IOException e) {
          
        }
      }
    }
    return classBytes;
  }
/*
  public static void clearResourceCache() {
	  SolutionClassLoader.resourceMap.clear();
  }
*/  
}
