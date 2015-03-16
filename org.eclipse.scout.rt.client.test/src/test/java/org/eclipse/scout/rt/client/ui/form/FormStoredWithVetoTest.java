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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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

  private List<IBean<?>> m_registeredServices = new ArrayList<>();
  private IExceptionHandlerService m_exceptionHandlerService;

  @Before
  public void setup() {
    m_exceptionHandlerService = Mockito.mock(IExceptionHandlerService.class);
    m_registeredServices.add(TestingUtility.registerService(9000, m_exceptionHandlerService, IExceptionHandlerService.class));
  }

  @After
  public void teardown() {
    TestingUtility.unregisterServices(m_registeredServices);
  }

  @Test
  public void testVetoHandlerSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    assertException();
    f.assertStoreInterrupted();
  }

  @Test
  public void testVetoHandlerOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    assertException();
    f.assertStoreInterrupted();
  }

  @Test
  public void testVetoFormSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    assertException();
    f.assertStoreInterrupted();
  }

  @Test
  public void testVetoFormOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    assertException();
    f.assertStoreInterrupted();
  }

  @Test
  public void testVetoCombinationSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    assertException();
    f.assertStoreInterrupted();
  }

  @Test
  public void testVetoCombinationOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    assertException();
    f.assertStoreInterrupted();
  }

  private void assertException() {
    ArgumentCaptor<ProcessingException> argument = ArgumentCaptor.forClass(ProcessingException.class);
    Mockito.verify(m_exceptionHandlerService).handleException(argument.capture());
    assertEquals("exception.isConsumed()", false, argument.getValue().isConsumed());
    assertEquals("exception.getMessage()", FormToStore.VETO_EXCEPTION_TEXT, argument.getValue().getMessage());
  }
}
