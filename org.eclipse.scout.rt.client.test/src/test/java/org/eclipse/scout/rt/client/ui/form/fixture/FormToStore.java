/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractSaveButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MainBox.SaveButton;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Fixture form {@link AbstractForm} with different implementation of {@link AbstractFormHandler#execStore()} defined in
 * {@link MethodImplementation}
 * 
 * @since 4.1.0
 */
public class FormToStore extends AbstractForm {
  public static final String VETO_EXCEPTION_TEXT = "Implementation throws a VetoException";
  private MethodImplementation m_execStoreHandlerImplementation = MethodImplementation.DO_NOTHING;
  private MethodImplementation m_execStoredImplementation = MethodImplementation.DO_NOTHING;

  public void setExecStoreHandlerImplementation(MethodImplementation execStoreHandlerImplementation) {
    m_execStoreHandlerImplementation = execStoreHandlerImplementation;
  }

  public void setExecStoredImplementation(MethodImplementation execStoredImplementation) {
    m_execStoredImplementation = execStoredImplementation;
  }

  /**
   * @throws org.eclipse.scout.rt.platform.exception.ProcessingException
   */
  public FormToStore() {
    super();
  }

  /**
   * @throws org.eclipse.scout.rt.platform.exception.ProcessingException
   */
  public void startModify() {
    startInternal(new ModifyHandler());
  }

  @Override
  protected void execStored() {
    doIt(m_execStoredImplementation);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(30)
    public class SaveButton extends AbstractSaveButton {
    }

    @Order(40)
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execStore() {
      doIt(m_execStoreHandlerImplementation);
    }
  }

  private void doIt(MethodImplementation implementation) {
    switch (implementation) {
      case MARK_NOT_STORED:
        setFormStored(false);
        break;
      case MARK_STORED:
        setFormStored(true);
        break;
      case VETO_EXCEPTION:
        throw new VetoException(VETO_EXCEPTION_TEXT);
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
    MARK_STORED,
    MARK_NOT_STORED,
    VETO_EXCEPTION,
    CONSUMED_VETO_EXCEPTION
  }

  public void clickSave() {
    getFieldByClass(SaveButton.class).doClick();
  }

  public void clickOk() {
    getFieldByClass(OkButton.class).doClick();
  }

  public void clickCancel() {
    getFieldByClass(CancelButton.class).doClick();
  }

}
