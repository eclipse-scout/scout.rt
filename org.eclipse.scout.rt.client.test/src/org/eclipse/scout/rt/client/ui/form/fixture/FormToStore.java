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

package org.eclipse.scout.rt.client.ui.form.fixture;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractSaveButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.SaveButton;

/**
 * Fixture form {@link AbstractForm} with different implementation of {@link AbstractFormHandler#execStore()} defined in
 * {@link MethodImplementation}
 * 
 * @since 4.1.0
 */
public class FormToStore extends AbstractForm {
  private MethodImplementation m_execStoreHandlerImplementation = MethodImplementation.DO_NOTHING;

  public void setExecStoreHandlerImplementation(MethodImplementation execStoreHandlerImplementation) {
    m_execStoreHandlerImplementation = execStoreHandlerImplementation;
  }

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public FormToStore() throws ProcessingException {
    super();
  }

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(30.0)
    public class SaveButton extends AbstractSaveButton {
    }

    @Order(40.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(50.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execStore() throws ProcessingException {
      doIt(m_execStoreHandlerImplementation);
    }
  }

  private void doIt(MethodImplementation implementation) throws ProcessingException {
    switch (implementation) {
      case CONSUMED_VETO_EXCEPTION:
        VetoException vetoException = new VetoException("Implementation throws a consumed VetoException");
        vetoException.consume();
        throw vetoException;
      case DO_NOTHING:
      default:
        //nothing to do
        break;
    }
  }

  /**
   * Ensure that the store operation was interrupted (form not stored, that need save and open).
   */
  public void assertSaveSuccessful() {
    assertEquals("isFormStored", true, isFormStored());
    assertEquals("isSaveNeeded", false, isSaveNeeded());
    assertEquals("isFormClosed", false, isFormClosed()); //form open
  }

  /**
   * Ensure that the store operation was interrupted (form not stored, that need save and open).
   */
  public void assertOkSuccessful() {
    assertEquals("isFormStored", true, isFormStored());
    assertEquals("isSaveNeeded", false, isSaveNeeded());
    assertEquals("isFormClosed", true, isFormClosed()); //form closed
  }

  /**
   * Ensure that the store operation was interrupted (form not stored, that need save and open).
   */
  public void assertStoreInterrupted() {
    assertEquals("isFormStored", false, isFormStored());
    assertEquals("isSaveNeeded", true, isSaveNeeded());
    assertEquals("isFormClosed", false, isFormClosed()); //form open
  }

  public static enum MethodImplementation {
    DO_NOTHING,
    CONSUMED_VETO_EXCEPTION
  }

  public void clickSave() throws ProcessingException {
    getFieldByClass(SaveButton.class).doClick();
  }

  public void clickOk() throws ProcessingException {
    getFieldByClass(OkButton.class).doClick();
  }

  public void clickCancel() throws ProcessingException {
    getFieldByClass(CancelButton.class).doClick();
  }

}
