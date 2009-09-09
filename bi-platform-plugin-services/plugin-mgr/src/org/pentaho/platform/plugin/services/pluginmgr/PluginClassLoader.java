/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.plugin.services.pluginmgr;

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
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

/**
 * A custom implementation of {@link ClassLoader} for Pentaho Platform Plugins.
 * It is used to load plugin classes and aids in retrieving resources by providing
 * a root directory to search for resources related to plugins.
 * <p>
 * Note: {@link PluginClassLoader} will search for jar files in a 'lib' subdirectory
 * under the pluginDir provided in the constructor.
 * @author aphillips
 */
public class PluginClassLoader extends ClassLoader {

  protected final Log logger = LogFactory.getLog(PluginClassLoader.class);

  private final Map<String, byte[]> resourceMap = new HashMap<String, byte[]>();

  private final Map<String, List<String>> loadedFrom = new HashMap<String, List<String>>();

  private String pluginDir;

  private final List<JarFile> jars = new ArrayList<JarFile>();

  /**
   * Creates a class loader for loading plugin classes and discovering resources.
   * Jars must be located in [pluginDir]/lib.
   * @param pluginDir
   * @param parent
   */
  public PluginClassLoader(final File pluginDir, Object parent) {
    this(pluginDir.getAbsolutePath(), parent);
  }

  public PluginClassLoader(final String pluginDir, Object parent) {
    super(parent.getClass().getClassLoader());
    this.pluginDir = pluginDir.replace('\\', '/');
    catalogJars();
  }

  /**
   * Returns the absolute path to the root directory of the plugin to which this classloader is assigned
   * @return absolute path to the plugin root
   */
  public String getPluginAbsPath() {
    return pluginDir;
  }

  private void catalogJars() {
    __catalogJars(new File(pluginDir, "lib")); //$NON-NLS-1$
  }

  private void __catalogJars(File folder) {
    if (folder.exists() && folder.isDirectory()) {
      // get a list of all the JAR files
      FilenameFilter filter = new WildcardFileFilter("*.jar"); //$NON-NLS-1$
      File jarFiles[] = folder.listFiles(filter);
      if (jarFiles != null && jarFiles.length > 0) {
        for (File file : jarFiles) {
          try {
            JarFile jar = new JarFile(file, true);
            addJar(jar);
          } catch (Exception e) {
            Logger.warn(getClass().toString(), Messages.getString(
                "PluginClassLoader.WARN_COULD_NOT_LOAD_JAR", file.getAbsolutePath()), e); //$NON-NLS-1$
          }
        }
      }
    }
  }

  public synchronized void addJar(JarFile jar) {
    String name = jar.getName();
    List<JarFile> jarsToRemove = new ArrayList<JarFile>();
    // remove this from the jar list if it exists
    for (JarFile existingJar : jars) {
      if (name.equals(existingJar.getName())) {
        jarsToRemove.add(existingJar);
      }

    }
    for (JarFile jarToRemove : jarsToRemove) {
      jars.remove(jarToRemove);
    }
    // now add the new jar
    jars.add(jar);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

    try {
      byte bytes[] = getResourceAsBytes(name);
      if (bytes == null) {
        throw new ClassNotFoundException(name);
      }
      // BCHOW - Add package information
      int i = name.lastIndexOf('.');
      if (i != -1) {
        String pkgname = name.substring(0, i);
        // Check if package already loaded.
        Package pkg = getPackage(pkgname);
        if (pkg == null) {
          // Package not found
          definePackage(pkgname, null, null, null, null, null, null, null);
        }
      }      
      return defineClass(name, bytes, 0, bytes.length);
    } catch (IOException e) {
      throw new ClassNotFoundException(name, e);
    }

  }

