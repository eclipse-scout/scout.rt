/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.imagebox;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.ImageFieldTest.TestForm.MainBox.ImageField;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractImageField}
 *
 * @since 3.10.0-M4
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ImageFieldTest {

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Test Form";
    }

    public void startForm() {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public AbstractImageField getImageField() {
      return getFieldByClass(ImageField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class ImageField extends AbstractImageField {

        @Order(10)
        public class ImageFieldMenu extends AbstractMenu {

          @Override
          protected String getConfiguredKeyStroke() {
            return "control-a";
          }

          @Override
          protected String getConfiguredText() {
            return "ImageFieldMenu";
          }
        }
      }
    }
  }

  private TestForm m_form;

  @Before
  public void setUp() {
    m_form = new TestForm();
    m_form.startForm();
  }

  @Test
  public void testMenusAndKeyStrokes() {
    List<IMenu> imageFieldMenus = m_form.getImageField().getMenus();
    Assert.assertEquals("ImageField should have 1 menu", 1, imageFieldMenus.size());
    Assert.assertEquals("ImageFieldMenu", imageFieldMenus.get(0).getText());
    Assert.assertEquals("control-a", imageFieldMenus.get(0).getKeyStroke());
  }

  @Test
  public void testFileExtensions() {
    List<String> extensions = m_form.getImageField().getFileExtensions();
    Assert.assertEquals("There should be 5 file extensions registered", 5, extensions.size());
    Assert.assertTrue(extensions.contains("png"));
    Assert.assertTrue(extensions.contains("bmp"));
    Assert.assertTrue(extensions.contains("jpg"));
    Assert.assertTrue(extensions.contains("jpeg"));
    Assert.assertTrue(extensions.contains("gif"));
  }

  @After
  public void tearDown() {
    m_form.doClose();
  }
}
