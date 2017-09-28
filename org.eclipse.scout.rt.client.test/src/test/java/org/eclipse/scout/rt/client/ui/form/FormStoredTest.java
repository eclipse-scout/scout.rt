/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
  public void testDefaultSave() throws Exception {
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testDefaultOk() throws Exception {
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testDefaultCancel() throws Exception {
    m_form.startModify();
    assertEquals("isSaveNeeded [before]", false, m_form.isSaveNeeded());
    m_form.clickCancel();
    assertEquals("isFormStored", false, m_form.isFormStored());
    assertEquals("isSaveNeeded", false, m_form.isSaveNeeded());
    assertEquals("isFormClosed", true, m_form.isFormClosed()); //form closed
  }

  @Test
  public void testMarkStoredHandlerSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredHandlerOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredFormSave() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredFormOk() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredCombinationSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredCombinationOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertOkSuccessful();
  }

  @Test
  public void testMarkNotStoredHandlerSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredHandlerOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormSave() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormOk() throws Exception {
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    m_form.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerSave() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickSave();
    m_form.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerOk() throws Exception {
    m_form.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    m_form.startModify();
    m_form.touch();
    assertEquals("isSaveNeeded [before]", true, m_form.isSaveNeeded());
    m_form.clickOk();
    m_form.assertStoreInterrupted();
  }
}
