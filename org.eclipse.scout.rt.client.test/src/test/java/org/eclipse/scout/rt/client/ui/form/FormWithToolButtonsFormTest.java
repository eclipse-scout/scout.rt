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
import org.eclipse.scout.rt.client.ui.form.FormWithToolButtonsFormTest.FormWithToolButtonsForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.form.FormWithToolButtonsFormTest.FormWithToolButtonsForm.ToolButton01;
import org.eclipse.scout.rt.client.ui.form.FormWithToolButtonsFormTest.FormWithToolButtonsForm.ToolButton02;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class FormWithToolButtonsFormTest {

  @Test
  public void testForm() throws ProcessingException {
    FormWithToolButtonsForm form = new FormWithToolButtonsForm();
    assertEquals(2, form.getToolButtons().size());
    assertEquals(ToolButton01.class.getSimpleName(), form.getToolButtons().get(0).getClass().getSimpleName());
    assertEquals(ToolButton02.class.getSimpleName(), form.getToolButtons().get(1).getClass().getSimpleName());

    ToolButton01 toolButton = form.getToolButtonByClass(ToolButton01.class);
    assertEquals(ToolButton01.class.getSimpleName(), toolButton.getClass().getSimpleName());
  }

  public class FormWithToolButtonsForm extends AbstractForm {

    public FormWithToolButtonsForm() throws ProcessingException {
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

    public void startView() throws ProcessingException {
      startInternal(new ViewHandler());
    }

    public CloseButton getCloseButton() {
      return getFieldByClass(CloseButton.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(30.0)
      public class CloseButton extends AbstractCloseButton {
      }
    }

    public abstract class AbstractButtonTemplate extends AbstractToolButton {
    }

    @Order(200)
    public class ToolButton01 extends AbstractToolButton {
      @Override
      protected String getConfiguredText() {
        return "Toolbutton";
      }

      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.Bookmark;
      }
    }

    @Order(210)
    public class ToolButton02 extends AbstractButtonTemplate {

      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.Gears;
      }
    }

    public class ViewHandler extends AbstractFormHandler {
    }
  }
}
