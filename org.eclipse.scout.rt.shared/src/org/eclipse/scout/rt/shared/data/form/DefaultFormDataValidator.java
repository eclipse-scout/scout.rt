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
package org.eclipse.scout.rt.shared.data.form;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.IValidator;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.util.ValidationUtility;

/**
 * Does input/output validation and rule checks on a form data with {@link AbstractFormFieldData#getValidationRules()}
 * <p>
 * Business rules include checks of configured properties (see {@link ValidationRule})
 * <p>
 * In {@link ValidationStrategy#QUERY} the following properties are checked (see {@link #preprocessRuleMap(Map)}:
 * <ul>
 * <li>max length</li>
 * <li>code type value</li>
 * <li>lookup call value (incl. master value)</li>
 * <li>regex</li>
 * </ul>
 * <p>
 * In {@link ValidationStrategy#PROCESS} all properties are checked:
 * <ul>
 * <li>required/mandatory</li>
 * <li>min length</li>
 * <li>max length</li>
 * <li>min value</li>
 * <li>max value</li>
 * <li>code type value</li>
 * <li>lookup call value (incl. master value)</li>
 * <li>regex</li>
 * <li>master required</li>
 * </ul>
 * <p>
 * All elements with no specified maxLength are checked by {@link IValidator#checkMaxLenghtDefault(Object)}
 */
public class DefaultFormDataValidator {

  public static class ValueCheckContext {
    public AbstractFormData formData;
    public Map<String, Object> ruleMap;
    public String ruleName;
    public Object ruleValue;
    public String fieldName;
    public Class<?> fieldType;
    public Object fieldValue;
  }

  public static interface IValueCheck {
    void check(ValueCheckContext ctx) throws Exception;
  }

