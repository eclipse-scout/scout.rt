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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;
import org.eclipse.scout.rt.client.ui.form.FormWithToolbuttonsFormTest.FormWithToolbuttonsForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.FormWithToolbuttonsFormTest.FormWithToolbuttonsForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.form.FormWithToolbuttonsFormTest.FormWithToolbuttonsForm.MainBox.GroupBox.DefaultField;
import org.eclipse.scout.rt.client.ui.form.FormWithToolbuttonsFormTest.FormWithToolbuttonsForm.Toolbutton01;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class FormWithToolbuttonsFormTest {

  @Test
  public void testForm() throws ProcessingException {
    FormWithToolbuttonsForm form = new FormWithToolbuttonsForm();
    assertEquals(1, form.getToolbuttons().size());
    assertEquals(Toolbutton01.class.getSimpleName(), form.getToolbuttons().get(0).getClass().getSimpleName());
  }

  public class FormWithToolbuttonsForm extends AbstractForm {

    public FormWithToolbuttonsForm() throws ProcessingException {
      super();
    }

    @Override
    protected boolean getConfiguredAskIfNeedSave() {
      return false;
    }

    @Override
    protected String getConfiguredTitle() {
      return TEXTS.get("StringField");
    }

    public void startPageForm() throws ProcessingException {
      startInternal(new PageFormHandler());
    }

    public CloseButton getCloseButton() {
      return getFieldByClass(CloseButton.class);
    }

    public DefaultField getDefaultField() {
      return getFieldByClass(DefaultField.class);
    }

    public GroupBox getGroupBox() {
      return getFieldByClass(GroupBox.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class DefaultField extends AbstractStringField {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Default");
          }
        }
      }

      @Order(30.0)
      public class CloseButton extends AbstractCloseButton {
      }
    }

    @Order(200)
    public class Toolbutton01 extends AbstractToolButton {
      @Override
      protected String getConfiguredText() {
        return "Toolbutton";
      }

      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.Bookmark;
      }
    }

    public class PageFormHandler extends AbstractFormHandler {

      @Override
      protected void execLoad() throws ProcessingException {
        getDefaultField().setValue(TEXTS.get("Lorem"));
      }
    }
  }
}
