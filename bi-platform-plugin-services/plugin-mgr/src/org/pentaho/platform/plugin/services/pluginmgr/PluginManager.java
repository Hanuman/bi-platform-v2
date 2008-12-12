package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IXulOverlay;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.services.solution.SolutionClassLoader;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.IMenuCustomization.CustomizationType;
import org.pentaho.ui.xul.IMenuCustomization.ItemType;
import org.pentaho.ui.xul.util.MenuCustomization;

/**
 * TODO migrate this to use an implementation of IPentahoObjectFactory to create and manage scope of objects.
 * @see IPentahoObjectFactory
 * @see WebSpringPentahoObjectFactory
 * @see StandaloneSpringPentahoObjectFactory
 */
public class PluginManager implements IPluginManager {

	protected static List<IMenuCustomization> menuCustomizations = new ArrayList<IMenuCustomization>();
	
	protected static Map<String,List<IContentGeneratorInfo>> contentGeneratorInfoByTypeMap = new HashMap<String,List<IContentGeneratorInfo>>();
	
	protected static Map<String,IContentGeneratorInfo> contentInfoMap = new HashMap<String,IContentGeneratorInfo>();
	
	protected static Map<String,IContentInfo> contentTypeByExtension = new HashMap<String,IContentInfo>();
	
	protected static List<IXulOverlay> overlays = new ArrayList<IXulOverlay>();
	
	protected static StandaloneObjectFactory objectFactory = new StandaloneObjectFactory();
	
	protected static final ThreadLocal<IPentahoSession> sessions = new ThreadLocal<IPentahoSession>();
	
	protected static PluginManager instance;
	
	public PluginManager() {
		instance = this;
	}
	
	 public IPentahoObjectFactory getObjectFactory() {
	   return objectFactory;
	 }

	public Set<String> getContentTypes() {
		return contentGeneratorInfoByTypeMap.keySet();
	}
	
	public List<IXulOverlay> getOverlays(){
	  return overlays;
	}
	
	public IContentInfo getContentInfoFromExtension( String extension, IPentahoSession session ) {
		return contentTypeByExtension.get( extension );
	}
	
  public List<IContentGeneratorInfo> getContentGeneratorInfoForType( String type, IPentahoSession session ) {
    return contentGeneratorInfoByTypeMap.get( type );
  }

	public static PluginManager getInstance() {
		return instance;
	}

	public IContentGenerator getContentGenerator( String id, IPentahoSession session ) throws ObjectFactoryException {
		IContentGeneratorInfo info = getContentGeneratorInfo( id, session );
		if( info == null ) {
			return null;
		}
		return (IContentGenerator) objectFactory.getObject( id, session);
	}
	
	public IContentGeneratorInfo getContentGeneratorInfo( String id, IPentahoSession session ) {
		IContentGeneratorInfo contentId = contentInfoMap.get( id );
		return contentId;
	}
	