  public static class DefaultMandatoryCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      if (!Boolean.TRUE.equals(ctx.ruleValue)) {
        return;
      }
      if (ctx.fieldType.isArray()) {
        ValidationUtility.checkMandatoryArray(ctx.fieldName, ctx.fieldValue);
      }
      else {
        ValidationUtility.checkMandatoryValue(ctx.fieldName, treat0AsNullIfAppropriate(ctx));
      }
    }
  }

  public static class DefaultMinLengthCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      ValidationUtility.checkMinLength(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
    }
  }

  public static class DefaultMaxLengthCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      ValidationUtility.checkMaxLength(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
    }
  }

  public static class DefaultMinValueCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      ValidationUtility.checkMinValue(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
    }
  }

  public static class DefaultMaxValueCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      ValidationUtility.checkMaxValue(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
    }
  }

  public static class DefaultCodeTypeCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      if (ctx.fieldType.isArray()) {
        if (ctx.fieldValue != null && Array.getLength(ctx.fieldValue) > 0) {
          ValidationUtility.checkCodeTypeArray(ctx.fieldName, ctx.fieldValue, createCodeType(ctx));
        }
      }
      else {
        if (treat0AsNullIfAppropriate(ctx) != null) {
          ValidationUtility.checkCodeTypeValue(ctx.fieldName, ctx.fieldValue, createCodeType(ctx));
        }
      }
    }

    @SuppressWarnings("unchecked")
    protected ICodeType<?> createCodeType(ValueCheckContext ctx) throws ProcessingException {
      Class<? extends ICodeType<?>> cls = (Class<? extends ICodeType<?>>) ctx.ruleValue;
      ICodeType<?> codeType = CODES.getCodeType(cls);
      if (codeType == null) {
        throw new ProcessingException(ctx.fieldName + " codeType " + cls.getSimpleName() + " does not exist");
      }
      return codeType;
    }

  }

  public static class DefaultLookupCallCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      if (ctx.fieldType.isArray()) {
        if (ctx.fieldValue != null && Array.getLength(ctx.fieldValue) > 0) {
          ValidationUtility.checkLookupCallArray(ctx.fieldName, ctx.fieldValue, createLookupCall(ctx));
        }
      }
      else {
        if (treat0AsNullIfAppropriate(ctx) != null) {
          ValidationUtility.checkLookupCallValue(ctx.fieldName, ctx.fieldValue, createLookupCall(ctx));
        }
      }
    }

    @SuppressWarnings("unchecked")
    protected LookupCall createLookupCall(ValueCheckContext ctx) throws ProcessingException {
      Class<? extends LookupCall> cls = (Class<? extends LookupCall>) ctx.ruleValue;
      LookupCall call;
      try {
        call = cls.newInstance();
      }
      catch (Throwable t) {
        throw new ProcessingException(ctx.fieldName + " can not verify " + cls.getSimpleName());
      }
      //does a master value exist?
      if (ctx.ruleMap.containsKey(ValidationRule.MASTER_VALUE_FIELD)) {
        Class<? extends AbstractValueFieldData<?>> masterFieldClass = (Class<? extends AbstractValueFieldData<?>>) ctx.ruleMap.get(ValidationRule.MASTER_VALUE_FIELD);
        if (masterFieldClass == null) {
          throw new ProcessingException(ctx.fieldName + " missing master field");
        }
        AbstractValueFieldData<?> masterField = (AbstractValueFieldData<?>) ctx.formData.getFieldByClass(masterFieldClass);
        if (masterField == null) {
          throw new ProcessingException(ctx.fieldName + " missing master field " + masterFieldClass.getSimpleName());
        }
        call.setMaster(masterField.getValue());
      }
      return call;
    }
  }

  public static class DefaultRegexCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      if (ctx.fieldType.isArray()) {
        ValidationUtility.checkArrayMatchesRegex(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
      }
      else {
        ValidationUtility.checkValueMatchesRegex(ctx.fieldName, ctx.fieldValue, ctx.ruleValue);
      }
    }
  }

  public static class DefaultMasterValueRequiredCheck implements IValueCheck {
    @Override
    public void check(ValueCheckContext ctx) throws Exception {
      //if slave value is null, no rule applies
      Object slaveValue = ctx.fieldValue;
      if (slaveValue == null || (slaveValue.getClass().isArray() && Array.getLength(slaveValue) == 0)) {
        return;
      }
      //slave is set and not null
      //is a master value necessary?
      if (!Boolean.TRUE.equals(ctx.ruleMap.get(ValidationRule.MASTER_VALUE_REQUIRED))) {
        return;
      }
      @SuppressWarnings("unchecked")
      Class<? extends AbstractValueFieldData<?>> masterFieldClass = (Class<? extends AbstractValueFieldData<?>>) ctx.ruleMap.get(ValidationRule.MASTER_VALUE_FIELD);
      if (masterFieldClass == null) {
        throw new ProcessingException(ctx.fieldName + " missing master field");
      }
      AbstractValueFieldData<?> masterField = (AbstractValueFieldData<?>) ctx.formData.getFieldByClass(masterFieldClass);
      if (masterField == null) {
        throw new ProcessingException(ctx.fieldName + " missing master field " + masterFieldClass.getSimpleName());
      }
      Object masterValue = masterField.getValue();
      //if master value is null, then fail
      if (masterValue == null || (masterValue.getClass().isArray() && Array.getLength(masterValue) == 0)) {
        throw new ProcessingException(ctx.fieldName + " slave is set but master is null: " + masterFieldClass.getSimpleName() + " -> " + ctx.fieldName);
      }
    }
  }

  public static final Map<String, IValueCheck> DEFAULT_VALUE_CHECKS;

  static {
    HashMap<String, DefaultFormDataValidator.IValueCheck> map = new HashMap<String, DefaultFormDataValidator.IValueCheck>();
    map.put(ValidationRule.MANDATORY, new DefaultMandatoryCheck());
    map.put(ValidationRule.MIN_LENGTH, new DefaultMinLengthCheck());
    map.put(ValidationRule.MAX_LENGTH, new DefaultMaxLengthCheck());
    map.put(ValidationRule.MIN_VALUE, new DefaultMinValueCheck());
    map.put(ValidationRule.MAX_VALUE, new DefaultMaxValueCheck());
    map.put(ValidationRule.CODE_TYPE, new DefaultCodeTypeCheck());
    map.put(ValidationRule.LOOKUP_CALL, new DefaultLookupCallCheck());
    map.put(ValidationRule.REGEX, new DefaultRegexCheck());
    map.put(ValidationRule.MASTER_VALUE_REQUIRED, new DefaultMasterValueRequiredCheck());
    DEFAULT_VALUE_CHECKS = Collections.unmodifiableMap(map);
  }

  private final IValidator m_baseValidator;
  private AbstractFormData m_formData;
  private Map<String, IValueCheck> m_valueChecks;
  private int m_validationStrategy;

  public DefaultFormDataValidator(IValidator baseValidator, int validationStrategy) {
    this(baseValidator, validationStrategy, DEFAULT_VALUE_CHECKS);
  }

  public DefaultFormDataValidator(IValidator baseValidator, int validationStrategy, Map<String, IValueCheck> defaultValueChecks) {
    m_baseValidator = baseValidator;
    m_validationStrategy = validationStrategy;
    m_valueChecks = defaultValueChecks;
  }

  public IValidator getBaseValidator() {
    return m_baseValidator;
  }

  public AbstractFormData getFormData() {
    return m_formData;
  }

  protected void setFormData(AbstractFormData formData) {
    m_formData = formData;
  }

  public int getValidationStrategy() {
    return m_validationStrategy;
  }

  public void setValidationStrategy(int strategy) {
    m_validationStrategy = strategy;
  }

  public Map<String/*rule name*/, IValueCheck> getValueChecks() {
    return m_valueChecks;
  }

  public void setValueChecks(Map<String/*rule name*/, IValueCheck> valueChecks) {
    m_valueChecks = valueChecks;
  }

  public void validate(AbstractFormData formData) throws Exception {
    setFormData(formData);
    if (getValidationStrategy() == ValidationStrategy.NO_CHECK) {
      return;
    }
    for (Map<String, AbstractPropertyData<?>> map : formData.getAllPropertiesRec().values()) {
      for (AbstractPropertyData<?> prop : map.values()) {
        Map<String, Object> ruleMap = new HashMap<String, Object>();
        preprocessRuleMap(ruleMap);
        visitProperty(prop, ruleMap);
      }
    }
    for (Map<String, AbstractFormFieldData> map : formData.getAllFieldsRec().values()) {
      for (AbstractFormFieldData field : map.values()) {
        Map<String, Object> ruleMap = field.getValidationRules();
        preprocessRuleMap(ruleMap);
        visitField(field, ruleMap);
      }
    }
  }

  /**
   * Preprocess rule map to adapt to check strategy.
   */
  protected void preprocessRuleMap(Map<String, Object> ruleMap) {
    if (getValidationStrategy() == ValidationStrategy.QUERY) {
      ruleMap.remove(ValidationRule.MANDATORY);
      ruleMap.remove(ValidationRule.MIN_LENGTH);
      ruleMap.remove(ValidationRule.MIN_VALUE);
      ruleMap.remove(ValidationRule.MAX_VALUE);
      ruleMap.remove(ValidationRule.MASTER_VALUE_REQUIRED);
    }
  }

  /**
   * Perform default checks for mandatory, maxLength, minValue, maxValue, codeType, lookupCall
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   */
  protected void visitProperty(AbstractPropertyData<?> prop, Map<String/*rule name*/, Object/*rule value*/> ruleMap) throws Exception {
    if (!prop.isValueSet()) {
      return;
    }
    if (ruleMap.size() == 0) {
      return;
    }
    ValueCheckContext ctx = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (ctx == null) {
          ctx = new ValueCheckContext();
          ctx.formData = getFormData();
          ctx.fieldName = prop.getClass().getSimpleName();
          ctx.fieldType = prop.getHolderType();
          ctx.fieldValue = prop.getValue();
          ctx.ruleMap = ruleMap;
        }
        ctx.ruleName = e.getKey();
        ctx.ruleValue = e.getValue();
        check.check(ctx);
      }
    }
    //add default maxLength check
    if (getBaseValidator() != null && !ruleMap.containsKey(ValidationRule.MAX_LENGTH)) {
      getBaseValidator().checkMaxLenghtDefault(prop.getValue());
    }
  }

  /**
   * Perform default checks on mandatory, maxLength, minValue, maxValue, codeType, lookupCall
   * <p>
   * No check is performed if {@link AbstractFormFieldData#isValueSet()}=false
   */
  protected void visitField(AbstractFormFieldData field, Map<String/*rule name*/, Object/*rule value*/> ruleMap) throws Exception {
    if (!field.isValueSet()) {
      return;
    }
    if (ruleMap.size() == 0) {
      return;
    }
    ValueCheckContext ctx = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (ctx == null) {
          ctx = new ValueCheckContext();
          ctx.formData = getFormData();
          ctx.fieldName = field.getClass().getSimpleName();
          if (field instanceof AbstractValueFieldData<?>) {
            ctx.fieldType = ((AbstractValueFieldData<?>) field).getHolderType();
            ctx.fieldValue = ((AbstractValueFieldData<?>) field).getValue();
          }
          ctx.ruleMap = ruleMap;
        }
        ctx.ruleName = e.getKey();
        ctx.ruleValue = e.getValue();
        check.check(ctx);
      }
    }
    //subtree checks
    if (getBaseValidator() != null && !ruleMap.containsKey(ValidationRule.MAX_LENGTH)) {
      if (field instanceof AbstractValueFieldData<?>) {
        //add default maxLength check
        if (!ruleMap.containsKey(ValidationRule.MAX_LENGTH)) {
          getBaseValidator().checkMaxLenghtDefault(((AbstractValueFieldData<?>) field).getValue());
        }
      }
      else if (field instanceof AbstractTreeFieldData) {
        getBaseValidator().validate(field);
      }
      else if (field instanceof AbstractTableFieldData) {
        getBaseValidator().validate(field);
      }
    }
  }

  private static Object treat0AsNullIfAppropriate(ValueCheckContext ctx) throws ProcessingException {
    // special treatment of code type, lookup call and mandatory check if zero null equality is provided
    boolean zeroNullEquality = Boolean.TRUE.equals(ctx.ruleMap.get(ValidationRule.ZERO_NULL_EQUALITY));
    Object value = ctx.fieldValue;
    if (zeroNullEquality && value != null) {
      // equality is provided and a value is set
      // if provided value corresponds to 0, provided null to the check method (reject validation)
      if (value instanceof Number) {
        if (((Number) value).longValue() == 0L) {
          value = null;
        }
      }
    }
    return value;
  }
}
