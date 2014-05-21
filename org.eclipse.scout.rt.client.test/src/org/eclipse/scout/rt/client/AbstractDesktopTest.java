/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.AbstractDesktopTest.CheckSaveTestForm.MainBox.MessageField;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractDesktop}
 */
@RunWith(ScoutClientTestRunner.class)
public class AbstractDesktopTest {
  private CheckSaveTestForm m_testForm;

  @Before
  public void setup() throws ProcessingException {
    m_testForm = new CheckSaveTestForm();
  }

  @After
  public void tearDown() throws ProcessingException {
    m_testForm.doClose();
  }

  @Test
  public void testNoSaveNeeded() throws ProcessingException {
    m_testForm.startNew();
    assertFalse(m_testForm.isSaveNeeded());
  }

  @Test
  public void testSaveNeeded() throws ProcessingException {
    m_testForm.startNew();
    m_testForm.getMessageField().setValue("test");
    assertTrue(m_testForm.isSaveNeeded());
  }

  /**
   * {@link AbstractDesktop#doBeforeClosingInternal()}
   */
  @Test
  public void testClosingDoBeforeClosingInternal() throws ProcessingException {
    AbstractDesktop d = new AbstractDesktop() {
    };
    boolean closing = d.doBeforeClosingInternal();
    assertTrue(closing);
  }

  @Test
  public void testUnsavedForms() throws ProcessingException {
    m_testForm.startNew();
    m_testForm.getMessageField().setValue("test");
    IDesktop d = ClientSyncJob.getCurrentSession().getDesktop();
    assertTrue(d.getUnsavedForms().contains(m_testForm));
  }

  @ClassId("d090cc19-ba7a-4f79-b147-e58765a837fb")
  class CheckSaveTestForm extends AbstractForm {

    /**
     * @throws ProcessingException
     */
    public CheckSaveTestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return TEXTS.get("AskIfSaveNeededForm");
    }

    public void startNew() throws ProcessingException {
      startInternal(new NewHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public MessageField getMessageField() {
      return getFieldByClass(MessageField.class);
    }

    @Order(10.0)
    @ClassId("d8a4cad3-4f85-4baa-993a-87a04d444e40")
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      @ClassId("6a3f206c-c51b-4274-9dc3-959bf05e74da")
      public class MessageField extends AbstractStringField {
      }

      @Order(20.0)
      @ClassId("c7310b71-1d1a-482e-80ee-45af9b3eb4a4")
      public class OkButton extends AbstractOkButton {
      }

      @Order(30.0)
      @ClassId("7016864a-e34a-49b1-a387-37bab208b458")
      public class CancelButton extends AbstractCancelButton {
      }
    }

    public class NewHandler extends AbstractFormHandler {
    }

  }

}
