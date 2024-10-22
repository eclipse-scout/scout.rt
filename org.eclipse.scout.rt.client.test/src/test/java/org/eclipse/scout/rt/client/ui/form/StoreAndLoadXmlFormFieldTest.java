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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTestGroupBox.InnerTestGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G1Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G2Box;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlFormFieldTest {

  @Test
  public void testXmlFieldIds() {
    TestForm f = new TestForm();
    assertEquals("MainBox", f.getMainBox().getFieldId());
    assertEquals("customId", f.getText4Field().getFieldId());
    assertEquals("Text1Field", f.getText1Field().getFieldId());
  }

  @Test
  public void testFieldIds() {
    TestForm f = new TestForm();
    checkFieldXml(f.getMainBox(), "MainBox");
    checkFieldXml(f.getText4Field(), "customId");
    checkFieldXml(f.getText1Field(), "Text1Field");

    checkFieldXml(f.getG1Box(), "G1Box");
    checkFieldXml(f.getG2Box(), "G2Box");
    checkFieldXml(f.getG3Box(), "G3Box");
    checkFieldXml(f.getG4Box(), "G4Box");

    checkFieldXml(f.getText3Field(), "Text3Field", new EnclosingField("G2Box", G2Box.class.getName()));

    checkFieldXml(f.getG1Box().getText1Field(), "Text1Field", new EnclosingField("G1Box", G1Box.class.getName()));
    checkFieldXml(f.getG2Box().getText1Field(), "Text1Field", new EnclosingField("G2Box", G2Box.class.getName()));

    checkFieldXml(f.getG1Box().getText2Field(), "Text2Field", new EnclosingField("G1Box", G1Box.class.getName()));
    checkFieldXml(f.getG2Box().getText2Field(), "Text2Field", new EnclosingField("G2Box", G2Box.class.getName()));

    checkFieldXml(f.getG1Box().getTestListBox(), "TestListBox", new EnclosingField("G1Box", G1Box.class.getName()));
    checkFieldXml(f.getG2Box().getTestListBox(), "TestListBox", new EnclosingField("G2Box", G2Box.class.getName()));

    checkFieldXml(f.getG1Box().getInnerTestGroupBox(), "InnerTestGroupBox", new EnclosingField("G1Box", G1Box.class.getName()));
    checkFieldXml(f.getG2Box().getInnerTestGroupBox(), "InnerTestGroupBox", new EnclosingField("G2Box", G2Box.class.getName()));

    checkFieldXml(f.getG1Box().getInnerTestGroupBox().getInnerText1Field(), "InnerText1Field", new EnclosingField("G1Box", G1Box.class.getName()), new EnclosingField("InnerTestGroupBox", InnerTestGroupBox.class.getName()));
    checkFieldXml(f.getG2Box().getInnerTestGroupBox().getInnerText1Field(), "InnerText1Field", new EnclosingField("G2Box", G2Box.class.getName()), new EnclosingField("InnerTestGroupBox", InnerTestGroupBox.class.getName()));
  }

  @Test
  public void testFormId() {
    TestForm f = new TestForm();
    Document xml = f.storeToXml();
    assertEquals("TestForm", xml.getDocumentElement().getAttribute("formId"));
    assertEquals(TestForm.class.getName(), xml.getDocumentElement().getAttribute("formQname"));
  }

  @Test
  public void testStoreLoad() {
    //store some values
    TestForm f = new TestForm();
    f.getText1Field().setValue("t1");
    f.getText3Field().setValue("t3");
    f.getText4Field().setValue("t4");
    f.getG1Box().getText1Field().setValue("g1t1");
    f.getG1Box().getText2Field().setValue("g1t2");
    f.getG2Box().getText1Field().setValue("g2t1");
    f.getG2Box().getText2Field().setValue("g2t2");
    f.getG3G4Text2Field().setValue("g3g2");
    f.getG1Box().getTestListBox().setValue(CollectionUtility.hashSet("g1L"));
    f.getG2Box().getTestListBox().setValue(CollectionUtility.hashSet("g2L"));
    String xml = f.storeToXmlString();

    f = new TestForm();
    assertTrue(f.loadFromXmlString(xml));

    //new form should contain the stored values
    assertEquals("t1", f.getText1Field().getValue());
    assertEquals("t3", f.getText3Field().getValue());
    assertEquals("t4", f.getText4Field().getValue());
    assertEquals("g1t1", f.getG1Box().getText1Field().getValue());
    assertEquals("g1t2", f.getG1Box().getText2Field().getValue());
    assertEquals("g2t1", f.getG2Box().getText1Field().getValue());
    assertEquals("g3g2", f.getG3G4Text2Field().getValue());
    assertEquals("g1L", CollectionUtility.firstElement(f.getG1Box().getTestListBox().getValue()));
    assertEquals("g2L", CollectionUtility.firstElement(f.getG2Box().getTestListBox().getValue()));
  }

  @Test
  public void testLoadUnknownFieldWithValue() {
    MissingFieldSourceTestForm source = new MissingFieldSourceTestForm();
    source.getText1Field().setValue("t1");
    source.getMyListBox().setValue(CollectionUtility.hashSet("test"));
    String xml = source.storeToXmlString();

    MissingFieldTargetTestForm target = new MissingFieldTargetTestForm();
    assertFalse(target.loadFromXmlString(xml));
    assertEquals("t1", target.getText1Field().getValue());
  }

  @Test
  public void testLoadUnknownFieldWithoutValue() {
    MissingFieldSourceTestForm source = new MissingFieldSourceTestForm();
    source.getText1Field().setValue("t1");
    source.getMyListBox().setValue(CollectionUtility.emptyHashSet());
    String xml = source.storeToXmlString();

    MissingFieldTargetTestForm target = new MissingFieldTargetTestForm();
    assertTrue(target.loadFromXmlString(xml));
    assertEquals("t1", target.getText1Field().getValue());
  }

  @Test
  public void testLoadInvalidField() {
    InvalidFieldSourceTestForm source = new InvalidFieldSourceTestForm();
    source.getText1Field().setValue("t1");
    String xml = source.storeToXmlString();

    InvalidFieldTargetTestForm target = new InvalidFieldTargetTestForm();
    assertFalse(target.loadFromXmlString(xml));
    assertNull(target.getText1Field().getValue());
  }

  @Test
  public void testLegacyLoad() {
    TestForm f = new TestForm();
    f.getText1Field().setValue("t1");
    f.getText3Field().setValue("t3");
    f.getG1Box().getText1Field().setValue("g1t1");
    f.getG1Box().getText2Field().setValue("g1t2");
    f.getG2Box().getText1Field().setValue("g2t1");
    f.getG2Box().getText2Field().setValue("g2t2");
    f.getG3G4Text2Field().setValue("g3g2");

    // remove enclosing field path information and fieldQname
    Document xml = f.storeToXml();
    for (Element e : XmlUtility.getChildElements(XmlUtility.getFirstChildElement(xml.getDocumentElement(), "fields"))) {
      for (Element toRemove : XmlUtility.getChildElements(e, "enclosingField")) {
        e.removeChild(toRemove);
      }
      e.removeAttribute("fieldQname");
    }

    // value should be imported to first field found
    f = new TestForm();
    assertTrue(f.loadFromXml(xml.getDocumentElement()));

    assertEquals("t3", f.getText3Field().getValue());

    String text1FieldVal = StringUtility.emptyIfNull(f.getText1Field().getValue())
        + StringUtility.emptyIfNull(f.getG1Box().getText1Field().getValue())
        + StringUtility.emptyIfNull(f.getG2Box().getText1Field().getValue());
    assertEquals("g2t1", text1FieldVal);

    String text2FieldVal = StringUtility.emptyIfNull(f.getG3G4Text2Field().getValue())
        + StringUtility.emptyIfNull(f.getG1Box().getText2Field().getValue())
        + StringUtility.emptyIfNull(f.getG2Box().getText2Field().getValue());
    assertEquals("g3g2", text2FieldVal);
  }

  private void checkFieldXml(IFormField field, String expectedFieldId, EnclosingField... expectedEnclosingFieldPath) {
    Document document = XmlUtility.createNewXmlDocument("field");
    Element xml = document.getDocumentElement();
    field.storeToXml(xml);
    assertXmlIds(expectedFieldId, field.getClass().getName(), xml);
    List<Element> enclosingFieldPath = XmlUtility.getChildElements(xml, "enclosingField");
    assertEquals(expectedEnclosingFieldPath.length, enclosingFieldPath.size());
    for (int i = 0; i < expectedEnclosingFieldPath.length; i++) {
      assertXmlIds(expectedEnclosingFieldPath[i].getXmlFieldId(), expectedEnclosingFieldPath[i].getXmlFieldQname(), enclosingFieldPath.get(i));
    }
  }

  private static void assertXmlIds(String expectedXmlFieldId, String expectedFqcn, Element xml) {
    assertEquals(expectedXmlFieldId, xml.getAttribute("fieldId"));
    assertEquals(expectedFqcn, xml.getAttribute("fieldQname"));
  }

  public static class EnclosingField {
    private final String m_xmlFieldId;
    private final String m_xmlFieldQname;

    public EnclosingField(String xmlFieldId, String xmlFieldQname) {
      m_xmlFieldId = xmlFieldId;
      m_xmlFieldQname = xmlFieldQname;
    }

    public String getXmlFieldId() {
      return m_xmlFieldId;
    }

    public String getXmlFieldQname() {
      return m_xmlFieldQname;
    }
  }

  public static final class MissingFieldSourceTestForm extends AbstractForm {

    public MainBox.MyListBox getMyListBox() {
      return getFieldByClass(MainBox.MyListBox.class);
    }

    public MainBox.Text1Field getText1Field() {
      return getFieldByClass(MainBox.Text1Field.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class Text1Field extends AbstractStringField {
      }

      @Order(20)
      public class MyListBox extends AbstractListBox<String> {
      }
    }
  }

  public static final class MissingFieldTargetTestForm extends AbstractForm {

    public MainBox.Text1Field getText1Field() {
      return getFieldByClass(MainBox.Text1Field.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class Text1Field extends AbstractStringField {
      }
    }
  }

  public static final class InvalidFieldSourceTestForm extends AbstractForm {

    public MainBox.Text1Field getText1Field() {
      return getFieldByClass(MainBox.Text1Field.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class Text1Field extends AbstractStringField {
      }
    }
  }

  public static final class InvalidFieldTargetTestForm extends AbstractForm {

    public MainBox.Text1Field getText1Field() {
      return getFieldByClass(MainBox.Text1Field.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class Text1Field extends AbstractLongField { // invalid type on purpose
      }
    }
  }
}
