package org.pentaho.platform.engine.services;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IMenuCustomization;
import org.pentaho.platform.api.ui.IMenuCustomization.CustomizationType;
import org.pentaho.platform.api.ui.IMenuCustomization.ItemType;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.GlobalObjectCreator;
import org.pentaho.platform.engine.core.system.objfac.LocalObjectCreator;
import org.pentaho.platform.engine.core.system.objfac.PentahoObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.SessionObjectCreator;
import org.pentaho.platform.engine.core.system.objfac.ThreadObjectCreator;
import org.pentaho.platform.engine.services.solution.SolutionClassLoader;
import org.pentaho.platform.util.MenuCustomization;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class PluginSettings implements IPluginSettings {

	protected static List<IMenuCustomization> menuCustomizations = new ArrayList<IMenuCustomization>();
	
	protected Map<String,IObjectCreator> contentGeneratorCreatorMap = new HashMap<String,IObjectCreator>();

	protected static Map<String,List<IObjectCreator>> contentGeneratorMap = new HashMap<String,List<IObjectCreator>>();
	
	protected static Map<String,List<IContentGeneratorInfo>> contentInfoForTypeLists = new HashMap<String,List<IContentGeneratorInfo>>();
	
	protected static Map<String,IContentGeneratorInfo> contentInfoMap = new HashMap<String,IContentGeneratorInfo>();
	
	protected static Map<String,IContentInfo> contentTypeByExtension = new HashMap<String,IContentInfo>();
	
	protected static PentahoObjectFactory contentGeneratorFactory;
	
	protected static final ThreadLocal<IPentahoSession> sessions = new ThreadLocal<IPentahoSession>();
	
	protected static PluginSettings instance;
	
	public PluginSettings() {
		instance = this;
	}
	
	public Set<String> getContentTypes() {
		return contentGeneratorMap.keySet();
	}
	
	public IContentInfo getContentInfoFromExtension( String extension, IPentahoSession session ) {
		return contentTypeByExtension.get( extension );
	}
	
	public List<IObjectCreator> getContentGeneratorsForType( String type, IPentahoSession session ) {
		return contentGeneratorMap.get( type );
	}
	
	public static PluginSettings getInstance() {
		return instance;
	}

	public IContentGenerator getContentGenerator( String id, IPentahoSession session ) {
		IContentGeneratorInfo info = getContentGeneratorInfo( id, session );
		if( info == null ) {
			return null;
		}
		IObjectCreator creator = info.getCreator();
		if( creator == null ) {
			return null;
		}
		try {
			IContentGenerator generator = (IContentGenerator) creator.getInstance( id, session);
			return generator;
		} catch (Exception e) {
			Logger.error( getClass().toString(), "Could not create content generator: "+id, e );
		}
		return null;
	}
	
	public IContentGeneratorInfo getContentGeneratorInfo( String id, IPentahoSession session ) {
		IContentGeneratorInfo contentId = contentInfoMap.get( id );
		return contentId;
	}
	
	public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentInfoForTypeLists.get( type );
		if( contentIds != null && contentIds.size() > 0 ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info;
		}
		return null;
	}
	
	public String getContentGeneratorIdForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentInfoForTypeLists.get( type );
		if( contentIds != null && contentIds.size() > 0 ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info.getId();
		}
		return null;
	}
	
	public String getContentGeneratorTitleForType( String type, IPentahoSession session ) {
		List<IContentGeneratorInfo> contentIds = contentInfoForTypeLists.get( type );
		if( contentIds != null && contentIds.size() > 0 ) {
			IContentGeneratorInfo info = contentIds.get( 0 );
			return info.getTitle();
		}
		return null;
	}
	
	public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session ) {
		// return the default content generator for the given type
		// for now we'll assume the first in the list is the default
		try {
			List<IObjectCreator> contentGenerators = contentGeneratorMap.get( type );
			if( contentGenerators != null && contentGenerators.size() > 0 ) {
				IObjectCreator creator = contentGenerators.get( 0 );
				IContentGenerator generator = (IContentGenerator) creator.getInstance( type, session);
				return generator;
			}
		} catch (Exception e) {
			Logger.error( getClass().toString() , "Could not get content generator", e );
		}
		return null;
	}
	
	public static String getContentGeneratorIdForType( String type ) {
		if( getInstance() == null ) {
			return "";
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getId() : "";
	}
	
	public static String getContentGeneratorTitleForType( String type ) {
		if( getInstance() == null ) {
			return "";
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getTitle() : "";
	}
	
	public static String getContentGeneratorUrlForType( String type ) {
		if( getInstance() == null ) {
			return "";
		}
		IPentahoSession session = sessions.get();
		IContentGeneratorInfo info = getInstance().getDefaultContentGeneratorInfoForType( type, session );
		return (info != null) ? info.getUrl() : "";
	}
	
	public static void setSession( IPentahoSession session ) {
		sessions.set( session );
	}
	
	public List<IMenuCustomization> getMenuCustomizations() {
		return menuCustomizations;
	}

	private synchronized void reset() {
		// clear out the existing settings
		menuCustomizations.clear();
		contentGeneratorMap.clear();
		contentInfoForTypeLists.clear();
		contentTypeByExtension.clear();
		contentGeneratorFactory = new PentahoObjectFactory();
		contentGeneratorCreatorMap = new HashMap<String,IObjectCreator>();
	}
	
	public synchronized boolean updatePluginSettings( IPentahoSession session, StringBuilder comments ) {
		
		reset();
		ISolutionRepository repo = PentahoSystem.getSolutionRepository(session);
		// look in each of the system setting folders looking for plugin.xml files
		String solutionRoot = PentahoSystem.getApplicationContext().getSolutionRootPath();
		// TODO convert this to VFS?
		File rootDir = new File( solutionRoot );
		if( !rootDir.exists() || !rootDir.isDirectory() ) {
			Logger.error( getClass().toString() , "Solution root is not a folder");
			return false;
		}
		boolean result = true;
		File systemDir = new File( rootDir, "system" );
		if( !rootDir.exists() || !rootDir.isDirectory() ) {
			Logger.error( getClass().toString() , "Connot find system folder");
			return false;
		}
		File kids[] = systemDir.listFiles();
		// look at each child to see if it is a folder
		for( File kid : kids ) {
			if( kid.isDirectory() ) {
				result &= processDirectory( kid, repo, session, comments );
			}
		}
		
		// we have all the content generator creators so we can create the object factory
		contentGeneratorFactory.setObjectCreators( contentGeneratorCreatorMap );
		
		return result;
	}
	
	protected boolean processDirectory( File folder, ISolutionRepository repo, IPentahoSession session, StringBuilder comments ) {
		// see if there is a plugin.xml file
		FilenameFilter filter = new NameFileFilter( "plugin.xml", IOCase.SENSITIVE );
		File kids[] = folder.listFiles(filter);
		if( kids == null || kids.length == 0 ) {
			// nothing to do here
			return true;
		}
		boolean hasLib = false;
		filter = new NameFileFilter( "lib", IOCase.SENSITIVE );
		kids = folder.listFiles(filter);
		if( kids != null && kids.length > 0 ) {
			hasLib = kids[0].exists() && kids[0].isDirectory();
		}
		// we have found a plugin.xml file
		// get the file from the repository
		String path = "system"+ ISolutionRepository.SEPARATOR +folder.getName()+ ISolutionRepository.SEPARATOR + "plugin.xml";
		try {
			Document doc = repo.getResourceAsDocument( path );
			// we have a plugin.xml document
			return processPluginSettings( doc, session, comments, folder.getName(), repo, hasLib );
		} catch (Exception e) {
			Logger.error( getClass().toString() , "Could not get plugin.xml: "+path );
		}
		return false;
		
	}
	
	protected boolean processPluginSettings( Document doc, IPentahoSession session, StringBuilder comments, String folder, ISolutionRepository repo, boolean hasLib ) {
		boolean result = true;
		
		result &= processPluginInfo( doc, session, comments );
		result &= processMenuItems( doc, session, comments );
		result &= processContentTypes( doc, session, comments );
		result &= processContentGenerators( doc, session, comments, folder, repo, hasLib );
		
		if( result ) {
			comments.append( "Plug refresh successful." );
		} else {
			comments.append( "Plug refresh had errors." );
		}
		
		return result;
	}

	protected boolean processPluginInfo( Document doc, IPentahoSession session, StringBuilder comments ) {
		Element node = (Element) doc.selectSingleNode( "/plugin" );
		if( node != null ) {
			String title = node.attributeValue( "title" );
			comments.append( "Updating plugin "+title );
			return true;
		}
		return false;
	}
	
	protected boolean processMenuItems( Document doc, IPentahoSession session, StringBuilder comments ) {
		// look for menu system customizations
		boolean result = true;

		List<?> nodes = doc.selectNodes( "//menu-item" );
		for( Object obj: nodes ) {
			Element node = (Element) obj;

			try {
				// create an IMenuCustomization object 
				String anchorId = node.attributeValue( "anchor" );
				String id = node.attributeValue( "id" );
				String command = node.attributeValue( "command" );
				String label = node.attributeValue( "label" );
				CustomizationType customizationType = CustomizationType.valueOf( node.attributeValue( "how" ) );
				ItemType itemType = ItemType.valueOf( node.attributeValue( "type" ) );
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
					comments.append( "Menu deletion: "+id );
				}
				else if( customizationType == CustomizationType.REPLACE ) {
					comments.append( "Menu replacement: "+id );
				} else {
					comments.append( "Menu addition: "+id );
				}
			} catch (Exception e) {
			}
		}
		
		return result;
	}
	
	protected boolean processContentTypes( Document doc, IPentahoSession session, StringBuilder comments ) {
		// look for menu system customizations
		boolean result = true;

		List<?> nodes = doc.selectNodes( "//content-type" );
		for( Object obj: nodes ) {
			Element node = (Element) obj;

			try {
				// create an IMenuCustomization object 
				String title = XmlDom4JHelper.getNodeText( "title", node );
				String description = XmlDom4JHelper.getNodeText( "description", node, "" );
				String extension = node.attributeValue( "type" );
				String mimeType =  node.attributeValue( "mime-type", "" );
				
				if( title != null && extension != null ) {
					ContentInfo contentInfo = new ContentInfo();
					contentInfo.setDescription( description );
					contentInfo.setTitle( title );
					contentInfo.setExtension( extension );
					contentInfo.setMimeType(mimeType);
					comments.append( "Content type registered: "+ extension + " : "+title );
				} else {
					comments.append( "Content type not registered: "+ extension + " : "+title );
				}
			} catch (Exception e) {
			}
		}
		
		return result;
	}

	protected boolean processContentGenerators( Document doc, IPentahoSession session, StringBuilder comments, String folder, ISolutionRepository repo, boolean hasLib ) {
		// look for menu system customizations
		boolean result = true;

		List<?> nodes = doc.selectNodes( "//content-generator" );
		for( Object obj: nodes ) {
			Element node = (Element) obj;
			
			// create an IMenuCustomization object 
			String className = XmlDom4JHelper.getNodeText( "classname", node, "");
			String fileInfoClassName = XmlDom4JHelper.getNodeText( "fileinfo-classname", node, "");
			String scope = node.attributeValue( "scope" );
			String id =  node.attributeValue( "id" );
			String type =  node.attributeValue( "type" );
			String url =  node.attributeValue( "url" );
			String title = XmlDom4JHelper.getNodeText( "title", node, "");
			String description = XmlDom4JHelper.getNodeText( "description", node, "");
			try {
				
				if( id != null && type != null && scope != null && className != null ) {
					IObjectCreator creator = null;
					if( "global".equals( scope ) ) {
						creator = new GlobalObjectCreator( className );
					}
					else if( "session".equals( scope ) ) {
						creator = new SessionObjectCreator( className );
					}
					else if( "local".equals( scope ) ) {
						creator = new LocalObjectCreator( className );
					}
					else if( "thread".equals( scope ) ) {
						creator = new ThreadObjectCreator( className );
					}
					
					if( creator != null ) {
						ClassLoader loader = null;
						if( hasLib ) {
							// this needs a solution class loader
							loader = new SolutionClassLoader( "system"+ISolutionRepository.SEPARATOR+folder+ISolutionRepository.SEPARATOR+"lib",
									this );
							creator.setClassLoader( loader );
						}
						ContentGeneratorInfo info = new ContentGeneratorInfo();
						info.setId( id );
						info.setTitle( title );
						info.setDescription( description );
						info.setObjectCreator( creator );
						info.setUrl( ( url != null ) ? url : "" );

						contentGeneratorCreatorMap.put( id, creator );
						contentInfoMap.put( id, info );
						List<IObjectCreator> creatorList = contentGeneratorMap.get( type );
						List<IContentGeneratorInfo> infoList = contentInfoForTypeLists.get( type );
						if( creatorList == null ) {
							creatorList = new ArrayList<IObjectCreator>();
							infoList = new ArrayList<IContentGeneratorInfo>();
							contentGeneratorMap.put( type, creatorList );
							contentInfoForTypeLists.put( type, infoList );
						}
						creatorList.add( creator );
						infoList.add( info );
						
						if( fileInfoClassName != null ) {
							// try to create the fileinfo generator class
							IFileInfoGenerator fileInfoGenerator = null;
							if( loader != null ) {
								fileInfoGenerator = createFileInfoGenerator( fileInfoClassName, loader );
							} else {
								fileInfoGenerator = createFileInfoGenerator( fileInfoClassName, getClass().getClassLoader() );
							}
							if( fileInfoGenerator != null ) {
								info.setFileInfoGenerator(fileInfoGenerator);
							}
						}
						
						comments.append( "Content generator registered: "+ id );
					} else {
						comments.append( "Content generator not registered: "+ id );
					}
				} else {
					comments.append( "Content generator not registered: "+ id );
				}
			} catch (Exception e) {
				comments.append( "Content generator not registered: "+ id );
				Logger.error( getClass().toString() , "Could not create content generator factory", e);
			}
		}
		return result;
	}
	
	private IFileInfoGenerator createFileInfoGenerator( String fileInfoClassName, ClassLoader loader ) {
		try {
			Class<?> clazz = loader.loadClass(fileInfoClassName);
			return (IFileInfoGenerator) clazz.newInstance();
		} catch (Exception e) {
			Logger.error( getClass().toString(), "Could not create file info generator: "+fileInfoClassName, e );
		}
		return null;
	}
	
}
