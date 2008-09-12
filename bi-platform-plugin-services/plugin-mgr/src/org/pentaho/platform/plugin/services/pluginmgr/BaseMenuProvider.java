package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.util.MenuUtil;

public abstract class BaseMenuProvider implements IMenuProvider {

	public abstract Object getMenuBar( String id, String documentPath, IPentahoSession session );
	
	public abstract Object getPopupMenu( String id, String documentPath, IPentahoSession session );
	
	protected abstract XulLoader getXulLoader();
	
	protected XulDomContainer getXulContainer( String documentPath, IPentahoSession session) {
		  try {
			  ISolutionRepository repo = PentahoSystem.getSolutionRepository(session);
			  InputStream in = repo.getResourceInputStream( documentPath, true);
		      SAXReader rdr = new SAXReader();
		      final Document doc = rdr.read(in);
		      
		      XulDomContainer container = getXulLoader().loadXul(doc);
		      
		      return container;
		  } catch (Exception e) {
			  session.error( Messages.getErrorString("BaseMenuProvider.ERROR_0001_COULD_NOT_GET_MENU_CONTAINER") , e); //$NON-NLS-1$
		  }
		  return null;
	}
	
	protected XulMenubar getXulMenubar( String id, String documentPath, IPentahoSession session ) {
	      XulDomContainer container = getXulContainer( documentPath, session );
	      if( container == null ) {
	    	  return null;
	      }
	      List<XulComponent> components = container.getDocumentRoot().getElementsByTagName( "menubar" ); //$NON-NLS-1$
	      for( XulComponent component : components ) {
	    	  if( component instanceof XulMenubar && component.getId().equals( id ) ) {
			      XulMenubar menubar = (XulMenubar) component;
			      // now get customizations to it
			      IPluginSettings pluginSettings = (IPluginSettings) PentahoSystem.getObject(session, "IPluginSettings" ); //$NON-NLS-1$
			      List<?> menuCustomizations = pluginSettings.getMenuCustomizations();
			      for( Object custom: menuCustomizations) {
			    	  if( custom instanceof IMenuCustomization ) {
		    			  IMenuCustomization item = (IMenuCustomization) custom;
			    		  try {
			    			  // apply each customization and log any failures
			    			  MenuUtil.customizeMenu(menubar, item, getXulLoader());
			    		  } catch (Exception e) {
			    			  session.error( Messages.getString("BaseMenuProvider.ERROR_0004_COULD_NOT_CUSTOMIZE_MENU", item.getId(), item.getLabel() ), e); //$NON-NLS-1$
			    		  }
			    	  }
			      }
			      
		    	  return menubar;
		      }		      
	      }
		  Logger.error( getClass().getName(), Messages.getErrorString("BaseMenuProvider.ERROR_0002_COULD_NOT_GET_MENUBAR") ); //$NON-NLS-1$
		  return null;
	}
	
	protected XulMenupopup getXulPopupMenu( String id, String documentPath, IPentahoSession session ) {
	      XulDomContainer container = getXulContainer( documentPath, session );
	      if( container != null ) {
		      List<XulComponent> components = container.getDocumentRoot().getElementsByTagName( "menupopup" ); //$NON-NLS-1$
		      for( XulComponent component : components ) {
		    	  if( component.getId().equals(id) && component instanceof XulMenupopup ) {
			    	  return (XulMenupopup) component;
		    	  }
		      }
	      }
		  Logger.error( getClass().getName(), Messages.getErrorString("BaseMenuProvider.ERROR_0003_COULD_NOT_GET_POPUP_MENU") ); //$NON-NLS-1$
		  return null;
	}
	
}
