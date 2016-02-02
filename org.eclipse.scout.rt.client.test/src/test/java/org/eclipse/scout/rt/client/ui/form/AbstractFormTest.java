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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractFormTest.WrapperTestFormWithClassId.MainBox.EmbeddedField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link AbstractForm}
 *
 * @since 3.10.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractFormTest {
  private static final String FORM_TEST_CLASS_ID = "FORM_TEST_CLASS_ID";
  private static final String WRAPPER_FORM_TEST_CLASS_ID = "WRAPPER_FORM_TEST_CLASS_ID";
  private static final String WRAPPER_FORM_FIELD_ID = "WRAPPER_FORM_FIELD_ID";

  /**
   * {@link AbstractForm#classId()}
   */
  @Test
  public void testClassIdAnnotated() {
    TestFormWithClassId form = new TestFormWithClassId();
    assertEquals(FORM_TEST_CLASS_ID, form.classId());
    testClassIdSetter(form, FORM_TEST_CLASS_ID);
  }

  /**
   * {@link AbstractForm#classId()}
   */
  @Test
  public void testClassIdNoAnnotation() {
    TestForm form = new TestForm();
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
  public void testWrappedForm() {
    WrapperTestFormWithClassId form = new WrapperTestFormWithClassId();
    form.getEmbeddedField().setInnerForm(new TestFormWithClassId());
    String classId = form.getEmbeddedField().getInnerForm().classId();
    assertTrue("ClassId of innerform should contain outerFormField id.", classId.contains(WRAPPER_FORM_FIELD_ID));
    assertTrue("ClassId of innerform should contain formid.", classId.contains(FORM_TEST_CLASS_ID));
  }

  /**
   * Tests that validating a valid form should not result in any error.
   */
  @Test
  public void testValidForm() {
    TestForm form = new TestForm();
    form.validateForm();
  }

  /**
   * Tests that validating a valid form with an ok status should not result in any error.
   */
  @Test
  public void testValidForm_OkStatus() {
    TestForm form = new TestForm();
    form.getMainBox().addErrorStatus(Status.OK_STATUS);
    form.validateForm();
  }

  /**
   * Tests that validating a valid form with an ok status should not result in any error.
   */
  public void testValidForm_ErrorStatus() {
    String errorMessage = "";
    try {
      TestForm form = new TestForm();
      form.getMainBox().addErrorStatus(new Status("ErrorMessage"));
      form.validateForm();
    }
    catch (VetoException ve) {
      errorMessage = ve.getDisplayMessage();
    }

    assertTrue(errorMessage.contains("MainBox: ErrorMessage"));

  }

  /**
   * Tests the html veto exception error message creation
   */
  @Test
  public void testVetoExceptionHtmlMessage() {
    TestFormWithMandatoryField form = new TestFormWithMandatoryField();
    String htmlErrorMessage = "";

    String expectedErrorMessage = HTML.fragment(
        HTML.bold(ScoutTexts.get("FormEmptyMandatoryFieldsMessage")),
        HTML.ul(HTML.li(form.getStringField().getFullyQualifiedLabel(": "))),
        HTML.br()).toEncodedHtml();

    try {
      form.validateForm();
    }
    catch (VetoException ve) {
      htmlErrorMessage = ve.getHtmlMessage().toEncodedHtml();
    }

    assertEquals(expectedErrorMessage, htmlErrorMessage);

  }

  // Test classes

  @ClassId(FORM_TEST_CLASS_ID)
  class TestFormWithClassId extends AbstractForm {

    public TestFormWithClassId() {
      super();
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
    }
  }

  class TestForm extends AbstractForm {

    private boolean m_closeInInit;

    public TestForm() {
      this(false);
    }

    public TestForm(boolean closeInInit) {
      m_closeInInit = closeInInit;
    }

    @Override
    protected void execInitForm() {
      if (m_closeInInit) {
        doClose();
      }
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected String getConfiguredLabel() {
        return "Main Box";
      }

    }
  }

  class TestFormWithMandatoryField extends TestForm {

    public TestFormWithMandatoryField() {
      super();
    }

    public StringField getStringField() {
      return getFieldByClass(StringField.class);
    }

    @Order(10)
    @InjectFieldTo(TestForm.MainBox.class)
    public class StringField extends AbstractStringField {

      @Override
      protected String getConfiguredLabel() {
        return "String Field";
      }

      @Override
      protected boolean getConfiguredMandatory() {
        return true;
      }
    }
  }

  @ClassId(WRAPPER_FORM_TEST_CLASS_ID)
  class WrapperTestFormWithClassId extends AbstractForm {

    public EmbeddedField getEmbeddedField() {
      return getFieldByClass(EmbeddedField.class);
    }

    public WrapperTestFormWithClassId() {
      super();
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Override
      protected void execInitField() {
        getFieldByClass(EmbeddedField.class).setInnerForm(new TestFormWithClassId());
      }

      @Order(10)
      @ClassId(WRAPPER_FORM_FIELD_ID)
      public class EmbeddedField extends AbstractWrappedFormField<TestFormWithClassId> {
      }
    }
  }

  @Test
  public void testGetSetTitle() throws Exception {
    final boolean[] called = {false};
    IForm form = new TestFormWithClassId();
    PropertyChangeListener l = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("title".equals(evt.getPropertyName()) && "foo".equals(evt.getNewValue())) {
          called[0] = true;
        }
      }
    };
    form.addPropertyChangeListener(l);
    form.setTitle("foo");
    assertEquals("foo", form.getTitle());
    assertTrue(called[0]);
  }

  @Test
  public void testGetSetSubTitle() throws Exception {
    final boolean[] called = {false};
    IForm form = new TestFormWithClassId();
    PropertyChangeListener l = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("subTitle".equals(evt.getPropertyName()) && "bar".equals(evt.getNewValue())) {
          called[0] = true;
        }
      }
    };
    form.addPropertyChangeListener(l);
    form.setSubTitle("bar");
    assertEquals("bar", form.getSubTitle());
    assertTrue(called[0]);
  }

  @Test
  public void testCloseInInitForm() throws Exception {
    // call doClose in execInitForm
    AbstractForm form = new TestForm(true);
    form.start();
    assertFalse(form.isFormStarted());
    assertFalse(form.isBlockingInternal());

    // regular form start
    form = new TestForm(false);
    form.start();
    assertTrue(form.isFormStarted());
    assertTrue(form.isBlockingInternal());
  }

}
