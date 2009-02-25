package org.pentaho.mantle.server.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.ReportParameter;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultParameterDefinition;
import org.pentaho.reporting.engine.classic.core.parameters.DefaultReportParameterValidator;
import org.pentaho.reporting.engine.classic.core.parameters.ListParameter;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterValues;
import org.pentaho.reporting.engine.classic.core.parameters.PlainParameter;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationResult;

public class ReportParameterHelper {
  
  private static final String parameterNamespace = AttributeNames.Core.NAMESPACE;
  private static final String parameterSelectionTypeAttribute = "parameter-render-type";
  
  public void processReportParameters(MasterReport report, List<ReportParameter> inReportParameters, ReportContainer reportContainer)
      throws ReportDataFactoryException {
    // insert parameter handling
    // report.setProperty("REGION", new String[] {"Southern"});
    ParameterContext parameterContext = new DefaultParameterContext(report);
    parameterContext.open();
    for (ParameterDefinitionEntry parameterDefinition : report.getParameterDefinition().getParameterDefinitions()) {
      if (parameterDefinition instanceof ListParameter) {
        ListParameter listParameter = (ListParameter) parameterDefinition;
        // process list parameter, apply input report parameters to report if possible
        ReportParameter reportParameter = processListParameter(listParameter, parameterContext, inReportParameters, report);
        reportContainer.getReportParameters().add(reportParameter);
      } else if (parameterDefinition instanceof PlainParameter) {
        // simple parameter, single selection
        PlainParameter plainParameter = (PlainParameter) parameterDefinition;
        // process plain parameter, apply input report parameters to report if possible
        ReportParameter reportParameter = processPlainParameter(plainParameter, parameterContext, inReportParameters, report);
        reportContainer.getReportParameters().add(reportParameter);
      }
    }
    if (report.getParameterDefinition() instanceof DefaultParameterDefinition) {
      ((DefaultParameterDefinition) report.getParameterDefinition()).setValidator(new DefaultReportParameterValidator());
    }
    ValidationResult vr = report.getParameterDefinition().getValidator().validate(new ValidationResult(), report.getParameterDefinition(), parameterContext);
    reportContainer.setPromptNeeded(!vr.isEmpty());
    parameterContext.close();
    System.out.println("Prompt Needed: " + reportContainer.isPromptNeeded());
  }

  private ReportParameter processPlainParameter(PlainParameter plainParameter, ParameterContext parameterContext, List<ReportParameter> inReportParameters,
      MasterReport report) throws ReportDataFactoryException {
    ReportParameter reportParameter = null;

    if (inReportParameters != null) {
      for (ReportParameter inReportParameter : inReportParameters) {
        if (inReportParameter.getName().equals(plainParameter.getName())) {
          reportParameter = inReportParameter;
          System.out.println("Received Parameter: " + plainParameter.getName() + " = " + inReportParameter.getValue());
          report.getParameterValues().put(plainParameter.getName(), inReportParameter.getValue());
          break;
        }
      }
    }
    if (reportParameter == null) {
      reportParameter = new ReportParameter();
      if (Number.class.isAssignableFrom(plainParameter.getValueType())) {
        reportParameter.setDefaultNumberValue((Number) plainParameter.getDefaultValue(parameterContext));
      } else if (plainParameter.getValueType().isAssignableFrom(String.class)) {
        reportParameter.setDefaultStringValue((String) plainParameter.getDefaultValue(parameterContext));
      } else if (plainParameter.getValueType().isAssignableFrom(Date.class)) {
        reportParameter.setDefaultDateValue((Date) plainParameter.getDefaultValue(parameterContext));
      }
      reportParameter.setName(plainParameter.getName());
      reportParameter.setMultiSelect(false);
      reportParameter.setParameterType(getParameterType(plainParameter));
      reportParameter.setPromptType(getPromptType(plainParameter, parameterContext));
    }
    
    return reportParameter;
  }
  
