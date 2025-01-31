/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.extension.ui.NotificationBadgeStatus;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractFormTest.WrapperTestFormWithClassId.MainBox.EmbeddedField;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.StringUtility;
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
   * Tests the html veto exception error message creation
   */
  @Test
  public void testVetoExceptionHtmlMessage() {
    TestFormWithMandatoryField form = new TestFormWithMandatoryField();
    String htmlErrorMessage = "";

    String expectedErrorMessage = HTML.fragment(
            HTML.div(TEXTS.get("FormEmptyMandatoryFieldsMessage")),
            HTML.ul(HTML.li(form.getStringField().getFullyQualifiedLabel(IValidateContentDescriptor.LABEL_SEPARATOR))))
        .toHtml();

    try {
      form.validateForm();
    }
    catch (VetoException ve) {
      htmlErrorMessage = ve.getHtmlMessage().toHtml();
    }

    assertEquals(expectedErrorMessage, htmlErrorMessage);
  }

  /**
   * Tests the html veto exception error message creation of a field with a html label
   */
  @Test
  public void testVetoExceptionHtmlMessageWithHtmlLabel() {
    TestFormWithMandatoryField form = new TestFormWithMandatoryField();
    form.getStringField().setLabelHtmlEnabled(true);
    form.getStringField().setLabel(HTML.italic("String") + " " + HTML.bold("Field"));
    String htmlErrorMessage = "";

    String expectedErrorMessage = HTML.fragment(
            HTML.div(TEXTS.get("FormEmptyMandatoryFieldsMessage")),
            HTML.ul(HTML.li(form.getMainBox().getFullyQualifiedLabel(IValidateContentDescriptor.LABEL_SEPARATOR) + IValidateContentDescriptor.LABEL_SEPARATOR + "String Field")))
        .toHtml();

    try {
      form.validateForm();
    }
    catch (VetoException ve) {
      htmlErrorMessage = ve.getHtmlMessage().toHtml();
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
  public void testGetSetTitle() {
    final boolean[] called = {false};
    IForm form = new TestFormWithClassId();
    PropertyChangeListener l = evt -> {
      if ("title".equals(evt.getPropertyName()) && "foo".equals(evt.getNewValue())) {
        called[0] = true;
      }
    };
    form.addPropertyChangeListener(l);
    form.setTitle("foo");
    assertEquals("foo", form.getTitle());
    assertTrue(called[0]);
  }

  @Test
  public void testGetSetSubTitle() {
    final boolean[] called = {false};
    IForm form = new TestFormWithClassId();
    PropertyChangeListener l = evt -> {
      if ("subTitle".equals(evt.getPropertyName()) && "bar".equals(evt.getNewValue())) {
        called[0] = true;
      }
    };
    form.addPropertyChangeListener(l);
    form.setSubTitle("bar");
    assertEquals("bar", form.getSubTitle());
    assertTrue(called[0]);
  }

  @Test
  public void testCloseInInitForm() {
    // call doClose in execInitForm
    AbstractForm form = new TestForm(true);
    assertTrue(form.isFormStartable());
    form.start();
    assertTrue(form.isFormStartable());
    assertFalse(form.isFormStarted());
    assertFalse(form.isBlockingInternal());

    // regular form start
    form = new TestForm(false);
    assertTrue(form.isFormStartable());
    form.start();
    assertFalse(form.isFormStartable());
    assertTrue(form.isFormStarted());
    assertTrue(form.isBlockingInternal());
  }

  @Test
  public void testStartedStateDuringInitialisation() {
    final Boolean[] resultStartable = {null};
    final Boolean[] resultStarted = {null};
    final Boolean[] resultBlocking = {null};
    final AbstractForm form = new TestForm(false);
    form.addFormListener(e -> {
      if (e.getType() == FormEvent.TYPE_LOAD_AFTER) {
        resultStartable[0] = Boolean.valueOf(form.isFormStartable());
        resultStarted[0] = Boolean.valueOf(form.isFormStarted());
        resultBlocking[0] = Boolean.valueOf(form.isBlockingInternal());
      }
    });
    form.start();
    assertFalse(resultStartable[0]);
    assertFalse(resultStarted[0]);
    assertTrue(resultBlocking[0]);
  }

  @Test
  public void testFormEventResetComplete() {
    final Boolean[] called = {false};
    final AbstractForm form = new TestForm(false);
    form.addFormListener(e -> {
      if (e.getType() == FormEvent.TYPE_RESET_COMPLETE) {
        called[0] = true;
      }
    });
    form.start();
    assertFalse(called[0]);
    form.doReset();
    assertTrue(called[0]);
  }

  @Test
  public void testNotificationBadgeText() {
    final AbstractForm form = new TestForm(false);
    assertNull(form.getNotificationBadgeText());

    form.setNotificationBadgeText("foo");
    assertEquals("foo", form.getNotificationBadgeText());
    form.setNotificationBadgeText("bar");
    assertEquals("bar", form.getNotificationBadgeText());
    form.setNotificationBadgeText(null);
    assertNull(form.getNotificationBadgeText());

    form.setNotificationBadgeText("foo");
    assertEquals("foo", form.getNotificationBadgeText());
    form.addStatus(new NotificationBadgeStatus("bar"));
    assertEquals("foo", form.getNotificationBadgeText());
    form.setNotificationBadgeText(null);
    assertNull(form.getNotificationBadgeText());

    form.addStatus(new NotificationBadgeStatus("bar"));
    assertNull(form.getNotificationBadgeText());
    form.setNotificationBadgeText("foo");
    assertEquals("foo", form.getNotificationBadgeText());
    form.setNotificationBadgeText(null);
    assertNull(form.getNotificationBadgeText());
  }
}
