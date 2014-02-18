/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model;

import java.io.Serializable;
import java.security.Permission;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.lookup.ICodeLookupCallFactoryService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

@SuppressWarnings("deprecation")
public abstract class AbstractDataModelAttribute extends AbstractPropertyObserver implements IDataModelAttribute, DataModelConstants, Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDataModelAttribute.class);
  private static final long serialVersionUID = 1L;

  private String m_id;
  private String m_text;
  private int m_type;
  private List<? extends IDataModelAttributeOp> m_operators;
  private int[] m_aggregationTypes;
  private String m_iconId;
  private boolean m_allowNullOperator;
  private boolean m_allowNotOperator;
  private boolean m_aggregationEnabled;
  private Class<? extends ICodeType> m_codeTypeClass;
  private ILookupCall<?> m_lookupCall;
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private boolean m_visible;
  private boolean m_activeFilterEnabled;
  private IDataModelEntity m_parentEntity;

  public AbstractDataModelAttribute() {
    this(true);
  }

  /**
   * @param callInitConfig
   *          true if {@link #callInitConfig()} should automcatically be invoked, false if the subclass invokes
   *          {@link #callInitConfig()} itself
   */
  public AbstractDataModelAttribute(boolean callInitConfig) {
    if (callInitConfig) {
      callInitConfig();
    }
  }

  protected void callInitConfig() {
    initConfig();
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(10)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(30)
  protected Class<? extends ILookupCall<?>> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(40)
  protected Class<? extends ICodeType<?, ?>> getConfiguredCodeType() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COMPOSER_ATTRIBUTE_TYPE)
  @Order(70)
  protected int getConfiguredType() {
    return TYPE_STRING;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredNullOperatorEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredNotOperatorEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(105)
  protected boolean getConfiguredAggregationEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(120)
  protected boolean getConfiguredActiveFilterEnabled() {
    return false;
  }

  @ConfigOperation
  @Order(10)
  protected void execInitAttribute() throws ProcessingException {

  }

  @ConfigOperation
  @Order(20)
  protected void execPrepareLookup(ILookupCall<?> call) throws ProcessingException {
  }

  @Override
  public Map<String, String> getMetaDataOfAttribute() {
    return null;
  }

  protected void initConfig() {
    m_visibleGranted = true;
    setNotOperatorEnabled(getConfiguredNotOperatorEnabled());
    setNullOperatorEnabled(getConfiguredNullOperatorEnabled());
    setAggregationEnabled(getConfiguredAggregationEnabled());
    setIconId(getConfiguredIconId());
    setText(getConfiguredText());
    setType(getConfiguredType());
    setVisible(getConfiguredVisible());
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());

    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends ILookupCall<?>> lsCls = getConfiguredLookupCall();
    if (lsCls != null) {
      ILookupCall<?> call;
      try {
        call = lsCls.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    injectOperators();
    injectAggregationTypes();
  }

  /*
   * Runtime
   */

  protected void injectOperators() {
    new DataModelAttributeInjector().injectOperators(this);
  }

  protected void injectAggregationTypes() {
    new DataModelAttributeInjector().injectAggregationTypes(this);
  }

  @Override
  public final void initAttribute() throws ProcessingException {
    execInitAttribute();
  }

  @Override
  public void prepareLookup(ILookupCall<?> call) throws ProcessingException {
    execPrepareLookup(call);
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public void setText(String s) {
    m_text = s;
  }

  @Override
  public int getType() {
    return m_type;
  }

  @Override
  public void setType(int i) {
    m_type = i;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void setIconId(String s) {
    m_iconId = s;
  }

  @Override
  public List<IDataModelAttributeOp> getOperators() {
    return Collections.unmodifiableList(m_operators);
  }

  @Override
  public void setOperators(List<? extends IDataModelAttributeOp> ops) {
    m_operators = ops;
  }

  @Override
  public int[] getAggregationTypes() {
    return m_aggregationTypes != null ? m_aggregationTypes : new int[0];
  }

  @Override
  public void setAggregationTypes(int[] aggregationTypes) {
    m_aggregationTypes = aggregationTypes;
  }

  @Override
  public boolean containsAggregationType(int agType) {
    if (m_aggregationTypes == null) {
      return false;
    }
    for (int i : m_aggregationTypes) {
      if (i == agType) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isNullOperatorEnabled() {
    return m_allowNullOperator;
  }

  @Override
  public void setNullOperatorEnabled(boolean b) {
    m_allowNullOperator = b;
  }

  @Override
  public boolean isAggregationEnabled() {
    return m_aggregationEnabled;
  }

  @Override
  public void setAggregationEnabled(boolean aggregationEnabled) {
    m_aggregationEnabled = aggregationEnabled;
    if (m_aggregationTypes != null) {
      injectAggregationTypes();
    }
  }

  @Override
  public boolean isNotOperatorEnabled() {
    return m_allowNotOperator;
  }

  @Override
  public void setNotOperatorEnabled(boolean b) {
    m_allowNotOperator = b;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends ICodeType<?, ?>> getCodeTypeClass() {
    return (Class<? extends ICodeType<?, ?>>) m_codeTypeClass;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setCodeTypeClass(Class<? extends ICodeType<?, ?>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = SERVICES.getService(ICodeLookupCallFactoryService.class).newInstance((Class<? extends ICodeType<?, Object>>) m_codeTypeClass);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public ILookupCall<Object> getLookupCall() {
    return (ILookupCall<Object>) m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<?> call) {
    m_lookupCall = call;
  }

  @Override
  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  @Override
  public void setVisiblePermission(Permission p) {
    setVisiblePermissionInternal(p);
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  protected void setVisiblePermissionInternal(Permission p) {
    m_visiblePermission = p;
  }

  @Override
  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  @Override
  public boolean isVisible() {
    return m_visible;
  }

  @Override
  public void setVisible(boolean b) {
    setVisibleProperty(b);
    calculateVisible();
  }

  protected void setVisibleProperty(boolean b) {
    m_visibleProperty = b;
  }

  protected boolean isVisibleProperty() {
    return m_visibleProperty;
  }

  @Override
  public void setActiveFilterEnabled(boolean active) {
    m_activeFilterEnabled = active;
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return m_activeFilterEnabled;
  }

  @Override
  public IDataModelEntity getParentEntity() {
    return m_parentEntity;
  }

  @Override
  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  /**
   * no access control for system buttons CANCEL and CLOSE
   */
  private void calculateVisible() {
    // access control
    m_visible = m_visibleGranted && m_visibleProperty;
  }

  @Override
  public boolean isMultiValued() {
    switch (getType()) {
      case DataModelConstants.TYPE_CODE_LIST:
      case DataModelConstants.TYPE_CODE_TREE:
      case DataModelConstants.TYPE_NUMBER_LIST:
      case DataModelConstants.TYPE_NUMBER_TREE: {
        return true;
      }
    }
    return false;
  }

  @Override
  public String formatValue(Object rawValue) {
    if (rawValue == null) {
      return formatNullValue();
    }

    switch (getType()) {
      case IDataModelAttribute.TYPE_CODE_LIST:
      case IDataModelAttribute.TYPE_CODE_TREE:
      case IDataModelAttribute.TYPE_NUMBER_LIST:
      case IDataModelAttribute.TYPE_NUMBER_TREE:
      case IDataModelAttribute.TYPE_SMART:
        return formatSmart(rawValue, getCodeTypeClass(), getLookupCall());
      case IDataModelAttribute.TYPE_DATE:
        return formatDate(rawValue, true, false);
      case IDataModelAttribute.TYPE_DATE_TIME:
        return formatDate(rawValue, true, true);
      case IDataModelAttribute.TYPE_TIME:
        return formatDate(rawValue, false, true);
      case IDataModelAttribute.TYPE_INTEGER:
        return formatInteger(rawValue, true);
      case IDataModelAttribute.TYPE_LONG:
        return formatLong(rawValue, true);
      case IDataModelAttribute.TYPE_DOUBLE:
        return formatDouble(rawValue, true, false);
      case IDataModelAttribute.TYPE_PLAIN_INTEGER:
        return formatInteger(rawValue, false);
      case IDataModelAttribute.TYPE_PLAIN_LONG:
        return formatLong(rawValue, false);
      case IDataModelAttribute.TYPE_PLAIN_DOUBLE:
        return formatDouble(rawValue, false, false);
      case IDataModelAttribute.TYPE_PERCENT:
        return formatDouble(rawValue, true, true);
      case IDataModelAttribute.TYPE_STRING:
      case IDataModelAttribute.TYPE_FULL_TEXT:
        return formatString(rawValue);
      default:
        return formatObject(rawValue);
    }
  }

  /**
   * Method is called in case the raw value is null.
   * This method may be overridden by subclass in order to provide a different formatted value for null values.
   * 
   * @return null
   */
  protected String formatNullValue() {
    return null;
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_DATE}</li>
   * <li>{@link DataModelConstants#TYPE_DATE_TIME}</li>
   * <li>{@link DataModelConstants#TYPE_TIME}</li>
   * </ul>
   * 
   * @param rawValue
   *          Raw value to format
   * @param hasDate
   *          True if the formatted value should show the date.
   * @param hasTime
   *          True if the formatted value should show the time.
   * @return Formatted value: raw value casted to Date, date format according to date & time specification
   */
  protected String formatDate(Object rawValue, boolean hasDate, boolean hasTime) {
    Date value = TypeCastUtility.castValue(rawValue, Date.class);

    DateFormat df = null;
    if (hasDate && !hasTime) {
      df = DateFormat.getDateInstance(DateFormat.MEDIUM, LocaleThreadLocal.get());
    }
    else if (!hasDate && hasTime) {
      df = DateFormat.getTimeInstance(DateFormat.SHORT, LocaleThreadLocal.get());
    }
    else {
      df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, LocaleThreadLocal.get());
    }
    df.setLenient(true);

    return df.format(value);
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_DOUBLE}</li>
   * <li>{@link DataModelConstants#TYPE_PLAIN_DOUBLE}</li>
   * <li>{@link DataModelConstants#TYPE_PERCENT}</li>
   * </ul>
   * 
   * @param rawValue
   *          Raw value to format
   * @param groupingUsed
   *          True if grouping should be used for formatting
   * @param percent
   *          True if a percent number format should be used for formatting
   * @return Formatted value: raw value casted to Double, number format with 2 fraction digits
   */
  protected String formatDouble(Object rawValue, boolean groupingUsed, boolean percent) {
    Double value = TypeCastUtility.castValue(rawValue, Double.class);

    NumberFormat fmt = null;
    if (percent) {
      fmt = NumberFormat.getPercentInstance(LocaleThreadLocal.get());
    }
    else {
      fmt = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
    }

    if (fmt instanceof DecimalFormat) {
      ((DecimalFormat) fmt).setMultiplier(1);
    }

    fmt.setMinimumFractionDigits(2);
    fmt.setMaximumFractionDigits(2);
    fmt.setGroupingUsed(groupingUsed);

    return fmt.format(value);
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_INTEGER}</li>
   * <li>{@link DataModelConstants#TYPE_PLAIN_INTEGER}</li>
   * </ul>
   * 
   * @param rawValue
   *          Raw value to format
   * @param groupingUsed
   *          True if grouping should be used for formatting
   * @return Formatted value: raw value casted to Integer, number format with no fraction digits
   */
  protected String formatInteger(Object rawValue, boolean groupingUsed) {
    Integer value = TypeCastUtility.castValue(rawValue, Integer.class);

    NumberFormat fmt = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
    fmt.setMinimumFractionDigits(0);
    fmt.setMaximumFractionDigits(0);
    fmt.setGroupingUsed(groupingUsed);
    return fmt.format(value);
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_LONG}</li>
   * <li>{@link DataModelConstants#TYPE_PLAIN_LONG}</li>
   * </ul>
   * 
   * @param rawValue
   *          Raw value to format
   * @param groupingUsed
   *          True if grouping should be used for formatting
   * @return Formatted value: raw value casted to Long, number format with no fraction digits
   */
  protected String formatLong(Object rawValue, boolean groupingUsed) {
    Long value = TypeCastUtility.castValue(rawValue, Long.class);

    NumberFormat fmt = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
    fmt.setMinimumFractionDigits(0);
    fmt.setMaximumFractionDigits(0);
    fmt.setGroupingUsed(groupingUsed);
    return fmt.format(value);
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_CODE_LIST}</li>
   * <li>{@link DataModelConstants#TYPE_CODE_TREE}</li>
   * <li>{@link DataModelConstants#TYPE_NUMBER_LIST}</li>
   * <li>{@link DataModelConstants#TYPE_NUMBER_TREE}</li>
   * <li>{@link DataModelConstants#TYPE_SMART}</li>
   * </ul>
   * If whether code type class nor lookup call is set, the return value will be null.
   * The method does not throw an exception. In case of failure, the return value is the empty string.
   * 
   * @param rawValue
   *          Raw value to format
   * @param codeTypeClass
   *          Code type class
   * @param lookupCall
   *          Lookup call (not used if code type class is set)
   * @return Formatted value: key is resolved by code type / lookup call
   */
  @SuppressWarnings("unchecked")
  protected String formatSmart(Object rawValue, Class<? extends ICodeType<?, ?>> codeTypeClass, ILookupCall<?> lookupCall) {
    if (codeTypeClass == null && lookupCall == null) {
      return null;
    }

    ILookupCall<Object> call;
    if (codeTypeClass != null) {
      call = SERVICES.getService(ICodeLookupCallFactoryService.class).newInstance((Class<? extends ICodeType<?, Object>>) codeTypeClass);
    }
    else if (lookupCall instanceof LookupCall<?>) {
      call = (ILookupCall<Object>) ((LookupCall<Object>) lookupCall).clone();
    }
    else {
      return null;
    }

    call.setKey(rawValue);
    call.setText(null);
    call.setAll(null);
    call.setRec(null);

    try {
      List<? extends ILookupRow<?>> result = call.getDataByKey();
      if (result.size() == 1) {
        return result.get(0).getText();
      }
      else if (result.size() > 1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
          if (i > 0) {
            sb.append(", ");
          }
          sb.append(result.get(i).getText());
        }
        return sb.toString();
      }
    }
    catch (ProcessingException e) {
      LOG.warn("Execution of lookup call failed", e);
    }
    return "";
  }

  /**
   * Formats the raw value for the following attribute types and in case the type did not match any known types.
   * <ul>
   * <li>{@link DataModelConstants#TYPE_STRING}</li>
   * <li>{@link DataModelConstants#TYPE_FULL_TEXT}</li>
   * </ul>
   * 
   * @param rawValue
   *          Raw value to format
   * @return Formatted value: raw value is casted to String
   */
  protected String formatString(Object rawValue) {
    return TypeCastUtility.castValue(rawValue, String.class);
  }

  /**
   * Formats the raw value for unknown attribute types
   * 
   * @param rawValue
   *          Raw value to format
   * @return Formatted value: raw value is casted to String
   */
  protected String formatObject(Object rawValue) {
    return rawValue.toString();
  }
}
