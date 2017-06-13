/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.math.BigDecimal;
import java.security.Permission;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributeInitAttributeChain;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelAttributeChains.DataModelAttributePrepareLookupChain;
import org.eclipse.scout.rt.shared.extension.data.model.IDataModelAttributeExtension;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.lookup.ICodeLookupCallFactoryService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("350b5965-e92d-4f7e-b7b7-7135a572ff91")
public abstract class AbstractDataModelAttribute extends AbstractPropertyObserver implements IDataModelAttribute, DataModelConstants, Serializable, IExtensibleObject {

  private static final long serialVersionUID = 1L;
  private static final String ALLOW_NULL_OPERATOR = "ALLOW_NULL_OPERATOR";
  private static final String ALLOW_NOT_OPERATOR = "ALLOW_NOT_OPERATOR";
  private static final String AGGREGATION_ENABLED = "AGGREGATION_ENABLED";
  private static final String ACTIVE_FILTER_ENABLED = "ACTIVE_FILTER_ENABLED";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDataModelAttribute.class);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(ALLOW_NULL_OPERATOR, ALLOW_NOT_OPERATOR, AGGREGATION_ENABLED, ACTIVE_FILTER_ENABLED);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);

  private String m_text;
  private int m_type;
  private double m_order;
  private List<? extends IDataModelAttributeOp> m_operators;
  private int[] m_aggregationTypes;
  private String m_iconId;
  private Class<? extends ICodeType> m_codeTypeClass;
  private ILookupCall<?> m_lookupCall;
  private Permission m_visiblePermission;
  private IDataModelEntity m_parentEntity;
  private final ObjectExtensions<AbstractDataModelAttribute, IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> m_objectExtensions;

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This DataModelAttribute is visible, if all dimensions are visible (all bits
   * set).
   */
  private byte m_visible;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #ALLOW_NULL_OPERATOR}, {@link #ALLOW_NOT_OPERATOR}, {@link #AGGREGATION_ENABLED},
   * {@link #ACTIVE_FILTER_ENABLED}
   */
  private byte m_flags;

  public AbstractDataModelAttribute() {
    this(true);
  }

  /**
   * @param callInitConfig
   *          true if {@link #callInitializer()} should automatically be invoked, false if the subclass invokes
   *          {@link #callInitializer()} itself
   */
  public AbstractDataModelAttribute(boolean callInitConfig) {
    m_objectExtensions = new ObjectExtensions<AbstractDataModelAttribute, IDataModelAttributeExtension<? extends AbstractDataModelAttribute>>(this, false);
    if (callInitConfig) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    interceptInitConfig();
  }

  /**
   * Calculates the column's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && IDataModelAttribute.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    IDataModelEntity parentEntity = getParentEntity();
    if (parentEntity != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + parentEntity.classId();
    }
    return simpleClassId;
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

  /**
   * Configures the view order of this data model attribute. The view order determines the order in which the attributes
   * appear. The order of attributes with no view order configured ({@code < 0}) is initialized based on the
   * {@link Order} annotation of the attribute class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this attribute.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(130)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  @ConfigOperation
  @Order(10)
  protected void execInitAttribute() {

  }

  @ConfigOperation
  @Order(20)
  protected void execPrepareLookup(ILookupCall<?> call) {
  }

  @Override
  public Map<String, String> getMetaDataOfAttribute() {
    return null;
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    setNotOperatorEnabled(getConfiguredNotOperatorEnabled());
    setNullOperatorEnabled(getConfiguredNullOperatorEnabled());
    setAggregationEnabled(getConfiguredAggregationEnabled());
    setIconId(getConfiguredIconId());
    setText(getConfiguredText());
    setType(getConfiguredType());
    setVisible(getConfiguredVisible());
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    setOrder(calculateViewOrder());

    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends ILookupCall<?>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<?> call;
      try {
        call = BEANS.get(lookupCallClass);
        setLookupCall(call);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + lookupCallClass.getName() + "'.", e));
      }
    }
    injectOperators();
    injectAggregationTypes();
  }

  @Override
  public final List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IDataModelAttributeExtension<? extends AbstractDataModelAttribute> createLocalExtension() {
    return new LocalDataModelAttributeExtension<AbstractDataModelAttribute>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /*
   * Runtime
   */

  protected void injectOperators() {
    List<IDataModelAttributeOp> operatorList = new ArrayList<IDataModelAttributeOp>();
    for (IDataModelAttributeOperatorProvider injector : BEANS.all(IDataModelAttributeOperatorProvider.class)) {
      injector.injectOperators(this, operatorList);
    }

    setOperators(operatorList);
  }

  protected void injectAggregationTypes() {
    List<Integer> aggregationTypeList = new ArrayList<>();
    for (IDataModelAttributeAggregationTypeProvider injector : BEANS.all(IDataModelAttributeAggregationTypeProvider.class)) {
      injector.injectAggregationTypes(this, aggregationTypeList);
    }

    int[] a = new int[aggregationTypeList.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = aggregationTypeList.get(i);
    }

    setAggregationTypes(a);
  }

  @Override
  public final void initAttribute() {
    interceptInitAttribute();
  }

  @Override
  public void prepareLookup(ILookupCall<?> call) {
    interceptPrepareLookup(call);
  }

  @Override
  public double getOrder() {
    return m_order;
  }

  @Override
  public void setOrder(double order) {
    m_order = order;
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
    return CollectionUtility.arrayList(m_operators);
  }

  @Override
  public void setOperators(List<? extends IDataModelAttributeOp> ops) {
    m_operators = CollectionUtility.arrayList(ops);
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
    return FLAGS_BIT_HELPER.isBitSet(ALLOW_NULL_OPERATOR, m_flags);
  }

  @Override
  public void setNullOperatorEnabled(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ALLOW_NULL_OPERATOR, b, m_flags);
  }

  @Override
  public boolean isAggregationEnabled() {
    return FLAGS_BIT_HELPER.isBitSet(AGGREGATION_ENABLED, m_flags);
  }

  @Override
  public void setAggregationEnabled(boolean aggregationEnabled) {
    m_flags = FLAGS_BIT_HELPER.changeBit(AGGREGATION_ENABLED, aggregationEnabled, m_flags);
    if (m_aggregationTypes != null) {
      injectAggregationTypes();
    }
  }

  @Override
  public boolean isNotOperatorEnabled() {
    return FLAGS_BIT_HELPER.isBitSet(ALLOW_NOT_OPERATOR, m_flags);
  }

  @Override
  public void setNotOperatorEnabled(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ALLOW_NOT_OPERATOR, b, m_flags);
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
      m_lookupCall = BEANS.get(ICodeLookupCallFactoryService.class).newInstance((Class<? extends ICodeType<?, Object>>) m_codeTypeClass);
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
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
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
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public boolean isVisible() {
    return NamedBitMaskHelper.allBitsSet(m_visible);
  }

  @Override
  public void setVisible(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE);
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  @Override
  public void setActiveFilterEnabled(boolean active) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ACTIVE_FILTER_ENABLED, active, m_flags);
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return FLAGS_BIT_HELPER.isBitSet(ACTIVE_FILTER_ENABLED, m_flags);
  }

  @Override
  public IDataModelEntity getParentEntity() {
    return m_parentEntity;
  }

  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  @Override
  public boolean isMultiValued() {
    switch (getType()) {
      case DataModelConstants.TYPE_CODE_LIST:
      case DataModelConstants.TYPE_CODE_TREE:
      case DataModelConstants.TYPE_NUMBER_LIST:
      case DataModelConstants.TYPE_NUMBER_TREE:
        return true;
      default:
        return false;
    }
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
      case IDataModelAttribute.TYPE_BIG_DECIMAL:
        return formatBigDecimal(rawValue, true, false);
      case IDataModelAttribute.TYPE_PLAIN_INTEGER:
        return formatInteger(rawValue, false);
      case IDataModelAttribute.TYPE_PLAIN_LONG:
        return formatLong(rawValue, false);
      case IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL:
        return formatBigDecimal(rawValue, false, false);
      case IDataModelAttribute.TYPE_PERCENT:
        return formatBigDecimal(rawValue, true, true);
      case IDataModelAttribute.TYPE_STRING:
      case IDataModelAttribute.TYPE_FULL_TEXT:
        return formatString(rawValue);
      default:
        return formatObject(rawValue);
    }
  }

  /**
   * Method is called in case the raw value is null. This method may be overridden by subclass in order to provide a
   * different formatted value for null values.
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
      df = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
    }
    else if (!hasDate && hasTime) {
      df = BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, NlsLocale.get());
    }
    else {
      df = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get());
    }
    df.setLenient(true);

    return df.format(value);
  }

  /**
   * Formats the raw value for the following attribute types:
   * <ul>
   * <li>{@link DataModelConstants#TYPE_BIG_DECIMAL}</li>
   * <li>{@link DataModelConstants#TYPE_PLAIN_BIG_DECIMAL}</li>
   * <li>{@link DataModelConstants#TYPE_PERCENT}</li>
   * </ul>
   *
   * @param rawValue
   *          Raw value to format
   * @param groupingUsed
   *          True if grouping should be used for formatting
   * @param percent
   *          True if a percent number format should be used for formatting
   * @return Formatted value: raw value casted to BigDecimal, number format with 2 fraction digits
   */
  protected String formatBigDecimal(Object rawValue, boolean groupingUsed, boolean percent) {
    BigDecimal value = TypeCastUtility.castValue(rawValue, BigDecimal.class);

    NumberFormat fmt = null;
    if (percent) {
      fmt = BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get());
    }
    else {
      fmt = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
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

    NumberFormat fmt = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
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

    NumberFormat fmt = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
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
   * If whether code type class nor lookup call is set, the return value will be null. The method does not throw an
   * exception. In case of failure, the return value is the empty string.
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
      call = BEANS.get(ICodeLookupCallFactoryService.class).newInstance((Class<? extends ICodeType<?, Object>>) codeTypeClass);
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
    catch (RuntimeException e) {
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

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalDataModelAttributeExtension<OWNER extends AbstractDataModelAttribute> extends AbstractSerializableExtension<OWNER> implements IDataModelAttributeExtension<OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalDataModelAttributeExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInitAttribute(DataModelAttributeInitAttributeChain chain) {
      getOwner().execInitAttribute();
    }

    @Override
    public void execPrepareLookup(DataModelAttributePrepareLookupChain chain, ILookupCall<?> call) {
      getOwner().execPrepareLookup(call);
    }

  }

  protected final void interceptInitAttribute() {
    List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions = getAllExtensions();
    DataModelAttributeInitAttributeChain chain = new DataModelAttributeInitAttributeChain(extensions);
    chain.execInitAttribute();
  }

  protected final void interceptPrepareLookup(ILookupCall<?> call) {
    List<? extends IDataModelAttributeExtension<? extends AbstractDataModelAttribute>> extensions = getAllExtensions();
    DataModelAttributePrepareLookupChain chain = new DataModelAttributePrepareLookupChain(extensions);
    chain.execPrepareLookup(call);
  }
}
