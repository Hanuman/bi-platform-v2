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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.perspective.solutionbrowser.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.ReportParameter;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
//import com.google.gwt.widgetideas.client.SliderBar;

public class ReportParameterUI extends HorizontalPanel {

  DisclosurePanel parameterDisclosurePanel = new DisclosurePanel(Messages.getString("reportParameters")); //$NON-NLS-1$
  CheckBox submitParametersOnChangeCheckBox = new CheckBox(Messages.getString("automaticallySubmitParameters")); //$NON-NLS-1$

  public ReportParameterUI() {
    parameterDisclosurePanel.setWidth("100%"); //$NON-NLS-1$
    parameterDisclosurePanel.setStyleName("reportParameterDisclosurePanel"); //$NON-NLS-1$
    parameterDisclosurePanel.setOpen(true);
  }

  public void init(final ReportContainer reportContainer, final List<ReportParameter> reportParameters, final IParameterSubmissionCallback callback) {
    // if (reportContainer.isPromptNeeded()) {
    // disclosurePanel.setOpen(true);
    // } else {
    // disclosurePanel.setOpen(false);
    // }
    FlexTable parameterTable = new FlexTable();
    int row = 0;
    final List<IParameterSubmissionCallback> submitCallbacks = new ArrayList<IParameterSubmissionCallback>();

    final ClickListener submitParametersListener = new ClickListener() {

      public void onClick(Widget sender) {
        // we must update the reportParameters from the UI entries
        for (IParameterSubmissionCallback paramCallback : submitCallbacks) {
          paramCallback.submitReportParameters();
        }
        // now call the real callback
        callback.submitReportParameters();
      }
    };

    for (final ReportParameter parameter : reportParameters) {
      parameterTable.setWidget(row, 0, new Label(parameter.getName()));
      Widget parameterUI = buildParameterUI(parameter, submitCallbacks, submitParametersListener);
      parameterTable.setWidget(row, 1, parameterUI);
      row++;
    }
    Button submitParametersButton = new Button(Messages.getString("submit")); //$NON-NLS-1$
    submitParametersButton.addClickListener(submitParametersListener);
    parameterTable.setWidget(row, 0, submitParametersButton);
    parameterTable.setWidget(row, 1, submitParametersOnChangeCheckBox);
    parameterDisclosurePanel.setContent(parameterTable);

    // add content to panel
    clear();
    setWidth("100%"); //$NON-NLS-1$
    add(parameterDisclosurePanel);
  }

