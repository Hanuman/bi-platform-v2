package org.pentaho.platform.util;

import org.pentaho.platform.api.ui.IMenuCustomization;

public class MenuCustomization implements IMenuCustomization {

	private String anchorId;
	
	private String id;
	
	private String command;
	
	private CustomizationType customizationType = CustomizationType.INSERT_BEFORE;
	
	private ItemType itemType = ItemType.MENU_ITEM;
	
	private String label = ""; //$NON-NLS-1$

	public MenuCustomization() {
		
	}
	
	public MenuCustomization( String id, String anchorId, String label, String command, ItemType itemType, CustomizationType customizationType ) {
		this.id = id;
		this.anchorId = anchorId;
		this.label = label;
		this.command = command;
		this.itemType = itemType;
		this.customizationType = customizationType;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAnchorId() {
		return anchorId;
	}

	public void setAnchorId(String anchorId) {
		this.anchorId = anchorId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public CustomizationType getCustomizationType() {
		return customizationType;
	}

	public void setCustomizationType(CustomizationType customizationType) {
		this.customizationType = customizationType;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}
}
