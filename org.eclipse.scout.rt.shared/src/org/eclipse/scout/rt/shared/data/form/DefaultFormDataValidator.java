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

import org.eclipse.scout.rt.shared.data.IValidator;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;
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

  public static final IValueCheck DEFAULT_REGEX_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Class<?> fieldType, Object fieldValue) throws Exception {
      if (fieldType.isArray()) {
        ValidationUtility.checkArrayMatchesRegex(fieldName, fieldValue, ruleValue);
      }
      else {
        ValidationUtility.checkValueMatchesRegex(fieldName, fieldValue, ruleValue);
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
    map.put(ValidationRule.REGEX, DEFAULT_REGEX_CHECK);
    DEFAULT_VALUE_CHECKS = Collections.unmodifiableMap(map);
  }

  private final IValidator m_baseValidator;
  private Map<String, IValueCheck> m_valueChecks;
  private int m_validationStrategy;

  public DefaultFormDataValidator(IValidator baseValidator, int validationStrategy) {
    m_baseValidator = baseValidator;
    m_validationStrategy = validationStrategy;
    m_valueChecks = DEFAULT_VALUE_CHECKS;
  }

  public IValidator getBaseValidator() {
    return m_baseValidator;
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
  protected void visitProperty(AbstractPropertyData<?> prop, Map<String/*rule name*/, Object/*rule value*/> ruleMap) throws Exception {
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
          valueType = prop.getHolderType();
          value = prop.getValue();
        }
        check.check(e.getKey(), e.getValue(), displayName, valueType, value);
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

}