  public Widget buildParameterUI(final ReportParameter parameter, final List<IParameterSubmissionCallback> submitCallbacks,
      final ClickListener submitParametersListener) {

    if (parameter.getPromptType() == ReportParameter.SELECTION_TYPE_LIST) {

      final ListBox choices = new ListBox(parameter.isMultiSelect());

      IParameterSubmissionCallback myCallback = new IParameterSubmissionCallback() {
        public void submitReportParameters() {
          parameter.clearSelections();
          for (int i = 0; i < choices.getItemCount(); i++) {
            if (choices.isItemSelected(i)) {
              if (parameter.getParameterType() == ReportParameter.DATE) {
                String dateStr = choices.getValue(i);
                parameter.getDateValues().add(new Date(dateStr));
              } else if (parameter.getParameterType() == ReportParameter.STRING) {
                String str = choices.getValue(i);
                parameter.getStringValues().add(str);
              } else if (parameter.getParameterType() == ReportParameter.NUMBER) {
                String numberStr = choices.getValue(i);
                parameter.getNumberValues().add(new Integer(numberStr));
              }
            }
          }
        }
      };
      submitCallbacks.add(myCallback);

      choices.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          // if onChangeParameterSubmit is true, submit results
          if (submitParametersOnChangeCheckBox.isChecked()) {
            submitParametersListener.onClick(choices);
          }
        }
      });
      choices.setWidth("250px"); //$NON-NLS-1$
      choices.setVisibleItemCount(parameter.isMultiSelect() ? 5 : 1);
      for (Object choiceKey : parameter.getChoices().keySet()) {
        choices.addItem(parameter.getChoices().get(choiceKey).toString(), choiceKey.toString());
      }
      List selectedValues = parameter.getValues();
      boolean valueSelected = false;
      for (Object selectedValue : selectedValues) {
        for (int i = 0; i < choices.getItemCount(); i++) {
          if (selectedValue.equals(choices.getValue(i))) {
            if (parameter.isMultiSelect()) {
              choices.setItemSelected(i, true);
            } else {
              choices.setSelectedIndex(i);
            }
            valueSelected = true;
          }
        }
      }
      // check if we need to select the default (user selection does not exist)
      if (!valueSelected) {
        Object defaultValue = parameter.getDefaultValue();
        if (defaultValue != null) {
          for (int i = 0; i < choices.getItemCount(); i++) {
            if (defaultValue.equals(choices.getValue(i))) {
              if (parameter.isMultiSelect()) {
                choices.setItemSelected(i, true);
              } else {
                choices.setSelectedIndex(i);
              }
            }
          }
        }
      }
      return choices;
    } else if (parameter.getPromptType() == ReportParameter.SELECTION_TYPE_SELECT) {
      FlowPanel buttonPanel = new FlowPanel();
      // check boxes
      final List<String> selectedValues = new ArrayList<String>();
      for (final Object choiceKey : parameter.getChoices().keySet()) {
        final String choiceValue = parameter.getChoices().get(choiceKey).toString();

        CheckBox tempBox = null;
        if (parameter.isMultiSelect()) {
          // checkbox
          tempBox = new CheckBox(choiceKey.toString());
        } else {
          // radio
          tempBox = new RadioButton(parameter.getName());
          tempBox.setText(choiceKey.toString());
        }

        final CheckBox checkBox = tempBox;

        boolean valueSelected = false;
        for (Object selectedValue : parameter.getValues()) {
          if (selectedValue.equals(choiceKey.toString())) {
            valueSelected = true;
            checkBox.setChecked(true);
            selectedValues.add(choiceValue);
          }
        }
        // check if we need to select the default (user selection does not exist)
        if (!valueSelected) {
          Object defaultValue = parameter.getDefaultValue();
          if (defaultValue != null && defaultValue.equals(choiceKey.toString())) {
            checkBox.setChecked(true);
            selectedValues.add(choiceValue);
          }
        }

        checkBox.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            if (checkBox.isChecked() && !selectedValues.contains(choiceValue)) {
              if (!parameter.isMultiSelect()) {
                selectedValues.clear();
              }
              selectedValues.add(choiceValue);
            } else if (!checkBox.isChecked()) {
              selectedValues.remove(choiceValue);
            }
            // if onChangeParameterSubmit is true, submit results
            if (submitParametersOnChangeCheckBox.isChecked()) {
              submitParametersListener.onClick(checkBox);
            }
          }
        });
        buttonPanel.add(checkBox);
      }
      IParameterSubmissionCallback myCallback = new IParameterSubmissionCallback() {
        public void submitReportParameters() {
          parameter.clearSelections();
          for (String value : selectedValues) {
            if (parameter.getParameterType() == ReportParameter.DATE) {
              parameter.getDateValues().add(new Date(value));
            } else if (parameter.getParameterType() == ReportParameter.STRING) {
              parameter.getStringValues().add(value);
            } else if (parameter.getParameterType() == ReportParameter.NUMBER) {
              parameter.getNumberValues().add(new Integer(value));
            }
          }
        }
      };
      submitCallbacks.add(myCallback);
      return buttonPanel;

    } else if (parameter.getPromptType() == ReportParameter.SELECTION_TYPE_SLIDER) {
      final List<Number> selectedValues = new ArrayList<Number>();

      double min = 0;
      double max = 0;

      for (final Object choiceKey : parameter.getChoices().keySet()) {
        min = Math.min(min, ((Number) choiceKey).doubleValue());
        max = Math.max(max, ((Number) choiceKey).doubleValue());
      }

      double currentValue = 0;
      boolean valueSelected = false;
      if (parameter.getValues() != null) {
        for (Object selectedValue : parameter.getValues()) {
          currentValue = ((Number) selectedValue).doubleValue();
          valueSelected = true;
          break;
        }
      }
      // check if we need to select the default (user selection does not exist)
      if (!valueSelected && parameter.getDefaultValue() != null) {
        currentValue = ((Number) parameter.getDefaultValue()).doubleValue();
      }

//      final SliderBar slider = new SliderBar(min, max);
//      slider.setStepSize(5.0);
//      slider.setCurrentValue(currentValue);
//      slider.setNumTicks(10);
//      slider.setNumLabels(5);
//      slider.setWidth("400px");
//      slider.addChangeListener(new ChangeListener() {
//
//        public void onChange(Widget sender) {
//          selectedValues.clear();
//          selectedValues.add(slider.getCurrentValue());
//          // if onChangeParameterSubmit is true, submit results
//          if (submitParametersOnChangeCheckBox.isChecked()) {
//            submitParametersListener.onClick(slider);
//          }
//        }
//
//      });
//      IParameterSubmissionCallback myCallback = new IParameterSubmissionCallback() {
//        public void submitReportParameters() {
//          parameter.clearSelections();
//          if (parameter.getParameterType() == ReportParameter.NUMBER) {
//            parameter.getNumberValues().add(slider.getCurrentValue());
//          }
//        }
//      };
//      submitCallbacks.add(myCallback);
//      return slider;
    } else if (parameter.getPromptType() == ReportParameter.SELECTION_TYPE_TEXTBOX) {
      // if the parameter has no choices, it is probably a plain parameter
      if (parameter.getChoices() == null || parameter.getChoices().size() == 0) {
        final TextBox box = new TextBox();
        if (parameter.getDefaultValue() != null) {
          box.setText(parameter.getDefaultValue().toString());
        } else if (parameter.getValue() != null) {
          box.setText(parameter.getValue().toString());
        }
        IParameterSubmissionCallback myCallback = new IParameterSubmissionCallback() {
          public void submitReportParameters() {
            parameter.clearSelections();
            String value = box.getText();
            if (parameter.getParameterType() == ReportParameter.DATE) {
              parameter.getDateValues().add(new Date(value));
            } else if (parameter.getParameterType() == ReportParameter.STRING) {
              parameter.getStringValues().add(value);
            } else if (parameter.getParameterType() == ReportParameter.NUMBER) {
              parameter.getNumberValues().add(new Integer(value));
            }
          }
        };
        submitCallbacks.add(myCallback);
        return box;
      }
      // if the parameter has choices...
      MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
      for (final Object choiceKey : parameter.getChoices().keySet()) {
        final String choiceValue = parameter.getChoices().get(choiceKey).toString();
        oracle.add(choiceValue);
      }
      final SuggestBox box = new SuggestBox(oracle);
      if (parameter.getDefaultValue() != null) {
        box.setText(parameter.getDefaultValue().toString());
      } else if (parameter.getValue() != null) {
        box.setText(parameter.getValue().toString());
      }
    
      IParameterSubmissionCallback myCallback = new IParameterSubmissionCallback() {
        public void submitReportParameters() {
          parameter.clearSelections();
          String value = box.getText();
          if (parameter.getParameterType() == ReportParameter.DATE) {
            parameter.getDateValues().add(new Date(value));
          } else if (parameter.getParameterType() == ReportParameter.STRING) {
            parameter.getStringValues().add(value);
          } else if (parameter.getParameterType() == ReportParameter.NUMBER) {
            parameter.getNumberValues().add(new Integer(value));
          }
        }
      };
      submitCallbacks.add(myCallback);
      return box;
    }
    return null;
  }

}