	public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get( type );
		if( !CollectionUtils.isEmpty(contentIds) ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info;
		}
		return null;
	}
	
	public String getContentGeneratorIdForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get( type );
		if( !CollectionUtils.isEmpty(contentIds) ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info.getId();
		}
		return null;
	}
	
	public String getContentGeneratorTitleForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get( type );
		if( !CollectionUtils.isEmpty(contentIds) ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info.getTitle();
		}
		return null;
	}
	
	public String getContentGeneratorUrlForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get( type );
		if( !CollectionUtils.isEmpty(contentIds) ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info.getUrl();
		}
		return null;
	}
	
	public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session ) throws ObjectFactoryException {
		// return the default content generator for the given type
		// for now we'll assume the first in the list is the default
		List<IContentGeneratorInfo> contentGenerators = contentGeneratorInfoByTypeMap.get( type );
		if( !CollectionUtils.isEmpty(contentGenerators) ) {
			String id = contentGenerators.get( 0 ).getId();
			IContentGenerator generator = (IContentGenerator) objectFactory.getObject(id, session);
			return generator;
		}
		return null;
	}
	
	public static String getContentGeneratorIdForType( String type ) {
		if( getInstance() == null ) {
			return ""; //$NON-NLS-1$
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getId() : ""; //$NON-NLS-1$
	}
	
	public static String getContentGeneratorTitleForType( String type ) {
		if( getInstance() == null ) {
			return ""; //$NON-NLS-1$
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getTitle() : ""; //$NON-NLS-1$
	}
	
	public static String getContentGeneratorUrlForType( String type ) {
		if( getInstance() == null ) {
			return ""; //$NON-NLS-1$
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getUrl() : ""; //$NON-NLS-1$
	}
	
	public static void setSession( IPentahoSession session ) {
		sessions.set( session );
	}
	
	public static void removeSession() {
	  sessions.remove();
	}
	
	public List<IMenuCustomization> getMenuCustomizations() {
		return menuCustomizations;
	}

	private synchronized void reset() {
		// clear out the existing settings
    overlays.clear();
		menuCustomizations.clear();
		contentGeneratorInfoByTypeMap.clear();
		contentTypeByExtension.clear();
		objectFactory.init(null, null);
		SolutionClassLoader.clearResourceCache();
	}
	
	public synchronized boolean updatePluginSettings( IPentahoSession session, List<String> comments ) {
		
		reset();
		ISolutionRepository repo = PentahoSystem.get( ISolutionRepository.class, session );
		if( repo == null ) {
			comments.add( Messages.getString("PluginSettings.ERROR_0008_CANNOT_GET_REPOSITORY") ); //$NON-NLS-1$
			Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0008_CANNOT_GET_REPOSITORY")); //$NON-NLS-1$
			return false;
		}
		// look in each of the system setting folders looking for plugin.xml files
		String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" ); //$NON-NLS-1$
		// TODO convert this to VFS?
		File systemDir = new File( systemPath ); 
		if( !systemDir.exists() || !systemDir.isDirectory() ) {
			comments.add( Messages.getString("PluginSettings.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER") ); //$NON-NLS-1$
			Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER")); //$NON-NLS-1$
			return false;
		}
		File kids[] = systemDir.listFiles();
		boolean result = true;
		// look at each child to see if it is a folder
		for( File kid : kids ) {
			if( kid.isDirectory() ) {
				result &= processDirectory( kid, repo, session, comments );
			}
		}
		
		return result;
	}
	
	protected boolean processDirectory( File folder, ISolutionRepository repo, IPentahoSession session, List<String> comments ) {
		// see if there is a plugin.xml file
		FilenameFilter filter = new NameFileFilter( "plugin.xml", IOCase.SENSITIVE ); //$NON-NLS-1$
		File kids[] = folder.listFiles(filter);
		if( kids == null || kids.length == 0 ) {
			// nothing to do here
			return true;
		}
		boolean hasLib = false;
		filter = new NameFileFilter( "lib", IOCase.SENSITIVE ); //$NON-NLS-1$
		kids = folder.listFiles(filter);
		if( kids != null && kids.length > 0 ) {
			hasLib = kids[0].exists() && kids[0].isDirectory();
		}
		// we have found a plugin.xml file
		// get the file from the repository
		String path = "system"+ ISolutionRepository.SEPARATOR +folder.getName()+ ISolutionRepository.SEPARATOR + "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
		try {
			Document doc = repo.getResourceAsDocument( path );
			// we have a plugin.xml document
			if( doc == null ) {
				comments.add( Messages.getString("PluginSettings.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ) ); //$NON-NLS-1$
				Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ) ); //$NON-NLS-1$
				return false;
			}
			return processPluginSettings( doc, session, comments, folder.getName(), repo, hasLib );
		} catch (Exception e) {
			Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ), e ); //$NON-NLS-1$
		}
		return false;
		
	}
	
	protected boolean processPluginSettings( Document doc, IPentahoSession session, List<String> comments, String folder, ISolutionRepository repo, boolean hasLib ) {
		boolean result = true;
		
		result &= processPluginInfo( doc, folder, session, comments );
		result &= processMenuItems( doc, session, comments );
		result &= processContentTypes( doc, session, comments );
		result &= processContentGenerators( doc, session, comments, folder, repo, hasLib );
		result &= processOverlays( doc, session, comments );
		
		if( result ) {
			comments.add( Messages.getString("PluginSettings.USER_PLUGIN_REFRESH_OK", folder) ); //$NON-NLS-1$
		} else {
			comments.add( Messages.getString("PluginSettings.USER_PLUGIN_REFRESH_BAD", folder) ); //$NON-NLS-1$
		}
		
		return result;
	}

	protected boolean processPluginInfo( Document doc, String folder, IPentahoSession session, List<String> comments ) {
		Element node = (Element) doc.selectSingleNode( "/plugin" ); //$NON-NLS-1$
		if( node != null ) {
			String title = node.attributeValue( "title" ); //$NON-NLS-1$
			comments.add( Messages.getString("PluginSettings.USER_UPDATING_PLUGIN", title, folder ) ); //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	protected boolean processMenuItems( Document doc, IPentahoSession session, List<String> comments ) {
		// look for menu system customizations
		boolean result = false;

		List<?> nodes = doc.selectNodes( "//menu-item" ); //$NON-NLS-1$
		for( Object obj: nodes ) {
			Element node = (Element) obj;

			String id = node.attributeValue( "id" ); //$NON-NLS-1$
			String label = node.attributeValue( "label" ); //$NON-NLS-1$
			try {
				// create an IMenuCustomization object 
				String anchorId = node.attributeValue( "anchor" ); //$NON-NLS-1$
				String command = node.attributeValue( "command" ); //$NON-NLS-1$
				CustomizationType customizationType = CustomizationType.valueOf( node.attributeValue( "how" ) ); //$NON-NLS-1$
				ItemType itemType = ItemType.valueOf( node.attributeValue( "type" ) ); //$NON-NLS-1$
				IMenuCustomization custom = new MenuCustomization();
				custom.setAnchorId(anchorId);
				custom.setId(id);
				custom.setCommand(command);
				custom.setLabel(label);
				custom.setCustomizationType(customizationType);
				custom.setItemType(itemType);
				// store it
				menuCustomizations.add( custom );
				if( customizationType == CustomizationType.DELETE ) {
					comments.add( Messages.getString("PluginSettings.USER_MENU_ITEM_DELETE", id) ); //$NON-NLS-1$
				}
				else if( customizationType == CustomizationType.REPLACE ) {
					comments.add( Messages.getString("PluginSettings.USER_MENU_ITEM_REPLACE", id, label) ); //$NON-NLS-1$
				} else {
					comments.add( Messages.getString("PluginSettings.USER_MENU_ITEM_ADDITION", id, label) ); //$NON-NLS-1$
				}
				result = true;
			} catch (Exception e) {
				comments.add( Messages.getString("PluginSettings.ERROR_0009_MENU_CUSTOMIZATION_ERROR", id, label) ); //$NON-NLS-1$
				Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0009_MENU_CUSTOMIZATION_ERROR", id, label), e); //$NON-NLS-1$
			}
		}
		
		return result;
	}

	 protected boolean processOverlays( Document doc, IPentahoSession session, List<String> comments ) {
	    // look for content types
	    boolean result = true;

	    List<?> nodes = doc.selectNodes( "//overlays/overlay" ); //$NON-NLS-1$
	    for( Object obj: nodes ) {
	      Element node = (Element) obj;
	      String xml = null;
	      String id = node.attributeValue( "id" ); //$NON-NLS-1$
	      String resourceBundleUri = node.attributeValue( "resourcebundle" ); //$NON-NLS-1$
	      if( node.elements() != null && node.elements().size() > 0 ) {
	        xml = ((Element)node.elements().get(0)).asXML();
	      }
	      if( StringUtils.isNotEmpty( id ) && StringUtils.isNotEmpty( xml ) ) {
	        addOverlay( id, xml, resourceBundleUri);
	      }
	      
	    }
	    
	    return result;
	 }
	 
	 public void addOverlay( String id, String xml, String resourceBundleUri ) {
     XulOverlay overlay = new XulOverlay( id, null, xml, resourceBundleUri );
     overlays.add( overlay );
	 }
	 
	protected boolean processContentTypes( Document doc, IPentahoSession session, List<String> comments ) {
		// look for content types
		boolean result = true;

		List<?> nodes = doc.selectNodes( "//content-type" ); //$NON-NLS-1$
		for( Object obj: nodes ) {
			Element node = (Element) obj;

			// create an IMenuCustomization object 
			String title = XmlDom4JHelper.getNodeText( "title", node ); //$NON-NLS-1$
			String extension = node.attributeValue( "type" ); //$NON-NLS-1$
			
			if( title != null && extension != null ) {
	      String description = XmlDom4JHelper.getNodeText( "description", node, "" ); //$NON-NLS-1$ //$NON-NLS-2$
	      String mimeType =  node.attributeValue( "mime-type", "" ); //$NON-NLS-1$ //$NON-NLS-2$
	      String iconUrl =  XmlDom4JHelper.getNodeText( "icon-url", node, "" ); //$NON-NLS-1$ //$NON-NLS-2$
				ContentInfo contentInfo = new ContentInfo();
				contentInfo.setDescription( description );
				contentInfo.setTitle( title );
				contentInfo.setExtension( extension );
				contentInfo.setMimeType(mimeType);
				contentInfo.setIconUrl(iconUrl);

				List<?> operationNodes = node.selectNodes( "operations/operation" ); //$NON-NLS-1$
        for( Object operationObj : operationNodes) {
          Element operationNode = (Element) operationObj;
          String id = XmlDom4JHelper.getNodeText( "id" , operationNode, "" ); //$NON-NLS-1$ //$NON-NLS-2$
          String command = XmlDom4JHelper.getNodeText( "command" , operationNode, "" ); //$NON-NLS-1$ //$NON-NLS-2$
          if( StringUtils.isNotEmpty( id ) && StringUtils.isNotEmpty( command )  ) {
            IPluginOperation operation = new PluginOperation( id, command );
            contentInfo.addOperation(operation);
          }
        }
        
        addContentInfo( extension, contentInfo );
				comments.add( Messages.getString("PluginSettings.USER_CONTENT_TYPE_REGISTERED", extension, title ) ); //$NON-NLS-1$
			} else {
				comments.add( Messages.getString("PluginSettings.USER_CONTENT_TYPE_NOT_REGISTERED", extension, title ) ); //$NON-NLS-1$
			}
		}
		
		return result;
	}

	public void addContentInfo( String extension, IContentInfo contentInfo ) {
    contentTypeByExtension.put( extension, contentInfo );
	}
	
	protected boolean processContentGenerators( Document doc, IPentahoSession session, List<String> comments, String folder, ISolutionRepository repo, boolean hasLib ) {
		// look for content generators
		boolean result = true;

		List<?> nodes = doc.selectNodes( "//content-generator" ); //$NON-NLS-1$
		for( Object obj: nodes ) {
			Element node = (Element) obj;
			
			// create an IMenuCustomization object 
			String className = XmlDom4JHelper.getNodeText( "classname", node, null); //$NON-NLS-1$
			String fileInfoClassName = XmlDom4JHelper.getNodeText( "fileinfo-classname", node, null); //$NON-NLS-1$
			String scope = node.attributeValue( "scope" ); //$NON-NLS-1$
			String id =  node.attributeValue( "id" ); //$NON-NLS-1$
			String type =  node.attributeValue( "type" ); //$NON-NLS-1$
			String url =  node.attributeValue( "url" ); //$NON-NLS-1$
			String title = XmlDom4JHelper.getNodeText( "title", node, null); //$NON-NLS-1$ 
			String description = XmlDom4JHelper.getNodeText( "description", node, ""); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				
				if( id != null && type != null && scope != null && className != null && title != null ) {
					
          ClassLoader loader = new SolutionClassLoader( "system"+ISolutionRepository.SEPARATOR+folder+ISolutionRepository.SEPARATOR+"lib", //$NON-NLS-1$ //$NON-NLS-2$
              this );
				  try {
				    addContentGenerator( id, title, description, type, url, scope, className, fileInfoClassName, 
				         session, comments, folder, loader );
					} catch (Exception e) {
						comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
					}
				} else {
					comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
				}
			} catch (Exception e) {
				comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder ) ); //$NON-NLS-1$
				Logger.error( getClass().toString() , Messages.getErrorString("PluginSettings.ERROR_0006_CANNOT_CREATE_CONTENT_GENERATOR_FACTORY", folder), e); //$NON-NLS-1$
			}
		}
		return result;
	}
	
	public void addContentGenerator( String id, String title, String description, String type, String url, String scope, String className, String fileInfoClassName, 
	    IPentahoSession session, List<String> comments, String location, ClassLoader loader ) throws ObjectFactoryException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    try {
      Class clazz = loader.loadClass( className );
      objectFactory.addObject( clazz.getSimpleName(), className, scope, loader);
      objectFactory.addObject( id, className, scope, loader);
    } catch ( Exception e ) {
      comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, location ) ); //$NON-NLS-1$
      return;
    }
  
    // do a test load of the content generator so we can fail now if the class cannot be found
    // this tests class loading and cast class issues
    Object tmpObject = objectFactory.getObject( id, session);
    
    if( !(tmpObject instanceof IContentGenerator) ) {
      comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, location ) ); //$NON-NLS-1$
      return;
    }
    
    if( tmpObject instanceof IPentahoInitializer ) {
      ((IPentahoInitializer) tmpObject).init(session);
    }
    
    ContentGeneratorInfo info = new ContentGeneratorInfo();
    info.setId( id );
    info.setTitle( title );
    info.setDescription( description );
    info.setUrl( ( url != null ) ? url : "" ); //$NON-NLS-1$

    if( fileInfoClassName != null ) {
      // try to create the fileinfo generator class
      IFileInfoGenerator fileInfoGenerator = null;

      fileInfoGenerator = createFileInfoGenerator( fileInfoClassName, loader );
      info.setFileInfoGenerator(fileInfoGenerator);
    }

    contentInfoMap.put( id, info );
    List<IContentGeneratorInfo> generatorList = contentGeneratorInfoByTypeMap.get( type );
    if( generatorList == null ) {
      generatorList = new ArrayList<IContentGeneratorInfo>();
      contentGeneratorInfoByTypeMap.put( type, generatorList);
    }
    generatorList.add( info );
                            
    comments.add( Messages.getString("PluginSettings.USER_CONTENT_GENERATOR_REGISTERED", id, location ) ); //$NON-NLS-1$
	}
	
	private IFileInfoGenerator createFileInfoGenerator( String fileInfoClassName, ClassLoader loader ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> clazz = loader.loadClass(fileInfoClassName);
		return (IFileInfoGenerator) clazz.newInstance();
	}
	
}
