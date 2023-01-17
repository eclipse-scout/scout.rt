/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link AbstractForm} with different implementation of {@link AbstractFormHandler#execStore()} and/or of
 * {@link AbstractForm#execStored()}.
 *
 * @since 4.1.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormStoredTest {

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
  public void testDefaultSave() {
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testDefaultOk() {
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testDefaultCancel() {
    m_form.startModify();
    assertEquals("isSaveNeeded [before]", false, m_form.isSaveNeeded());
    m_form.clickCancel();
    assertEquals("isFormStored", false, m_form.isFormStored());
    assertEquals("isSaveNeeded", false, m_form.isSaveNeeded());
    assertEquals("isFormClosed", true, m_form.isFormClosed()); //form closed
  }

  @Test
  public void testMarkStoredHandlerSave() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredHandlerOk() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredFormSave() {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredFormOk() {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredCombinationSave() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredCombinationOk() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkNotStoredHandlerSave() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredHandlerOk() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormSave() {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormOk() {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationSave() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationOk() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerSave() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerOk() {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }
}
