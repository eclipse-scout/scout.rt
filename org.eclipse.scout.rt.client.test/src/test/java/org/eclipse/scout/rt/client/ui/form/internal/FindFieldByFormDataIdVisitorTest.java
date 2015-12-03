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
package org.eclipse.scout.rt.client.ui.form.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link FindFieldByFormDataIdVisitor}. Bugzilla entry 399610: Form: exportFormData/importFormData incorrect
 * for forms containing wrapped inner forms. https://bugs.eclipse.org/bugs/show_bug.cgi?id=399610
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FindFieldByFormDataIdVisitorTest {

  private static final String FIRST_FIELD_ID = "FirstField";
  private static final String SECOND_FIELD_ID = "SecondField";
  private static final String THIRD_FIELD_ID = "ThirdField";

  private static final String FIRST_FIELD_DATA_ID = FormDataUtility.getFieldDataId(FIRST_FIELD_ID);
  private static final String SECOND_FIELD_DATA_ID = FormDataUtility.getFieldDataId(SECOND_FIELD_ID);
  private static final String THIRD_FIELD_DATA_ID = FormDataUtility.getFieldDataId(THIRD_FIELD_ID);

  private MainForm m_mainForm;

  @Before
  public void before() throws Exception {
    m_mainForm = new MainForm();
    m_mainForm.start();
  }

  @After
  public void after() throws Exception {
    m_mainForm.doReset();
    m_mainForm.doCancel();
    m_mainForm = null;
  }

  @Test
  public void testFixture() throws Exception {
    assertNotNull(m_mainForm);
    assertEquals(FIRST_FIELD_ID, m_mainForm.getFirstField().getFieldId());
    InnerForm innerForm = m_mainForm.getWrappedFormField().getInnerForm();
    assertNotNull(innerForm);
    assertEquals(FIRST_FIELD_ID, innerForm.getFirstField().getFieldId());
    assertEquals(SECOND_FIELD_ID, innerForm.getSecondField().getFieldId());
    InnerInnerForm innerInnerForm = innerForm.getWrappedFormField().getInnerForm();
    assertNotNull(innerInnerForm);
    assertEquals(FIRST_FIELD_ID, innerInnerForm.getFirstField().getFieldId());
    assertEquals(SECOND_FIELD_ID, innerInnerForm.getSecondField().getFieldId());
    assertEquals(THIRD_FIELD_ID, innerInnerForm.getThirdField().getFieldId());
  }

  /* --------------------------------------------------------------------------
   * first field
   * --------------------------------------------------------------------------
   */
  @Test
  public void testGetFirstFieldWithoutForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(FIRST_FIELD_DATA_ID);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getFirstField(), field);
  }

  @Test
  public void testGetFirstFieldOnMainForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(FIRST_FIELD_DATA_ID, m_mainForm);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getFirstField(), field);
  }

  @Test
  public void testGetFirstFieldOnInnerForm() throws Exception {
    InnerForm innerForm = m_mainForm.getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(FIRST_FIELD_DATA_ID, innerForm);
    innerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerForm.getFirstField(), field);
  }

  @Test
  public void testGetFirstFieldOnInnerInnerForm() throws Exception {
    InnerInnerForm innerInnerForm = m_mainForm.getWrappedFormField().getInnerForm().getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(FIRST_FIELD_DATA_ID, innerInnerForm);
    innerInnerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerInnerForm.getFirstField(), field);
  }

  /* --------------------------------------------------------------------------
   * second field
   * --------------------------------------------------------------------------
   */
  @Test
  public void testGetSecondFieldWithoutForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(SECOND_FIELD_DATA_ID);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getWrappedFormField().getInnerForm().getSecondField(), field);
  }

  @Test
  public void testGetSecondFieldOnMainForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(SECOND_FIELD_DATA_ID, m_mainForm);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getWrappedFormField().getInnerForm().getSecondField(), field);
  }

  @Test
  public void testGetSecondFieldOnInnerForm() throws Exception {
    InnerForm innerForm = m_mainForm.getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(SECOND_FIELD_DATA_ID, innerForm);
    innerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerForm.getSecondField(), field);
  }

  @Test
  public void testGetSecondFieldOnInnerInnerForm() throws Exception {
    InnerInnerForm innerInnerForm = m_mainForm.getWrappedFormField().getInnerForm().getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(SECOND_FIELD_DATA_ID, innerInnerForm);
    innerInnerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerInnerForm.getSecondField(), field);
  }

  /* --------------------------------------------------------------------------
   * third field
   * --------------------------------------------------------------------------
   */
  @Test
  public void testGetThirdFieldWithoutForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(THIRD_FIELD_DATA_ID);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getWrappedFormField().getInnerForm().getWrappedFormField().getInnerForm().getThirdField(), field);
  }

  @Test
  public void testGetThirdFieldOnMainForm() throws Exception {
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(THIRD_FIELD_DATA_ID, m_mainForm);
    m_mainForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(m_mainForm.getWrappedFormField().getInnerForm().getWrappedFormField().getInnerForm().getThirdField(), field);
  }

  @Test
  public void testGetThirdFieldOnInnerForm() throws Exception {
    InnerForm innerForm = m_mainForm.getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(THIRD_FIELD_DATA_ID, innerForm);
    innerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerForm.getWrappedFormField().getInnerForm().getThirdField(), field);
  }

  @Test
  public void testGetThirdFieldOnInnerInnerForm() throws Exception {
    InnerInnerForm innerInnerForm = m_mainForm.getWrappedFormField().getInnerForm().getWrappedFormField().getInnerForm();
    FindFieldByFormDataIdVisitor visitor = new FindFieldByFormDataIdVisitor(THIRD_FIELD_DATA_ID, innerInnerForm);
    innerInnerForm.visitFields(visitor);
    IFormField field = visitor.getField();
    assertNotNull(field);
    assertSame(innerInnerForm.getThirdField(), field);
  }

  /* --------------------------------------------------------------------------
   * fixture
   * --------------------------------------------------------------------------
   */
  public static class MainForm extends AbstractForm {

    public MainForm() {
      super();
    }

    public MainForm.MainBox getMainBox() {
      return (MainForm.MainBox) getRootGroupBox();
    }

    public MainForm.MainBox.WrappedFormField getWrappedFormField() {
      return getFieldByClass(MainForm.MainBox.WrappedFormField.class);
    }

    public MainForm.MainBox.FirstField getFirstField() {
      return getFieldByClass(MainForm.MainBox.FirstField.class);
    }

    @Override
    public void start() {
      startInternal(new MainFormHandler());
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class WrappedFormField extends AbstractWrappedFormField<InnerForm> {

        @Override
        protected void execInitField() {
          setInnerForm(new InnerForm());
        }
      }

      @Order(20)
      public class FirstField extends AbstractStringField {
      }
    }

    public class MainFormHandler extends AbstractFormHandler {
    }
  }

  public static class InnerForm extends AbstractForm {

    public InnerForm() {
      super();
    }

    public InnerForm.MainBox getMainBox() {
      return (InnerForm.MainBox) getRootGroupBox();
    }

    public InnerForm.MainBox.WrappedFormField getWrappedFormField() {
      return getFieldByClass(InnerForm.MainBox.WrappedFormField.class);
    }

    public InnerForm.MainBox.FirstField getFirstField() {
      return getFieldByClass(InnerForm.MainBox.FirstField.class);
    }

    public InnerForm.MainBox.SecondField getSecondField() {
      return getFieldByClass(InnerForm.MainBox.SecondField.class);
    }

    @Override
    public void start() {
      startInternal(new InnerFormHandler());
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class WrappedFormField extends AbstractWrappedFormField<InnerInnerForm> {
        @Override
        protected void execInitField() {
          setInnerForm(new InnerInnerForm());
        }
      }

      @Order(20)
      public class FirstField extends AbstractStringField {
      }

      @Order(30)
      public class SecondField extends AbstractStringField {
      }
    }

    public class InnerFormHandler extends AbstractFormHandler {
    }
  }

  public static class InnerInnerForm extends AbstractForm {

    public InnerInnerForm() {
      super();
    }

    public InnerInnerForm.MainBox getMainBox() {
      return (InnerInnerForm.MainBox) getRootGroupBox();
    }

    public InnerInnerForm.MainBox.FirstField getFirstField() {
      return getFieldByClass(InnerInnerForm.MainBox.FirstField.class);
    }

    public InnerInnerForm.MainBox.SecondField getSecondField() {
      return getFieldByClass(InnerInnerForm.MainBox.SecondField.class);
    }

    public InnerInnerForm.MainBox.ThirdField getThirdField() {
      return getFieldByClass(InnerInnerForm.MainBox.ThirdField.class);
    }

    @Override
    public void start() {
      startInternal(new InnerInnerFormHandler());
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(20)
      public class FirstField extends AbstractStringField {
      }

      @Order(30)
      public class SecondField extends AbstractStringField {
      }

      @Order(40)
      public class ThirdField extends AbstractStringField {
      }
    }

    public class InnerInnerFormHandler extends AbstractFormHandler {
    }
  }
}
