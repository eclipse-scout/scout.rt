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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTestGroupBox.InnerTestGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G1Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G2Box;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class StoreAndLoadXml3FormTest {

  @Test
  public void testXmlFieldIds() throws Exception {
    TestForm f = new TestForm();
    assertEquals("MainBox", f.getMainBox().getFieldId());
    assertEquals("customId", f.getText4Field().getFieldId());
    assertEquals("Text1Field", f.getText1Field().getFieldId());
  }

  @Test
  public void testFieldIds() throws Exception {
    TestForm f = new TestForm();
    checkFieldXml(f.getMainBox(), "MainBox");
    checkFieldXml(f.getText4Field(), "customId");
    checkFieldXml(f.getText1Field(), "Text1Field");

    checkFieldXml(f.getG1Box(), "G1Box");
    checkFieldXml(f.getG2Box(), "G2Box");
    checkFieldXml(f.getG3Box(), "G3Box");
    checkFieldXml(f.getG4Box(), "G4Box");

    checkFieldXml(f.getText3Field(), "Text3Field");

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
  public void testFormId() throws Exception {
    TestForm f = new TestForm();
    SimpleXmlElement xml = f.storeXML();
    assertEquals("TestForm", xml.getAttribute("formId"));
    assertEquals(TestForm.class.getName(), xml.getAttribute("formQname"));
  }

  @Test
  public void testStoreLoad() throws ProcessingException {
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
    f.getG1Box().getTestListBox().setValue(new String[]{"g1L"});
    f.getG2Box().getTestListBox().setValue(new String[]{"g2L"});
    String xml = f.getXML(null);

    f = new TestForm();
    f.setXML(xml);

    //new form should contain the stored values
    assertEquals("t1", f.getText1Field().getValue());
    assertEquals("t3", f.getText3Field().getValue());
    assertEquals("t4", f.getText4Field().getValue());
    assertEquals("g1t1", f.getG1Box().getText1Field().getValue());
    assertEquals("g1t2", f.getG1Box().getText2Field().getValue());
    assertEquals("g2t1", f.getG2Box().getText1Field().getValue());
    assertEquals("g3g2", f.getG3G4Text2Field().getValue());
    assertEquals("g1L", f.getG1Box().getTestListBox().getValue()[0]);
    assertEquals("g2L", f.getG2Box().getTestListBox().getValue()[0]);
  }

  @Test
  public void testLegacyLoad() throws ProcessingException {
    TestForm f = new TestForm();
    f.getText1Field().setValue("t1");
    f.getText3Field().setValue("t3");
    f.getG1Box().getText1Field().setValue("g1t1");
    f.getG1Box().getText2Field().setValue("g1t2");
    f.getG2Box().getText1Field().setValue("g2t1");
    f.getG2Box().getText2Field().setValue("g2t2");
    f.getG3G4Text2Field().setValue("g3g2");

    // remove enclosing field path information and fieldQname
    SimpleXmlElement xml = f.storeXML();
    for (SimpleXmlElement e : xml.getChild("fields").getChildren()) {
      e.removeChildren("enclosingField");
      e.removeAttribute("fieldQname");
    }

    // value should be imported to first field found
    f = new TestForm();
    f.loadXML(xml);

    assertEquals("t3", f.getText3Field().getValue());

    String text1FieldVal = StringUtility.nvl(f.getText1Field().getValue(), "")
        + StringUtility.nvl(f.getG1Box().getText1Field().getValue(), "")
        + StringUtility.nvl(f.getG2Box().getText1Field().getValue(), "");
    assertEquals("g2t1", text1FieldVal);

    String text2FieldVal = StringUtility.nvl(f.getG3G4Text2Field().getValue(), "")
        + StringUtility.nvl(f.getG1Box().getText2Field().getValue(), "")
        + StringUtility.nvl(f.getG2Box().getText2Field().getValue(), "");
    assertEquals("g3g2", text2FieldVal);
  }

  private void checkFieldXml(IFormField field, String expectedFieldId, EnclosingField... expectedEnclosingFieldPath) throws ProcessingException {
    SimpleXmlElement xml = new SimpleXmlElement("field");
    field.storeXML(xml);
    assertXmlIds(expectedFieldId, field.getClass().getName(), xml);
    ArrayList<SimpleXmlElement> enclosingFieldPath = xml.getChildren("enclosingField");
    assertEquals(expectedEnclosingFieldPath.length, enclosingFieldPath.size());
    for (int i = 0; i < expectedEnclosingFieldPath.length; i++) {
      assertXmlIds(expectedEnclosingFieldPath[i].getXmlFieldId(), expectedEnclosingFieldPath[i].getXmlFieldQname(), enclosingFieldPath.get(i));
    }
  }

  private static void assertXmlIds(String expectedXmlFieldId, String expectedFqcn, SimpleXmlElement xml) {
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
}
