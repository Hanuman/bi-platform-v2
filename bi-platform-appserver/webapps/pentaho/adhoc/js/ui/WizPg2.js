/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created  
 * @author Steven Barkdull
 */

/**
 * 
 */
WizPg2 = function()
{
	this.initText();
  var content2 = document.getElementById( "content2" );
  content2.title = Messages.getString( "STEP3_SELECT_AN_ITEM_TOOLTIP" );
  
	var step2img = document.getElementById('step2img');
	step2img.title = Messages.getString("step3Title");
	
	var parent = document.getElementById( "groups2Td" );
	this.groupsCtrl = new ROGroupsCtrl( "groups2Div" );
	parent.appendChild( this.groupsCtrl.getRoot() );
	
	var detailsTd = document.getElementById( "details2Td" );
	this.detailsCtrl = new ListCtrl( "details2Div", WaqrWizard.CN_DETAILS_LIST, ListCtrl.SINGLE_SELECT );
	detailsTd.appendChild( this.detailsCtrl.getRoot());
	this.detailsCtrl.setDefaultSelectedItemClassName( "selectedItem" );
	
	var filtersTd = document.getElementById( "filters2Td" );
	this.filtersCtrl = new ListCtrl( "filters2Div", WaqrWizard.CN_FILTERS_LIST, ListCtrl.SINGLE_SELECT );
	filtersTd.appendChild( this.filtersCtrl.getRoot());
	this.filtersCtrl.setDefaultSelectedItemClassName( "selectedItem" );
	
	// constraints control setup
	this.groupHeaderEditorCtrl = new GroupHeaderEditorCtrl();
	this.groupItemEditorCtrl = new GroupItemEditorCtrl();
	
	// detail controls
	this.detailItemEditorCtrl = new DetailItemEditorCtrl();
	
	this.constraintsEditorCtrl = new ConstraintsEditorCtrl();
	this.columnSortersEditorCtrl = new ColumnSorterEditorCtrl();
}
	
WizPg2.prototype.initText = function()
{
	Messages.setElementText("step3SelectedItemsTitle", "selectedItemsTitle");
	Messages.setElementText("step3GroupsTitle", "step3GroupsTitle");
	Messages.setElementText("step3DetailsTitle", "step3DetailsTitle");
	Messages.setElementText("step3FiltersTitle", "step3FiltersTitle");
	Messages.setElementText("step3GrpsGeneralTitle", "step3GrpsGeneralTitle");
	Messages.setElementText("step3GrpsFormatTitle", "step3GrpsFormatTitle");
	Messages.setElementText("step3GrpsLvlNameTitle", "step3GrpsLvlNameTitle");
	Messages.setElementText("step3GrpOptionsTitle", "step3GrpOptionsTitle");
	Messages.setElementText("step3RepeatGrpHdr", "step3RepeatGrpHdr");
	Messages.setElementText("step3ShowTotals", "step3ShowTotals");
	Messages.setElementText("step3PgBrkTitle", "step3PgBrkTitle");
	Messages.setElementText("step3GrpAlignTitle", "step3GrpAlignTitle");
	Messages.setElementText("step3GrpsFormatTitle2", "step3GrpsFormatTitle2");
	Messages.setElementText("step3GrpAlignTitle2", "step3GrpAlignTitle2");
	Messages.setElementText("step3DetailsFormatTitle", "step3DetailsFormatTitle");
	Messages.setElementText("step3CalcsTitle", "step3CalcsTitle");
	Messages.setElementText("step3DetailsAlignTitle", "step3DetailsAlignTitle");
	Messages.setElementText("step3DetailsUseCalcFunc", "step3DetailsUseCalcFunc");

	Messages.setElementText("step3ConstraintsTitle", "step3ConstraintsTitle");
	Messages.setElementText("step3ColumnSorterEditorTitle", "step3SortColumns");
	Messages.setElementText("step3ConstraintCurrSettings", "step3ConstraintCurrSettings");
	Messages.setElementText("step3ShowGroupSummary", "STEP3_SHOW_GROUP_SUMMARY");
	Messages.setElementText("step3GroupTotalLabel", "STEP3_GROUP_TOTAL_LABEL");
	
	document.getElementById( "step3ShowGroupSummary" ).title = Messages.getString( "STEP3_SHOW_GROUP_SUMMARY_TOOLTIP" );
	document.getElementById( "step3RepeatGrpHdr" ).title = Messages.getString( "STEP3_REPEAT_GROUP_HEADER_TOOLTIP" );
	document.getElementById( "step3GroupTotalLabel" ).title = Messages.getString( "STEP3_GROUP_TOTAL_LABEL_TOOLTIP" );
	document.getElementById( "groupTotalLabelText" ).title = Messages.getString( "STEP3_GROUP_TOTAL_LABEL_TOOLTIP" );
	document.getElementById( "levelname_box" ).title = Messages.getString( "STEP3_GROUP_LEVEL_NAME_TOOLTIP" );
}
WizPg2.prototype.showPg = function()
{
	var step2img = document.getElementById('step2img');
	if( step2img.blur ) {
		step2img.blur();
	}
	var title = Messages.getString("step3Title");
	document.getElementById('wizard_title').innerHTML = title+'<span class="wizard_shadow">'+title+'</span>';
	document.getElementById('content2').style.display='block';
	step2img.src=UIUtil.getImageFolderPath() + 'step3_active.png';
	setHeights_step2();
}
WizPg2.prototype.hidePg = function()
{
	document.getElementById('content2').style.display='none';
	document.getElementById('step2img').src=UIUtil.getImageFolderPath() + 'step3_available.png';
	var ctrl = this.getGroupHeaderEditorCtrl();
	ctrl.hide();
	ctrl = this.getGroupItemEditorCtrl();
	ctrl.hide();
	ctrl = this.getDetailItemEditorCtrl();
	ctrl.hide();
	ctrl = this.getConstraintsEditorCtrl();
	ctrl.hide();
	ctrl = this.getColumnSorterEditorCtrl();
	ctrl.hide();
}
WizPg2.prototype.getGroupHeaderEditorCtrl = function()
{
	return this.groupHeaderEditorCtrl;
}
WizPg2.prototype.getGroupItemEditorCtrl = function()
{
	return this.groupItemEditorCtrl;
}
// detail -----
WizPg2.prototype.getDetailsCtrl = function()
{
	return this.detailsCtrl;
}
WizPg2.prototype.getDetailItemEditorCtrl = function()
{
	return this.detailItemEditorCtrl;
}
//filter ------
WizPg2.prototype.getFiltersCtrl = function()
{
	return this.filtersCtrl;
}
/**
 * @return ConstraintsEditorCtrl
 */
WizPg2.prototype.getConstraintsEditorCtrl =  function()
{
	return this.constraintsEditorCtrl;
}
/**
 * @return ColumnSorterCtrl
 */
WizPg2.prototype.getColumnSorterEditorCtrl =  function()
{
	return this.columnSortersEditorCtrl;
}

/**
 * @return ROGroupsCtrl
 */
WizPg2.prototype.getGroupsCtrl = function()
{
	return this.groupsCtrl;
}
 