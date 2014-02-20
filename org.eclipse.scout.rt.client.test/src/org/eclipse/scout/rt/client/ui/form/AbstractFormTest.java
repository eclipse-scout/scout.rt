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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractFormTest.WrapperTestFormWithClassId.MainBox.EmbeddedField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractForm}
 * 
 * @since 3.10.0
 */
public class AbstractFormTest {
  private static final String FORM_TEST_CLASS_ID = "FORM_TEST_CLASS_ID";
  private static final String WRAPPER_FORM_TEST_CLASS_ID = "WRAPPER_FORM_TEST_CLASS_ID";
  private static final String WRAPPER_FORM_FIELD_ID = "WRAPPER_FORM_FIELD_ID";

  /**
   * {@link AbstractForm#classId()}
   */
  @Test
  public void testClassIdAnnotated() throws ProcessingException {
    TestFormWithClassId form = new TestFormWithClassId();
    assertEquals(FORM_TEST_CLASS_ID, form.classId());
    testClassIdSetter(form, FORM_TEST_CLASS_ID);
  }

  /**
   * {@link AbstractForm#classId()}
   */
  @Test
  public void testClassIdNoAnnotation() throws ProcessingException {
    TestFormWithoutClassId form = new TestFormWithoutClassId();
    assertFalse("ClassId should always be set.", StringUtility.isNullOrEmpty(form.classId()));
    testClassIdSetter(form, form.classId());
  }

  private void testClassIdSetter(IForm form, String expectedDefaultClassId) {
    String customClassId = "customClassId";
    form.setClassId(customClassId);
    assertEquals("Expected custom classId set by setClassId().", customClassId, form.classId());
    form.setClassId(null);
    assertEquals("Expected default classId after setClassId(null).", expectedDefaultClassId, form.classId());
  }

  /**
   * Test {@link AbstractForm#classId()} in a wrapped form
   */
  @Test
  public void testWrappedForm() throws ProcessingException {
    WrapperTestFormWithClassId form = new WrapperTestFormWithClassId();
    form.getEmbeddedField().setInnerForm(new TestFormWithClassId());
    String classId = form.getEmbeddedField().getInnerForm().classId();
    assertTrue("ClassId of innerform should contain outerFormField id.", classId.contains(WRAPPER_FORM_FIELD_ID));
    assertTrue("ClassId of innerform should contain formid.", classId.contains(FORM_TEST_CLASS_ID));
  }

  // Test classes

  @ClassId(FORM_TEST_CLASS_ID)
  class TestFormWithClassId extends AbstractForm {

    public TestFormWithClassId() throws ProcessingException {
      super();
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {
    }
  }

  class TestFormWithoutClassId extends AbstractForm {

    public TestFormWithoutClassId() throws ProcessingException {
      super();
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {
    }
  }

  @ClassId(WRAPPER_FORM_TEST_CLASS_ID)
  class WrapperTestFormWithClassId extends AbstractForm {

    public EmbeddedField getEmbeddedField() {
      return getFieldByClass(EmbeddedField.class);
    }

    public WrapperTestFormWithClassId() throws ProcessingException {
      super();
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {
      @Override
      protected void execInitField() throws ProcessingException {
        getFieldByClass(EmbeddedField.class).setInnerForm(new TestFormWithClassId());
      }

      @Order(10.0)
      @ClassId(WRAPPER_FORM_FIELD_ID)
      public class EmbeddedField extends AbstractWrappedFormField<TestFormWithClassId> {
      }
    }
  }

}
