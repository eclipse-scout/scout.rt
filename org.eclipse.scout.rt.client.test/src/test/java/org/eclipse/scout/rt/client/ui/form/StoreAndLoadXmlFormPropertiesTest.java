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

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.DynamicCancelButton;
import org.eclipse.scout.testing.client.form.DynamicForm;
import org.eclipse.scout.testing.client.form.DynamicGroupBox;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests exporting properties from/to an XML and vice versa.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlFormPropertiesTest {

  private static final boolean EXPECTED_BOOLEAN = true;
  private static final int EXPECTED_INTEGER = 42;
  private static final String EXPECTED_TEXT = "a test text";

  @Test
  public void testStoreAndLoadPrimitiveType() {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithAllProperties f = new DynamicFormWithAllProperties("Form1", mainBox);
    try {
      initializeFormWithAllProperties(f);
      String xml = assertStoredAllPropertiesToXml(f);

      // reset properties
      f.setPrimitiveBoolean(false);
      f.setPrimitiveInteger(0);
      f.setText(null);

      // import xml and check properties
      assertTrue(f.loadFromXmlString(xml));
      assertTrue(f.isPrimitiveBoolean());
      assertEquals(42, f.getPrimitiveInteger());
      assertEquals(EXPECTED_TEXT, f.getText());
    }
    finally {
      f.doClose();
    }
  }

  @Test
  public void testUnknownPropertyWithValue() {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithAllProperties source = new DynamicFormWithAllProperties("Form2Source", mainBox);
    final DynamicFormWithSomeProperties target = new DynamicFormWithSomeProperties("Form2Target", mainBox);
    try {
      initializeFormWithAllProperties(source);
      String xml = assertStoredAllPropertiesToXml(source);

      // import xml to a form with one property missing
      target.start(new FormHandler());
      assertFalse(target.loadFromXmlString(xml)); // import unsuccessful
      // assert that valid properties were imported correctly
      assertEquals(EXPECTED_INTEGER, target.getPrimitiveInteger());
      assertEquals(EXPECTED_BOOLEAN, target.isPrimitiveBoolean());
    }
    finally {
      source.doClose();
      target.doClose();
    }
  }
  @Test
  public void testUnknownPropertyWithoutValue() {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithAllProperties source = new DynamicFormWithAllProperties("Form2Source", mainBox);
    final DynamicFormWithSomeProperties target = new DynamicFormWithSomeProperties("Form2Target", mainBox);
    try {
      initializeFormWithAllProperties(source);
      source.setText(null); // this property will be lost, but has no value anyway
      String xml = assertStoredAllPropertiesToXml(source);

      // import xml to a form with one property missing
      target.start(new FormHandler());
      assertTrue(target.loadFromXmlString(xml)); // import successful because unknown property had no value
      // assert that valid properties were imported correctly
      assertEquals(EXPECTED_INTEGER, target.getPrimitiveInteger());
      assertEquals(EXPECTED_BOOLEAN, target.isPrimitiveBoolean());
    }
    finally {
      source.doClose();
      target.doClose();
    }
  }

  @Test
  public void testInvalidProperty() {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithAllProperties source = new DynamicFormWithAllProperties("Form3Source", mainBox);
    final DynamicFormWithInvalidProperties target = new DynamicFormWithInvalidProperties("Form3Target", mainBox);
    try {
      initializeFormWithAllProperties(source);
      String xml = assertStoredAllPropertiesToXml(source);

      // import xml to a form containing same property name but different type
      target.start(new FormHandler());
      assertFalse(target.loadFromXmlString(xml));
      assertEquals(0, target.getText());
      assertEquals(EXPECTED_BOOLEAN, target.isPrimitiveBoolean());
      assertEquals(EXPECTED_INTEGER, target.getPrimitiveInteger());
    }
    finally {
      source.doClose();
      target.doClose();
    }
  }

  private static void initializeFormWithAllProperties(DynamicFormWithAllProperties source) {
    source.start(new FormHandler());
    source.setPrimitiveBoolean(EXPECTED_BOOLEAN);
    source.setPrimitiveInteger(EXPECTED_INTEGER);
    source.setText(EXPECTED_TEXT);
  }

  private static String assertStoredAllPropertiesToXml(DynamicFormWithAllProperties source) {
    String xml = source.storeToXmlString();
    assertNotNull(xml);
    assertTrue(xml.contains("primitiveInteger"));
    assertTrue(xml.contains("primitiveBoolean"));
    assertTrue(xml.contains("text"));
    return xml;
  }

  protected static final class DynamicFormWithAllProperties extends DynamicForm {
    private boolean m_primitiveBoolean;
    private int m_primitiveInteger;
    private String m_text;

    private DynamicFormWithAllProperties(String title, IGroupBox mainBox) {
      super(title, mainBox);
    }

    @FormData
    public boolean isPrimitiveBoolean() {
      return m_primitiveBoolean;
    }

    @FormData
    public void setPrimitiveBoolean(boolean primitiveBoolean) {
      m_primitiveBoolean = primitiveBoolean;
    }

    @FormData
    public int getPrimitiveInteger() {
      return m_primitiveInteger;
    }

    @FormData
    public void setPrimitiveInteger(int primitiveInteger) {
      m_primitiveInteger = primitiveInteger;
    }

    @FormData
    public String getText() {
      return m_text;
    }

    @FormData
    public void setText(String text) {
      m_text = text;
    }
  }

  protected static final class DynamicFormWithSomeProperties extends DynamicForm {
    private boolean m_primitiveBoolean;
    private int m_primitiveInteger;

    private DynamicFormWithSomeProperties(String title, IGroupBox mainBox) {
      super(title, mainBox);
    }

    @FormData
    public boolean isPrimitiveBoolean() {
      return m_primitiveBoolean;
    }

    @FormData
    public void setPrimitiveBoolean(boolean primitiveBoolean) {
      m_primitiveBoolean = primitiveBoolean;
    }

    @FormData
    public int getPrimitiveInteger() {
      return m_primitiveInteger;
    }

    @FormData
    public void setPrimitiveInteger(int primitiveInteger) {
      m_primitiveInteger = primitiveInteger;
    }
  }

  protected static final class DynamicFormWithInvalidProperties extends DynamicForm {
    private boolean m_primitiveBoolean;
    private int m_primitiveInteger;
    private int m_text;  // this property has an invalid type - this is expected

    private DynamicFormWithInvalidProperties(String title, IGroupBox mainBox) {
      super(title, mainBox);
    }

    @FormData
    public boolean isPrimitiveBoolean() {
      return m_primitiveBoolean;
    }

    @FormData
    public void setPrimitiveBoolean(boolean primitiveBoolean) {
      m_primitiveBoolean = primitiveBoolean;
    }

    @FormData
    public int getPrimitiveInteger() {
      return m_primitiveInteger;
    }

    @FormData
    public void setPrimitiveInteger(int primitiveInteger) {
      m_primitiveInteger = primitiveInteger;
    }

    @FormData
    public int getText() {
      return m_text;
    }

    @FormData
    public void setText(int text) {
      m_text = text;
    }
  }
}
