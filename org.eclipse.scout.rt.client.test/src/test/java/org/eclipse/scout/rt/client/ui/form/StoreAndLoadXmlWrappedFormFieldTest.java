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

import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.TextField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.Variant1Field;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.Variant2Field;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This test contains a form with 1 StringField and 2 WrappedFormFields. Both WrappedFormFields have the same (!)
 * innerForm loaded. The innerForm also contains a StringField. Afterwards the form's state is persisted to xml. The
 * form's values are cleared and the state is read from xml again.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlWrappedFormFieldTest {

  private static final String TOP_LEVE_TEXT_FIELD_VALUE = "sample text";
  private static final String INNER_FORM1_TEXT_FIELD_VALUE = "Variant1.innerTextField";
  private static final String INNER_FORM1_TEXT_PROPERTY_VALUE = "Variant1.innerTextProperty";
  private static final String INNER_FORM2_TEXT_FIELD_VALUE = "Variant2.innerTextField";
  private static final String INNER_FORM2_TEXT_PROPERTY_VALUE = "Variant2.innerTextProperty";

  @Test
  public void testStoreAndLoad() {
    TestForm testForm = new TestForm();

    // set values
    testForm.getTextField().setValue(TOP_LEVE_TEXT_FIELD_VALUE);
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(INNER_FORM1_TEXT_FIELD_VALUE);
    testForm.getVariant2Field().getInnerForm().getInnerTextField().setValue(INNER_FORM2_TEXT_FIELD_VALUE);
    testForm.getVariant1Field().getInnerForm().setText(INNER_FORM1_TEXT_PROPERTY_VALUE);
    testForm.getVariant2Field().getInnerForm().setText(INNER_FORM2_TEXT_PROPERTY_VALUE);
    assertFormValues(testForm);

    // get xml of form state
    String xml = testForm.storeToXmlString();
    assertXmlDocument(xml);

    // clear all values
    testForm.getTextField().setValue(null);
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(null);
    testForm.getVariant1Field().getInnerForm().setText(null);
    testForm.getVariant2Field().getInnerForm().getInnerTextField().setValue(null);
    testForm.getVariant2Field().getInnerForm().setText(null);

    // set xml to form again
    assertTrue(testForm.loadFromXmlString(xml));
    assertFormValues(testForm);
  }

  @Test
  public void testLoadUnknownInnerFormWithoutValue() {
    TestForm testForm = new TestForm();
    String xml = testForm.storeToXmlString();

    // should not be successful
    // only best-effort. we do not check inner forms recursively for values
    TestFormWithoutInnerForm target = new TestFormWithoutInnerForm();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
  }

  @Test
  public void testLoadUnknownInnerFormWithFieldValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(INNER_FORM1_TEXT_FIELD_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithoutInnerForm target = new TestFormWithoutInnerForm();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
  }

  @Test
  public void testLoadUnknownInnerFormWithPropertyValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().setText(INNER_FORM1_TEXT_PROPERTY_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithoutInnerForm target = new TestFormWithoutInnerForm();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithUnknownFieldWithValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(INNER_FORM1_TEXT_FIELD_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithInnerFormWithoutField target = new TestFormWithInnerFormWithoutField();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithUnknownFieldWithoutValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(null);
    String xml = testForm.storeToXmlString();

    // should be successful because no value is set
    TestFormWithInnerFormWithoutField target = new TestFormWithInnerFormWithoutField();
    assertTrue(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithInvalidField() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(INNER_FORM1_TEXT_FIELD_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithInnerFormWithInvalidField target = new TestFormWithInnerFormWithInvalidField();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getVariant1Field().getInnerForm().getInnerTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithUnknownPropertyWithValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().setText(INNER_FORM1_TEXT_PROPERTY_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithInnerFormWithoutProperty target = new TestFormWithInnerFormWithoutProperty();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
    assertNull(target.getVariant1Field().getInnerForm().getInnerTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithUnknownPropertyWithoutValue() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().setText(null);
    String xml = testForm.storeToXmlString();

    // should be successful because no value is set
    TestFormWithInnerFormWithoutProperty target = new TestFormWithInnerFormWithoutProperty();
    assertTrue(target.loadFromXmlString(xml));
    assertNull(target.getTextField().getValue());
    assertNull(target.getVariant1Field().getInnerForm().getInnerTextField().getValue());
  }

  @Test
  public void testLoadInnerFormWithInvalidProperty() {
    TestForm testForm = new TestForm();
    testForm.getVariant1Field().getInnerForm().setText(INNER_FORM1_TEXT_PROPERTY_VALUE);
    String xml = testForm.storeToXmlString();

    // should not be successful
    TestFormWithInnerFormWithInvalidProperty target = new TestFormWithInnerFormWithInvalidProperty();
    assertFalse(target.loadFromXmlString(xml));
    assertEquals(0L, target.getVariant1Field().getInnerForm().getText());
  }

  private void assertFormValues(TestForm testForm) {
    assertEquals(TOP_LEVE_TEXT_FIELD_VALUE, testForm.getTextField().getValue());
    assertEquals(INNER_FORM1_TEXT_FIELD_VALUE, testForm.getVariant1Field().getInnerForm().getInnerTextField().getValue());
    assertEquals(INNER_FORM1_TEXT_PROPERTY_VALUE, testForm.getVariant1Field().getInnerForm().getText());
    assertEquals(INNER_FORM2_TEXT_FIELD_VALUE, testForm.getVariant2Field().getInnerForm().getInnerTextField().getValue());
    assertEquals(INNER_FORM2_TEXT_PROPERTY_VALUE, testForm.getVariant2Field().getInnerForm().getText());
  }

  private void assertXmlDocument(String xml) {
    assertNotNull(xml);

    Document document = XmlUtility.getXmlDocument(xml);
    Element root = document.getDocumentElement();
    assertEquals("form-state", root.getNodeName());
    assertForm(TestForm.class, root);
    assertEmptyProperties(root);

    // fields
    Element xFields = XmlUtility.getFirstChildElement(root, "fields");
    assertNotNull(xFields);
    List<Element> fields = XmlUtility.getChildElements(xFields);
    assertEquals(5, fields.size());
    assertField(TestForm.MainBox.class, fields.get(0));
    assertField(GroupBox.class, fields.get(1));
    assertField(TextField.class, fields.get(2));
    assertField(Variant1Field.class, fields.get(3));
    assertField(Variant2Field.class, fields.get(4));

    // variant fields
    assertWrappedForm(fields.get(3));
    assertWrappedForm(fields.get(4));
  }

  private void assertWrappedForm(Element element) {
    assertForm(InnerTestForm.class, element);

    // properties
    Element xProps = XmlUtility.getFirstChildElement(element, "properties");
    assertNotNull(xProps);
    List<Element> props = XmlUtility.getChildElements(xProps);
    assertEquals(1, props.size());
    assertEquals("text", props.get(0).getAttribute("name"));

    // fields
    Element xFlds = XmlUtility.getFirstChildElement(element, "fields");
    assertNotNull(xFlds);
    List<Element> flds = XmlUtility.getChildElements(xFlds);
    assertEquals(2, flds.size());
    assertField(InnerTestForm.MainBox.class, flds.get(0));
    assertField(InnerTestForm.MainBox.InnerTextField.class, flds.get(1));
  }

  private void assertEmptyProperties(Element element) {
    Element props = XmlUtility.getFirstChildElement(element, "properties");
    assertNotNull(props);
    assertEquals(0, props.getChildNodes().getLength());
  }

  private void assertForm(Class<? extends IForm> formClass, Element e) {
    assertNotNull(e);
    assertEquals(formClass.getSimpleName(), e.getAttribute("formId"));
    assertEquals(formClass.getName(), e.getAttribute("formQname"));
    assertEquals(2, XmlUtility.getChildElements(e).size());
  }

  private void assertField(Class<? extends IFormField> fieldClass, Element e) {
    assertNotNull(e);
    assertEquals(fieldClass.getSimpleName(), e.getAttribute("fieldId"));
    assertEquals(fieldClass.getName(), e.getAttribute("fieldQname"));
  }

  public static final class TestForm extends AbstractForm {

    public TextField getTextField() {
      return getFieldByClass(TextField.class);
    }

    public Variant1Field getVariant1Field() {
      return getFieldByClass(Variant1Field.class);
    }

    public Variant2Field getVariant2Field() {
      return getFieldByClass(Variant2Field.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }

        @Order(20.0)
        public class Variant1Field extends AbstractWrappedFormField<InnerTestForm> {
          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestForm.class;
          }
        }

        @Order(30.0)
        public class Variant2Field extends AbstractWrappedFormField<InnerTestForm> {

          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestForm.class;
          }
        }
      }
    }
  }

  public static final class InnerTestForm extends AbstractForm {

    private String m_text;

    public MainBox.InnerTextField getInnerTextField() {
      return getFieldByClass(MainBox.InnerTextField.class);
    }

    @FormData
    public String getText() {
      return m_text;
    }

    @FormData
    public void setText(String text) {
      m_text = text;
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerTextField extends AbstractStringField {
      }
    }
  }

  public static final class TestFormWithoutInnerForm extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }
      }
    }
  }

  public static final class TestFormWithInnerFormWithoutProperty extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    public MainBox.GroupBox.Variant1Field getVariant1Field() {
      return getFieldByClass(MainBox.GroupBox.Variant1Field.class);
    }

    public MainBox.GroupBox.Variant2Field getVariant2Field() {
      return getFieldByClass(MainBox.GroupBox.Variant2Field.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }

        @Order(20.0)
        public class Variant1Field extends AbstractWrappedFormField<InnerTestFormWithoutProperty> {
          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithoutProperty.class;
          }
        }

        @Order(30.0)
        public class Variant2Field extends AbstractWrappedFormField<InnerTestFormWithoutProperty> {

          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithoutProperty.class;
          }
        }
      }
    }
  }

  public static final class TestFormWithInnerFormWithoutField extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    public MainBox.GroupBox.Variant1Field getVariant1Field() {
      return getFieldByClass(MainBox.GroupBox.Variant1Field.class);
    }

    public MainBox.GroupBox.Variant2Field getVariant2Field() {
      return getFieldByClass(MainBox.GroupBox.Variant2Field.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }

        @Order(20.0)
        public class Variant1Field extends AbstractWrappedFormField<InnerTestFormWithoutField> {
          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithoutField.class;
          }
        }

        @Order(30.0)
        public class Variant2Field extends AbstractWrappedFormField<InnerTestFormWithoutField> {

          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithoutField.class;
          }
        }
      }
    }
  }

  public static final class InnerTestFormWithoutProperty extends AbstractForm {

    public MainBox.InnerTextField getInnerTextField() {
      return getFieldByClass(MainBox.InnerTextField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerTextField extends AbstractStringField {
      }
    }
  }

  public static final class InnerTestFormWithoutField extends AbstractForm {

    private String m_text;

    @FormData
    public String getText() {
      return m_text;
    }

    @FormData
    public void setText(String text) {
      m_text = text;
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {
    }
  }

  public static final class InnerTestFormWithInvalidProperty extends AbstractForm {

    private long m_text; // property has invalid type

    public MainBox.InnerTextField getInnerTextField() {
      return getFieldByClass(MainBox.InnerTextField.class);
    }

    @FormData
    public long getText() {
      return m_text;
    }

    @FormData
    public void setText(long text) {
      m_text = text;
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerTextField extends AbstractStringField {
      }
    }
  }

  public static final class TestFormWithInnerFormWithInvalidProperty extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    public MainBox.GroupBox.Variant1Field getVariant1Field() {
      return getFieldByClass(MainBox.GroupBox.Variant1Field.class);
    }

    public MainBox.GroupBox.Variant2Field getVariant2Field() {
      return getFieldByClass(MainBox.GroupBox.Variant2Field.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }

        @Order(20.0)
        public class Variant1Field extends AbstractWrappedFormField<InnerTestFormWithInvalidProperty> {
          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithInvalidProperty.class;
          }
        }

        @Order(30.0)
        public class Variant2Field extends AbstractWrappedFormField<InnerTestFormWithInvalidProperty> {

          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithInvalidProperty.class;
          }
        }
      }
    }
  }

  public static final class TestFormWithInnerFormWithInvalidField extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    public MainBox.GroupBox.Variant1Field getVariant1Field() {
      return getFieldByClass(MainBox.GroupBox.Variant1Field.class);
    }

    public MainBox.GroupBox.Variant2Field getVariant2Field() {
      return getFieldByClass(MainBox.GroupBox.Variant2Field.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(50.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }

        @Order(20.0)
        public class Variant1Field extends AbstractWrappedFormField<InnerTestFormWithInvalidField> {
          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithInvalidField.class;
          }
        }

        @Order(30.0)
        public class Variant2Field extends AbstractWrappedFormField<InnerTestFormWithInvalidField> {

          @Override
          protected Class<? extends IForm> getConfiguredInnerForm() {
            return InnerTestFormWithInvalidField.class;
          }
        }
      }
    }
  }

  public static final class InnerTestFormWithInvalidField extends AbstractForm {

    private String m_text;

    public MainBox.InnerTextField getInnerTextField() {
      return getFieldByClass(MainBox.InnerTextField.class);
    }

    @FormData
    public String getText() {
      return m_text;
    }

    @FormData
    public void setText(String text) {
      m_text = text;
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerTextField extends AbstractIntegerField {
      }
    }
  }
}
