/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField;
import org.eclipse.scout.rt.client.ui.tile.FormFieldTileTest.TestForm.TestMainBox.TestTileField.TestTileGrid;
import org.eclipse.scout.rt.client.ui.tile.FormFieldTileTest.TestForm.TestMainBox.TestTileField.TestTileGrid.TestImageTile;
import org.eclipse.scout.rt.client.ui.tile.FormFieldTileTest.TestForm.TestMainBox.TestTileField.TestTileGrid.TestImageTile.TestImageField;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractFormFieldTile;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormFieldTileTest {

  private TestForm m_fixture;

  @Test
  public void testGetForm() {
    TestImageField field = m_fixture.getImageFieldInTile();
    assertTrue(field.isInitDone());
    assertSame(m_fixture, field.getForm());
    m_fixture.doClose();
    assertTrue(field.isDisposeDone());
    assertTrue(m_fixture.isDisposeDone());
  }

  @Test
  public void testSetFieldStyle() {
    TestImageField fieldInMenu = m_fixture.getImageFieldInTile();
    IGroupBox mainBox = m_fixture.getRootGroupBox();

    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, mainBox.getFieldStyle());

    mainBox.setFieldStyle(IFormField.FIELD_STYLE_CLASSIC, true);

    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle()); // change is not propagated into fields that are not part of the main field tree
    assertEquals(IFormField.FIELD_STYLE_CLASSIC, mainBox.getFieldStyle());

    mainBox.setFieldStyle(IFormField.FIELD_STYLE_ALTERNATIVE, false);
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, fieldInMenu.getFieldStyle());
    assertEquals(IFormField.FIELD_STYLE_ALTERNATIVE, mainBox.getFieldStyle());
  }

  @Test
  public void testSetDisabledStyle() {
    TestImageField field = m_fixture.getImageFieldInTile();
    IGroupBox mainBox = m_fixture.getRootGroupBox();

    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, field.getDisabledStyle());
    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, mainBox.getDisabledStyle());

    mainBox.setDisabledStyle(IFormField.DISABLED_STYLE_READ_ONLY, true);

    assertEquals(IFormField.DISABLED_STYLE_DEFAULT, field.getDisabledStyle()); // change is not propagated into fields that are not part of the main field tree
    assertEquals(IFormField.DISABLED_STYLE_READ_ONLY, mainBox.getDisabledStyle());
  }

  @Test
  public void testSetStatusVisible() {
    TestImageField field = m_fixture.getImageFieldInTile();
    IGroupBox mainBox = m_fixture.getRootGroupBox();
    mainBox.setStatusVisible(false, true);

    assertFalse(field.isStatusVisible()); // by default within a tile a formfield has not status.
    assertFalse(mainBox.isStatusVisible());

    mainBox.setStatusVisible(true, true);
    assertFalse(field.isStatusVisible()); // status change is not propagated into fields that are not part of the main field tree
    assertTrue(mainBox.isStatusVisible());
  }

  @Test
  public void testParentField() {
    TestImageField field = m_fixture.getImageFieldInTile();
    assertNull(field.getParentField()); // the form field in the menu has no direct parent form field.
  }

  @Before
  public void createFixture() {
    m_fixture = new TestForm();
    m_fixture.setShowOnStart(false);
    m_fixture.start();
  }

  @After
  public void cleanup() {
    m_fixture.doClose();
  }

  public static class TestForm extends AbstractForm {

    public TestImageField getImageFieldInTile() {
      return getFieldByClass(TestImageField.class);
    }

    public class TestMainBox extends AbstractGroupBox {
      public class TestTileField extends AbstractTileField<TestTileGrid> {
        public class TestTileGrid extends AbstractTileGrid<TestImageTile> {
          public class TestImageTile extends AbstractFormFieldTile<TestImageField> {
            public class TestImageField extends AbstractImageField {
            }
          }
        }
      }
    }
  }
}
