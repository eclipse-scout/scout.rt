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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * Does input validation and static rule checks on a form data.
 * <p>
 * Business rules include configured maxlength, required, min, max and code/lookup value validation.
 */
public class DefaultFormDataValidator {
  private final AbstractFormData m_formData;

  public DefaultFormDataValidator(AbstractFormData formData) {
    m_formData = formData;
  }

  public AbstractFormData getFormData() {
    return m_formData;
  }

  public void validate() throws Exception {
    for (Map<String, AbstractPropertyData<?>> map : getFormData().getAllPropertiesRec().values()) {
      for (AbstractPropertyData<?> prop : map.values()) {
        Map<String, Object> ruleMap = fetchRules(prop);
        validateProperty(prop, ruleMap);
      }
    }
    for (Map<String, AbstractFormFieldData> map : getFormData().getAllFieldsRec().values()) {
      for (AbstractFormFieldData field : map.values()) {
        Map<String, Object> ruleMap = fetchRules(field);
        if (field instanceof AbstractValueFieldData<?>) {
          validateValueField((AbstractValueFieldData<?>) field, ruleMap);
        }
        else {
          validateBasicField(field, ruleMap);
        }
      }
    }
  }

  protected void validateProperty(AbstractPropertyData<?> prop, Map<String, Object> ruleMap) throws Exception {
  }

  protected void validateBasicField(AbstractFormFieldData field, Map<String, Object> ruleMap) throws Exception {
  }

  protected void validateValueField(AbstractValueFieldData<?> field, Map<String, Object> ruleMap) throws Exception {
  }

  /**
   * @return the map of all the rules of the object's class and all its super classes according to
   *         {@link ValidationRule}
   */
  protected Map<String, Object> fetchRules(Object obj) throws Exception {
    if (obj == null) {
      return Collections.emptyMap();
    }
    HashMap<String, Object> ruleMap = new HashMap<String, Object>();
    Class c = obj.getClass();
    while (c != null) {
      try {
        Field f = c.getField("validationRules");
        @SuppressWarnings("unchecked")
        Map<String, Object> staticMap = (Map<String, Object>) f.get(null);
        if (staticMap != null) {
          for (Map.Entry<String, Object> entry : staticMap.entrySet()) {
            if (!ruleMap.containsKey(entry.getKey())) {
              ruleMap.put(entry.getKey(), entry.getValue());
            }
          }
        }
      }
      catch (NoSuchFieldException e) {
        //nop
      }
      //super class
      c = c.getSuperclass();
    }
    return ruleMap;
  }

}
