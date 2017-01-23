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
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.junit.Test;

public class FormDataToStringTest {

  @Test
  public void testEmpty() {
    FormDataFixture d = new FormDataFixture();
    assertEquals("FormDataFixture[]", d.toString());
  }

  @Test
  public void testStringProperty() {
    FormDataFixture d = new FormDataFixture();
    d.getPropertyByClass(FormDataFixture.StringProperty.class).setValue("S");
    assertEquals("FormDataFixture[\n  StringProperty: S\n]", d.toString());
  }

  @Test
  public void testStringField() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.StringValue.class).setValue("S");
    assertEquals("FormDataFixture[\n  StringValue: S\n]", d.toString());
  }

  @Test
  public void testStringPropertyAndStringField() {
    FormDataFixture d = new FormDataFixture();
    d.getPropertyByClass(FormDataFixture.StringProperty.class).setValue("S");
    d.getFieldByClass(FormDataFixture.StringValue.class).setValue("S");
    assertEquals("FormDataFixture[\n  StringProperty: S\n  StringValue: S\n]", d.toString());
  }

  @Test
  public void testIntArrayFieldEmpty() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.IntArrayValue.class).setValue(new int[]{});
    assertEquals("FormDataFixture[\n  IntArrayValue: []\n]", d.toString());
  }

  @Test
  public void testIntArrayField() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.IntArrayValue.class).setValue(new int[]{1, 2, 3});
    assertEquals("FormDataFixture[\n  IntArrayValue: [1,2,3]\n]", d.toString());
  }

  @Test
  public void testIntIntArrayField() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.IntIntArrayValue.class).setValue(new int[][]{new int[]{1, 2}, new int[]{3, 4}});
    assertEquals("FormDataFixture[\n  IntIntArrayValue: [[1,2],[3,4]]\n]", d.toString());
  }

  @Test
  public void testLongArrayField() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.LongArrayValue.class).setValue(new Long[]{1L, 2L, 3L});
    assertEquals("FormDataFixture[\n  LongArrayValue: [1,2,3]\n]", d.toString());
  }

  @Test
  public void testFieldWithProperty() {
    FormDataFixture d = new FormDataFixture();
    d.getFieldByClass(FormDataFixture.FieldWithProperty.class).setValue("F");
    d.getFieldByClass(FormDataFixture.FieldWithProperty.class).getPropertyByClass(FormDataFixture.FieldWithProperty.InnerProperty.class).setValue("P");
    assertEquals("FormDataFixture[\n  FieldWithProperty: F\n    InnerProperty: P\n]", d.toString());
  }

  @IgnoreBean
  private static final class FormDataFixture extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public FormDataFixture() {
    }

    public class StringProperty extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class StringValue extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class FieldWithProperty extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public class InnerProperty extends AbstractPropertyData<String> {
        private static final long serialVersionUID = 1L;
      }

    }

    public class IntArrayValue extends AbstractValueFieldData<int[]> {
      private static final long serialVersionUID = 1L;
    }

    public class IntIntArrayValue extends AbstractValueFieldData<int[][]> {
      private static final long serialVersionUID = 1L;
    }

    public class LongArrayValue extends AbstractValueFieldData<Long[]> {
      private static final long serialVersionUID = 1L;
    }
  }

}
