/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form.fixture;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public abstract class AbstractTestGroupBoxData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractTestGroupBoxData() {
  }

  public InnerText1 getInnerText1() {
    return getFieldByClass(InnerText1.class);
  }

  public TestListBox getTestListBox() {
    return getFieldByClass(TestListBox.class);
  }

  public Text1 getText1() {
    return getFieldByClass(Text1.class);
  }

  public Text2 getText2() {
    return getFieldByClass(Text2.class);
  }

  public static class InnerText1 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public InnerText1() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }

  public static class TestListBox extends AbstractValueFieldData<String[]> {
    private static final long serialVersionUID = 1L;

    public TestListBox() {
    }
  }

  public static class Text1 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text1() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }

  public static class Text2 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text2() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }
}
