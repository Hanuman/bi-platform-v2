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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Sept 15, 2008 
 * @author jdixon
 */

package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IFileTypePlugin;

/**
 * 
 * @author jamesdixon
 *
 */
public interface IPluginSettings {

	/**
	 * Returns a set of the content types that the registered plugins can
	 * process. If the plugin is intended to handle the processing of a 
	 * document in the solution repository the types need to match with
	 * the filename extension.
	 * @return
	 */
	public Set<String> getContentTypes();
	
	public IContentInfo getContentInfoFromExtension( String extension, IPentahoSession session );
	
	/**
	 * Returns a list of object factories that can be used to create content
	 * generators for a given type. In most cases there will only be one
	 * content generator for any one type.
	 * @param type
	 * @param session A session used for storing objects from session-scoped factories
	 * @return List of IObjectCreator objects
	 */
	public List<IObjectCreator> getContentGeneratorsForType( String type, IPentahoSession session );
	
	public IContentGenerator getContentGenerator( String id, IPentahoSession session ) throws ObjectFactoryException;
	
	public IContentGeneratorInfo getContentGeneratorInfo( String id, IPentahoSession session );
	
	public IContentGeneratorInfo getDefaultContentGeneratorInfoForType( String type, IPentahoSession session );
	
	public String getContentGeneratorIdForType( String type, IPentahoSession session );
	
	public String getContentGeneratorTitleForType( String type, IPentahoSession session );
	
	public String getContentGeneratorUrlForType( String type, IPentahoSession session );
	
	public IContentGenerator getContentGeneratorForType( String type, IPentahoSession session ) throws ObjectFactoryException;
	
	/**
	 * Returns a list of menu customization objects. Objects in this list will be
	 * org.pentaho.ui.xul.IMenuCustomization objects
	 * @return List of IMenuCustomization objects
	 */
	public List getMenuCustomizations();
	
	/**
	 * Returns a list of file type plugins, that specify behavior for viewing and 
	 * editing plugins.
	 * 
	 * @return list of file type plugins
	 */
	public List<IFileTypePlugin> getFileTypePlugins();
	
	/**
	 * Causes the plugin settings object to re-register all of the plugins that
	 * are defined in pentaho-solutions/system/./plugin.xml files
	 * @param session A session to be used for getting the plugin.xml files
	 * @param comments A list of strings that readable messages will be added to
	 * as the plugins are processed.
	 * @return true if no errors were encountered
	 */
	public boolean updatePluginSettings( IPentahoSession session, List<String> comments );

}
