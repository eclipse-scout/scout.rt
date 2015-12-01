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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 * Tests exporting a from to an XML and vice versa.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXml1FormTest {

  private static final boolean EXPECTED_BOOLEAN = true;
  private static final int EXPECTED_INTEGER = 42;
  private static final String EXPECTED_TEXT = "a test text";

  @Test
  public void testStoreAndLoadPrimitiveType() throws Exception {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithProperties f = new DynamicFormWithProperties("Form1", mainBox);
    try {
      f.start(new FormHandler());

      // change values
      f.setPrimitiveBoolean(EXPECTED_BOOLEAN);
      f.setPrimitiveInteger(EXPECTED_INTEGER);
      f.setText(EXPECTED_TEXT);

      // export to xml and check result
      String xml = f.storeToXmlString();
      assertNotNull(xml);
      assertTrue(xml.contains("primitiveInteger"));
      assertTrue(xml.contains("primitiveBoolean"));
      assertTrue(xml.contains("text"));

      // reset properties
      f.setPrimitiveBoolean(false);
      f.setPrimitiveInteger(0);
      f.setText(null);

      // import xml and check properties
      f.loadFromXmlString(xml);
      assertTrue(f.isPrimitiveBoolean());
      assertEquals(42, f.getPrimitiveInteger());
      assertEquals(EXPECTED_TEXT, f.getText());
    }
    finally {
      f.doClose();
    }
  }

  public static final class DynamicFormWithProperties extends DynamicForm {
    private boolean m_primitiveBoolean;
    private int m_primitiveInteger;
    private String m_text;

    private DynamicFormWithProperties(String title, IGroupBox mainBox) {
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
}
