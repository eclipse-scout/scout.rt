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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.ExceptionHandlerError;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link AbstractForm} where {@link AbstractFormHandler#execStore()} or {@link AbstractForm#execStored()}
 * throws a {@link VetoException}.
 * A mocked {@link IExceptionHandlerService} simulates that a message box will be displayed for the
 * {@link VetoException}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormStoredWithVetoTest {

  @Test
  public void testVetoHandlerSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickSave();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  @Test
  public void testVetoHandlerOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickOk();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  @Test
  public void testVetoFormSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickSave();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  @Test
  public void testVetoFormOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickOk();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  @Test
  public void testVetoCombinationSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickSave();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  @Test
  public void testVetoCombinationOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());

    try {
      f.clickOk();
      fail("VetoException excepted to be handled in ExceptionHandler");
    }
    catch (ExceptionHandlerError e) {
      assertVetoException(e.getCause());
      f.assertStoreInterrupted();
    }
  }

  private void assertVetoException(ProcessingException e) {
    assertTrue("VetoException expected [actual=" + e.getClass() + "]", e instanceof VetoException);
    assertFalse("exception.isConsumed()", e.isConsumed());
    assertEquals("exception.getMessage()", FormToStore.VETO_EXCEPTION_TEXT, e.getMessage());
  }
}
