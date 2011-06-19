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
import org.eclipse.scout.rt.shared.util.FormDataUtility;

/**
 * Does input/output validation and rule checks on a form data with {@link AbstractFormFieldData#getValidationRules()},
 * see {@link ValidationRule}.
 * <p>
 * Business rules include configured maxlength, required, min, max and code/lookup value validation.
 */
public class DefaultFormDataValidator {

  public static interface IValueCheck {
    void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception;
  }

  public static final IValueCheck DEFAULT_MANDATORY_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkRequired(fieldName, fieldValue);
    }
  };

  public static final IValueCheck DEFAULT_MIN_LENGTH_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkMinLength(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MAX_LENGTH_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkMaxLength(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MIN_VALUE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkMinValue(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_MAX_VALUE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkMaxValue(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_CODE_TYPE_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkCodeType(fieldName, fieldValue, ruleValue);
    }
  };

  public static final IValueCheck DEFAULT_LOOKUP_CALL_CHECK = new IValueCheck() {
    @Override
    public void check(String ruleName, Object ruleValue, String fieldName, Object fieldValue) throws Exception {
      FormDataUtility.checkLookupCall(fieldName, fieldValue, ruleValue);
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

  public DefaultFormDataValidator(AbstractFormData formData) {
    m_formData = formData;
    m_valueChecks = DEFAULT_VALUE_CHECKS;
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
    for (Map<String, AbstractPropertyData<?>> map : getFormData().getAllPropertiesRec().values()) {
      for (AbstractPropertyData<?> prop : map.values()) {
        Map<String, Object> ruleMap = new HashMap<String, Object>();
        validateProperty(prop, ruleMap);
      }
    }
    for (Map<String, AbstractFormFieldData> map : getFormData().getAllFieldsRec().values()) {
      for (AbstractFormFieldData field : map.values()) {
        Map<String, Object> ruleMap = field.getValidationRules();
        validateField(field, ruleMap);
      }
    }
  }

  /**
   * Perform default checks on mandatory, maxLength, minValue, maxValue, codeType, lookupCall
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
    Object value = null;
    String displayName = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (displayName == null) {
          displayName = prop.getClass().getSimpleName();
        }
        if (value == null) {
          value = ((AbstractPropertyData<?>) prop).getValue();
        }
        check.check(e.getKey(), e.getValue(), displayName, value);
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
    Object value = null;
    String displayName = null;
    //apply rules
    for (Map.Entry<String, Object> e : ruleMap.entrySet()) {
      IValueCheck check = getValueChecks().get(e.getKey());
      if (check != null) {
        //lazy create
        if (displayName == null) {
          displayName = field.getClass().getSimpleName();
        }
        if (value == null) {
          if (field instanceof AbstractValueFieldData<?>) {
            value = ((AbstractValueFieldData<?>) field).getValue();
          }
        }
        check.check(e.getKey(), e.getValue(), displayName, value);
      }
    }
  }

}
