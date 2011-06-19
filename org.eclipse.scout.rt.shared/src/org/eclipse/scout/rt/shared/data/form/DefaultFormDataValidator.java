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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.util.ValidationUtility;

/**
 * Does input/output validation and rule checks on a form data with {@link AbstractFormFieldData#getValidationRules()},
 * see {@link ValidationRule}.
 * <p>
 * Business rules include configured maxlength, required, min, max and code/lookup value validation.
 * <p>
 * All elements with no maxLength limit are limited to 250 characters (String) resp. 50MB (arrays)
 */
public class DefaultFormDataValidator {

  public static interface IValueCheck {
    void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception;
  }

  public static final IValueCheck DEFAULT_MANDATORY_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      if (!Boolean.TRUE.equals(ruleValue)) {
        return;
      }
      if (fieldType.isArray()) {
        ValidationUtility.checkMandatoryArray(fieldName, fieldValue);
      }
      else {
        ValidationUtility.checkMandatoryValue(fieldName, fieldValue);
      }
    }
  };

  public static final IValueCheck DEFAULT_MIN_LENGTH_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      ValidationUtility.checkMinLength(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MAX_LENGTH_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      ValidationUtility.checkMaxLength(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MIN_VALUE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      ValidationUtility.checkMinValue(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MAX_VALUE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      ValidationUtility.checkMaxValue(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_CODE_TYPE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      if (fieldType.isArray()) {
        ValidationUtility.checkCodeTypeArray(fieldName, fieldValue, ruleValue);
      }
      else {
        ValidationUtility.checkCodeTypeValue(fieldName, fieldValue, ruleValue);
      }
    }
  };

  public static final IValueCheck DEFAULT_LOOKUP_CALL_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      if (fieldType.isArray()) {
        ValidationUtility.checkLookupCallArray(fieldName, fieldValue, ruleValue);
      }
      else {
        ValidationUtility.checkLookupCallValue(fieldName, fieldValue, ruleValue);
      }
    }
  };

  public static final Map<String, IValueCheck> DEFAULT_VALUE_CHECKS;

  static {
    HashMap<String, DefaultFormDataValidator.IValueCheck> map = new HashMap<String, DefaultFormDataValidator.IValueCheck>();
    map.put(ValidationRule.MANDATORY, DEFAULT_MANDATORY_CHECK);
    map.put(ValidationRule.MIN_LENGTH, DEFAULT_MIN_LENGTH_CHECK);
    map.put(ValidationRule.MAX_LENGTH, DEFAULT_MAX_LENGTH_CHECK);
    map.put(ValidationRule.MIN_VALUE, DEFAULT_MIN_VALUE_CHECK);
    map.put(ValidationRule.MAX_VALUE, DEFAULT_MAX_VALUE_CHECK);
    map.put(ValidationRule.CODE_TYPE, DEFAULT_CODE_TYPE_CHECK);
    map.put(ValidationRule.LOOKUP_CALL, DEFAULT_LOOKUP_CALL_CHECK);
    DEFAULT_VALUE_CHECKS = Collections.unmodifiableMap(map);
  }

  private final AbstractFormData m_formData;
  private Map<String, IValueCheck> m_valueChecks;
  private int m_validationStrategy;

  public DefaultFormDataValidator(int validationStrategy, AbstractFormData formData) {
    m_validationStrategy = validationStrategy;
    m_formData = formData;
    m_valueChecks = DEFAULT_VALUE_CHECKS;
  }

  public int getValidationStrategy() {
    return m_validationStrategy;
  }

  public void setValidationStrategy(int strategy) {
    m_validationStrategy = strategy;
  }

  public AbstractFormData getFormData() {
    return m_formData;
  }

  public Map<String/*rule name*/, IValueCheck> getValueChecks() {
    return m_valueChecks;
  }

  public void setValueChecks(Map<String/*rule name*/, IValueCheck> valueChecks) {
    m_valueChecks = valueChecks;
  }

  public void validate() throws Exception {
    if (getValidationStrategy() == ValidationStrategy.NO_CHECK) {
      return;
    }
    for (Map<String, AbstractPropertyData<?>> map : getFormData().getAllPropertiesRec().values()) {
      for (AbstractPropertyData<?> prop : map.values()) {
        Map<String, Object> ruleMap = new HashMap<String, Object>();
        preprocessRuleMap(ruleMap);
        validateProperty(prop, ruleMap);
      }
    }
    for (Map<String, AbstractFormFieldData> map : getFormData().getAllFieldsRec().values()) {
      for (AbstractFormFieldData field : map.values()) {
        Map<String, Object> ruleMap = field.getValidationRules();
        preprocessRuleMap(ruleMap);
        validateField(field, ruleMap);
      }
    }
  }

  /**
   * Preprocess rule map to adapt to check strategy.
   * <p>
   * In case of {@link ValidationStrategy#Query} the default removes mandatory, minLength, minValue, maxValue checks.
   */
  protected void preprocessRuleMap(Map<String, Object> ruleMap) {
    if (getValidationStrategy() == ValidationStrategy.QUERY) {
      ruleMap.remove(ValidationRule.MANDATORY);
      ruleMap.remove(ValidationRule.MIN_LENGTH);
      ruleMap.remove(ValidationRule.MIN_VALUE);
      ruleMap.remove(ValidationRule.MAX_VALUE);
    }
  }

  /**
   * Perform default checks for mandatory, maxLength, minValue, maxValue, codeType, lookupCall
   * <p>
   * No check is performed if {@link AbstractPropertyData#isValueSet()}=false
   */
  protected void validateProperty(AbstractPropertyData<?> prop, Map<String/*rule name*/, Object/*rule value*/> ruleMap) throws Exception {
    if (!prop.isValueSet()) {
      return;
    }
    if (ruleMap.size() == 0) {
      return;
    }
    String displayName = null;
    Class<?> valueType = null;
    Object value = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (displayName == null) {
          displayName = prop.getClass().getSimpleName();
        }
        if (valueType == null || value == null) {
          valueType = ((AbstractPropertyData<?>) prop).getHolderType();
          value = ((AbstractPropertyData<?>) prop).getValue();
        }
        check.check(e.getKey(), e.getValue(), displayName, valueType, value);
      }
    }
    //add default maxLength check
    if (!ruleMap.containsKey(ValidationRule.MAX_LENGTH)) {
      if (prop.getHolderType() == String.class) {
        ValidationUtility.checkMaxLength(prop.getClass().getSimpleName(), prop.getValue(), 250);
        return;
      }
      else if (prop.getHolderType().isArray()) {
        //50MB
        ValidationUtility.checkMaxLength(prop.getClass().getSimpleName(), prop.getValue(), 50000000);
        return;
      }
    }
  }

  /**
   * Perform default checks on mandatory, maxLength, minValue, maxValue, codeType, lookupCall
   * <p>
   * No check is performed if {@link AbstractFormFieldData#isValueSet()}=false
   */
  protected void validateField(AbstractFormFieldData field, Map<String/*rule name*/, Object/*rule value*/> ruleMap) throws Exception {
    if (!field.isValueSet()) {
      return;
    }
    if (ruleMap.size() == 0) {
      return;
    }
    String displayName = null;
    Class<?> valueType = null;
    Object value = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (displayName == null) {
          displayName = field.getClass().getSimpleName();
        }
        if (valueType == null || value == null) {
          if (field instanceof AbstractValueFieldData<?>) {
            valueType = ((AbstractValueFieldData<?>) field).getHolderType();
            value = ((AbstractValueFieldData<?>) field).getValue();
          }
        }
        check.check(e.getKey(), e.getValue(), displayName, valueType, value);
      }
    }
    //add default maxLength check
    if (!ruleMap.containsKey(ValidationRule.MAX_LENGTH)) {
      if (field instanceof AbstractValueFieldData<?>) {
        AbstractValueFieldData<?> valueField = (AbstractValueFieldData<?>) field;
        if (valueField.getHolderType() == String.class) {
          ValidationUtility.checkMaxLength(valueField.getClass().getSimpleName(), valueField.getValue(), 250);
          return;
        }
        else if (valueField.getHolderType().isArray()) {
          //50MB
          ValidationUtility.checkMaxLength(valueField.getClass().getSimpleName(), valueField.getValue(), 50000000);
          return;
        }
      }
    }
  }

}
