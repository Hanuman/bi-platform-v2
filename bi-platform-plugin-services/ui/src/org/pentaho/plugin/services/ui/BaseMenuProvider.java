package org.pentaho.plugin.services.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IMenuCustomization;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PluginSettings;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.util.MenuUtil;

public abstract class BaseMenuProvider implements IMenuProvider {

	private List<IMenuCustomization> customizations = new ArrayList<IMenuCustomization>();
	
	public void addCustomization( IMenuCustomization custom ) {
		customizations.add( custom );
	}
	
	public abstract Object getMenuBar( String id, IPentahoSession session );
	
	public abstract Object getPopupMenu( String id, IPentahoSession session );
	
	protected abstract XulLoader getXulLoader();
	
	protected XulDomContainer getXulContainer(IPentahoSession session) {
		  try {
			  ISolutionRepository repo = PentahoSystem.getSolutionRepository(session);
			  InputStream in = repo.getResourceInputStream("system/ui/menubar.xul", true);
		      SAXReader rdr = new SAXReader();
		      final Document doc = rdr.read(in);
		      
		      XulDomContainer container = getXulLoader().loadXul(doc);
		      
		      return container;
		  } catch (Exception e) {
			  session.error( "Menu system could not be generated" , e);
		  }
		  return null;
	}
	
	protected XulMenubar getXulMenubar( String id, IPentahoSession session ) {
		  try {
		      XulDomContainer container = getXulContainer( session );
		      List<XulComponent> components = container.getDocumentRoot().getElementsByTagName( "menubar" );
		      for( XulComponent component : components ) {
		    	  if( component instanceof XulMenubar && component.getId().equals( id ) ) {
				      XulMenubar menubar = (XulMenubar) component;
				      // now get customizations to it
				      PluginSettings pluginSettings = (PluginSettings) PentahoSystem.getObject(session, "IPluginSettings" );
				      List<IMenuCustomization> menuCustomizations = pluginSettings.getMenuCustomizations();
				      MenuUtil.customizeMenu(menubar, menuCustomizations, getXulLoader());
			    	  return menubar;
			      }		      
		      }

		  } catch (Exception e) {
			  session.error( "Menu system could not be generated" , e);
		  }
		  return null;
	}
	
	protected XulMenupopup getXulPopupMenu( String id, IPentahoSession session ) {
		  try {
		      XulDomContainer container = getXulContainer( session );
		      List<XulComponent> components = container.getDocumentRoot().getElementsByTagName( "menupopup[@ID='"+id+"']" );
		      if( components.size() > 0 && components.get(0) instanceof XulMenupopup ) {
		    	  return (XulMenupopup) components.get(0);
		      }
		  } catch (Exception e) {
			  session.error( "Menu system could not be generated" , e);
		  }
		  return null;
	}
	
}
