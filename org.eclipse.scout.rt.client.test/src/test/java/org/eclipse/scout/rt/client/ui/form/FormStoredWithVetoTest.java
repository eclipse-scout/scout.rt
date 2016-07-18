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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link AbstractForm} where {@link AbstractFormHandler#execStore()} or {@link AbstractForm#execStored()}
 * throws a {@link VetoException}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormStoredWithVetoTest {

  private FormToStore m_form;

  @Before
  public void before() {
    m_form = new FormToStore();
  }

  @After
  public void after() {
    if (m_form == null || !m_form.isShowing()) {
      return;
    }
    m_form.doClose();
  }

  @Test
  public void testVetoHandlerSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickSave();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testVetoHandlerOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickOk();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testVetoFormSave() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickSave();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testVetoFormOk() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickOk();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testVetoCombinationSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickSave();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testVetoCombinationOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());

    m_form.clickOk();

    // verify
    assertHandledVetoException();
    m_form.assertStoreInterrupted();
  }

  private void assertHandledVetoException() {
    // Ensure 'JUnitExceptionHandler' to be installed.
    JUnitExceptionHandler exceptionHandler = BEANS.get(JUnitExceptionHandler.class);
    assertTrue(String.format("this test expects 'JUnitExceptionHandler' to be installed [actual=%s]", exceptionHandler), exceptionHandler instanceof JUnitExceptionHandler);

    try {
      exceptionHandler.throwOnError();
      fail("VetoException excepted to be handled in 'JUnitExceptionHandler'");
    }
    catch (Throwable e) {
      assertTrue("VetoException expected [actual=" + e.getClass() + "]", e instanceof VetoException);
      assertFalse("exception.isConsumed()", ((VetoException) e).isConsumed());
      assertEquals("exception.getMessage()", FormToStore.VETO_EXCEPTION_TEXT, ((VetoException) e).getStatus().getBody());
    }
  }
}