  private ReportParameter processListParameter(ListParameter listParameter, ParameterContext parameterContext, List<ReportParameter> inReportParameters,
      MasterReport report) throws ReportDataFactoryException {
    ReportParameter reportParameter = null;
    if (inReportParameters != null) {
      for (ReportParameter inReportParameter : inReportParameters) {
        if (inReportParameter.getName().equals(listParameter.getName())) {
          reportParameter = inReportParameter;
          if (listParameter.isAllowMultiSelection()) {
            Object values[] = inReportParameter.getValues().toArray();
            for (int i = 0; i < values.length; i++) {
              System.out.println("Received Parameter[" + i + "]: " + listParameter.getName() + " = " + values[i]);
            }
            report.getParameterValues().put(listParameter.getName(), inReportParameter.getValues().toArray(new String[] {}));
          } else {
            System.out.println("Received Parameter: " + listParameter.getName() + " = " + inReportParameter.getValue());
            report.getParameterValues().put(listParameter.getName(), inReportParameter.getValue());
          }
          break;
        }
      }
    }
    if (reportParameter == null) {
      reportParameter = new ReportParameter();
      if (Number.class.isAssignableFrom(listParameter.getValueType())) {
        reportParameter.setDefaultNumberValue((Number) listParameter.getDefaultValue(parameterContext));
        HashMap<Number, String> parameterChoices = getNumberParameterChoices((ListParameter) listParameter, parameterContext);
        reportParameter.setNumberParameterChoices(parameterChoices);
      } else if (listParameter.getValueType().isAssignableFrom(String.class)) {
        reportParameter.setDefaultStringValue((String) listParameter.getDefaultValue(parameterContext));
        HashMap<String, String> parameterChoices = getStringParameterChoices((ListParameter) listParameter, parameterContext);
        reportParameter.setStringParameterChoices(parameterChoices);
      } else if (listParameter.getValueType().isAssignableFrom(Date.class)) {
        reportParameter.setDefaultDateValue((Date) listParameter.getDefaultValue(parameterContext));
        HashMap<Date, String> parameterChoices = getDateParameterChoices((ListParameter) listParameter, parameterContext);
        reportParameter.setDateParameterChoices(parameterChoices);
      }
      reportParameter.setName(listParameter.getName());
      reportParameter.setMultiSelect(listParameter.isAllowMultiSelection());
      reportParameter.setParameterType(getParameterType(listParameter));
      reportParameter.setPromptType(getPromptType(listParameter, parameterContext));
    }
    return reportParameter;
  }

  private static int getPromptType(ParameterDefinitionEntry parameter, ParameterContext parameterContext) {
    String attribute = parameter.getParameterAttribute(parameterNamespace, parameterSelectionTypeAttribute, parameterContext);
    if ("list".equals(attribute)) {
      return ReportParameter.SELECTION_TYPE_LIST;
    } else if ("textbox".equals(attribute)) {
      return ReportParameter.SELECTION_TYPE_TEXTBOX;
    } else if ("select".equals(attribute)) {
      return ReportParameter.SELECTION_TYPE_SELECT;
    } else if ("slider".equals(attribute)) {
      return ReportParameter.SELECTION_TYPE_SLIDER;
    }
    return ReportParameter.SELECTION_TYPE_TEXTBOX;
  }
  
  private static int getParameterType(ParameterDefinitionEntry parameter) {
    Class parameterTypeClass = parameter.getValueType();
    if (Number.class.isAssignableFrom(parameterTypeClass)) {
      return ReportParameter.NUMBER;
    } else if (String.class.isAssignableFrom(parameterTypeClass)) {
      return ReportParameter.STRING;
    } else if (Date.class.isAssignableFrom(parameterTypeClass)) {
      return ReportParameter.DATE;
    }
    return ReportParameter.STRING;
  }

  private HashMap<Date, String> getDateParameterChoices(ListParameter parameter, ParameterContext parameterContext) throws ReportDataFactoryException {
    ParameterValues values = parameter.getValues(parameterContext);
    // id/value mappings for each parameter
    HashMap<Date, String> parameters = new HashMap<Date, String>();
    for (int i = 0; i < values.getRowCount(); i++) {
      Date key = (Date) values.getKeyValue(i);
      String value = (String) values.getTextValue(i);
      parameters.put(key, value);
    }
    return parameters;
  }

  private HashMap<String, String> getStringParameterChoices(ListParameter parameter, ParameterContext parameterContext) throws ReportDataFactoryException {
    ParameterValues values = parameter.getValues(parameterContext);
    // id/value mappings for each parameter
    HashMap<String, String> parameters = new HashMap<String, String>();
    for (int i = 0; i < values.getRowCount(); i++) {
      String key = (String) values.getKeyValue(i);
      String value = (String) values.getTextValue(i);
      parameters.put(key, value);
    }
    return parameters;
  }

  private HashMap<Number, String> getNumberParameterChoices(ListParameter parameter, ParameterContext parameterContext) throws ReportDataFactoryException {
    ParameterValues values = parameter.getValues(parameterContext);
    // id/value mappings for each parameter
    HashMap<Number, String> parameters = new HashMap<Number, String>();
    for (int i = 0; i < values.getRowCount(); i++) {
      Number key = (Number) values.getKeyValue(i);
      String value = (String) values.getTextValue(i).toString();
      parameters.put(key, value);
    }
    return parameters;
  }

}