  /*
   * This code will allow manifest information to be exposed via class packages.
   * To be enabled in Sugar.
   * Access to the manifest for a class will need to be made possible
   
  protected Class<?> __defineClass( String name, byte b[], int off, int len ) {
    
    int i = name.lastIndexOf('.');
    if (i != -1) {
      String pkgname = name.substring(0, i);
      // Check if package already loaded.
      Package pkg = getPackage(pkgname);
      Manifest man = res.getManifest();
      if (pkg == null) {
        // Package not found
        if (man != null) {
          definePackage(pkgname, man);
        } else {
          definePackage(pkgname, null, null, null, null, null, null, null);
        }
      }
    }    
    return super.defineClass(name, b, off, len);
  }
  
  protected Package definePackage(String name, Manifest man)
    throws IllegalArgumentException {

    String path = name.replace('.', '/').concat("/");
    String specTitle = null, specVersion = null, specVendor = null;
    String implTitle = null, implVersion = null, implVendor = null;

    Attributes attr = man.getAttributes(path);
    if (attr != null) {
      specTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
      specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
      specVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
      implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
      implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
      implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
    }
    attr = man.getMainAttributes();
    if (attr != null) {
      if (specTitle == null) {
        specTitle = attr.getValue(Name.SPECIFICATION_TITLE);
      }
      if (specVersion == null) {
        specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
      }
      if (specVendor == null) {
        specVendor = attr.getValue(Name.SPECIFICATION_VENDOR);
      }
      if (implTitle == null) {
        implTitle = attr.getValue(Name.IMPLEMENTATION_TITLE);
      }
      if (implVersion == null) {
        implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
      }
      if (implVendor == null) {
        implVendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
      }
    }
    return definePackage(name, specTitle, specVersion, specVendor,
        implTitle, implVersion, implVendor, null);
  }
  */
  public List<String> listLoadedJars() {
    List<String> jarList = new ArrayList<String>();
    jarList.addAll(loadedFrom.keySet());
    return jarList;
  }

