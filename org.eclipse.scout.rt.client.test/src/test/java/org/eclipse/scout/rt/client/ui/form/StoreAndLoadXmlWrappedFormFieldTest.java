/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.InnerTestForm.MainBox.InnerTextField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.TextField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.Variant1Field;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWrappedFormFieldTest.TestForm.MainBox.GroupBox.Variant2Field;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
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

  private static final String STRING1 = "sample text";
  private static final String STRING2 = "Variant1.innerTextField";
  private static final String STRING3 = "Variant2.innerTextField";

  @Test
  public void testStoreAndLoad() {
    TestForm testForm = new TestForm();

    // set values
    testForm.getTextField().setValue(STRING1);
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(STRING2);
    testForm.getVariant2Field().getInnerForm().getInnerTextField().setValue(STRING3);
    assertFormValues(testForm);

    // get xml of form state
    String xml = testForm.storeToXmlString();
    assertXmlDocument(xml);

    // clear all values
    testForm.getTextField().setValue(null);
    testForm.getVariant1Field().getInnerForm().getInnerTextField().setValue(null);
    testForm.getVariant2Field().getInnerForm().getInnerTextField().setValue(null);

    // set xml to form again
    testForm.loadFromXmlString(xml);
    assertFormValues(testForm);
  }

  private void assertFormValues(TestForm testForm) {
    assertEquals(STRING1, testForm.getTextField().getValue());
    assertEquals(STRING2, testForm.getVariant1Field().getInnerForm().getInnerTextField().getValue());
    assertEquals(STRING3, testForm.getVariant2Field().getInnerForm().getInnerTextField().getValue());
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
    assertEmptyProperties(element);

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

  public final static class InnerTestForm extends AbstractForm {

    public InnerTextField getInnerTextField() {
      return getFieldByClass(InnerTextField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerTextField extends AbstractStringField {
      }
    }
  }
}