  private String getJarLocalName(String jarName) {
    logger.debug("original jar path "+jarName); //$NON-NLS-1$
    String name = jarName.replace('\\', '/');
    logger.debug("finding jar name for path ["+name+"] relative to ["+pluginDir+"/lib]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    int idx = name.indexOf(pluginDir + "/lib"); //$NON-NLS-1$
    return name.substring(idx);
  }

  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An input stream for reading the resource, or <code>null</code> if the resource could not be found 
   */
  @Override
  public InputStream getResourceAsStream(final String name) {
      String entryName = prepareEntryName(name);
      for (JarFile jar : jars) {
        ZipEntry entry = jar.getEntry(entryName);
        if (entry != null) {
          String jarKey = getJarLocalName(jar.getName());
          List<String> classList = loadedFrom.get(jarKey);
          if (classList == null) {
            classList = new ArrayList<String>();
            loadedFrom.put(jarKey, classList);
          }
          classList.add(name);
          if(logger.isTraceEnabled()) {
            logger.trace("adding class: " + jarKey + '/' + name); //$NON-NLS-1$
          }
          try {
            return jar.getInputStream(entry);
          } catch (IOException e) {
            logger.warn(Messages.getString("PluginClassLoader.WARN_CLASS_NOT_REGISTERED", entryName), e); //$NON-NLS-1$
            return null;
          }
        }
      }
    //if we haven't found the resource in our jars, call super which will eventually call findResource
    return super.getResourceAsStream(name);
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {

    List<URL> urls = getResourceList(name, true);
    @SuppressWarnings( { "unchecked" })
    Enumeration<URL> enumer = IteratorUtils.asEnumeration(urls.iterator());
    return enumer;
  }

  @Override
  protected URL findResource(String name) {

    try {
      List<URL> urls = getResourceList(name, false);
      if (urls.size() > 0) {
        return urls.get(0);
      }
    } catch (MalformedURLException e) {
      // ignored
    }
    return null;
  }

  private String prepareEntryName(String name) {
    String entryName = name;
    String extension = ""; //$NON-NLS-1$
    if (entryName.endsWith(".xml")) { //$NON-NLS-1$
      extension = ".xml"; //$NON-NLS-1$
    } else if (entryName.endsWith(".class")) { //$NON-NLS-1$
      extension = ".class"; //$NON-NLS-1$
    } else if (entryName.endsWith(".properties")) { //$NON-NLS-1$
      extension = ".properties"; //$NON-NLS-1$
    }
    entryName = entryName.substring(0, entryName.length() - extension.length());
    entryName = entryName.replace('.', '/');
    if ("".equals(extension)) { //$NON-NLS-1$
      entryName += ".class"; //$NON-NLS-1$
    } else {
      entryName += extension;
    }
    return entryName;
  }

  private List<URL> getResourceList(final String name, boolean multiple) throws MalformedURLException {
    List<URL> urls = new ArrayList<URL>();
    String entryName = prepareEntryName(name);
    for (JarFile jar : jars) {
      ZipEntry entry = jar.getEntry(entryName);
      if (entry != null) {
        String urlPath = "jar:file:" + jar.getName() + "!/" + name; //$NON-NLS-1$ //$NON-NLS-2$
        
        URL url = new URL(urlPath);
        try {
//          System.err.println("trying "+url);
          url.openConnection().connect();
          urls.add(url);
        } catch (IOException e) {
        }
      }
    }
    
    //if resource was not found in jars, check the filesystem
    try {
      String filePath = new File(pluginDir, name).getAbsolutePath();
      URL url = new URL("file:" + filePath); //$NON-NLS-1$
//      System.err.println("trying "+url);
      url.openConnection().connect();
      urls.add(url);
    } catch (IOException e) {
    }
    return urls;
  }
  
  public boolean isPluginClass(Class<?> clazz) {
    boolean ret = false;
    for(JarFile jar : jars) {
      String searchEntry = clazz.getName().replace('.','/')+".class"; //$NON-NLS-1$
      if(jar.getJarEntry(searchEntry) != null) {
        ret = true;
      }
    }
    return ret;
  }

  @Override
  public java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> loadedClass = super.loadClass(name, resolve);

    //if a plugin class was loaded by a classloader other than an instance of PluginClassLoader, report a warning
    if (isPluginClass(loadedClass)
        && loadedClass.getClassLoader() != null
        && !PluginClassLoader.class.isAssignableFrom(loadedClass.getClassLoader().getClass())) {
      Logger.debug(this, "Class [" + loadedClass.getName() + "] is provided in the plugin libraries, but was not loaded by "  //$NON-NLS-1$//$NON-NLS-2$
          + PluginClassLoader.class.getSimpleName() + ".  It is being loaded by the classloader [" //$NON-NLS-1$
          + loadedClass.getClassLoader().getClass().getSimpleName()
          + "].  This is not necessarily an error, but this information can be useful in tracking down strange plugin behavior."); //$NON-NLS-1$
    }
    return loadedClass;
  }

  /**
   * Returns the requested resource as an InputStream.
   * @param name The resource name
   * @retruns An byte array of the resource, or <code>null</code> if the resource could not be found 
   */
  protected byte[] getResourceAsBytes(final String name) throws IOException {
    byte[] ret = __getResourceAsBytes(name, ""); //$NON-NLS-1$
    if(ret != null) {
      return ret;
    }
    return __getResourceAsBytes(name, "lib/"); //$NON-NLS-1$
  }
  
  protected byte[] __getResourceAsBytes(final String name, String pathPrefix) throws IOException {
    byte[] classBytes = null;
    InputStream in = null;
    try {
      String key = pluginDir + "/" + pathPrefix + name; //$NON-NLS-1$
      classBytes = resourceMap.get(key);
      if (classBytes == null) {
        in = getResourceAsStream(name);
        if (in == null) {
          return null;
        }
        ByteArrayOutputStream bin = new ByteArrayOutputStream();
        byte bytes[] = new byte[4096];
        int n = in.read(bytes);
        while (n != -1) {
          bin.write(bytes, 0, n);
          n = in.read(bytes);
        }
        classBytes = bin.toByteArray();
        resourceMap.put(key, classBytes);
      }
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {

        }
      }
    }
    return classBytes;
  }

}
