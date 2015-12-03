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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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

  @Test
  public void testDefaultSave() throws Exception {
    FormToStore f = new FormToStore();
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertSaveSuccessful();
  }

  @Test
  public void testDefaultOk() throws Exception {
    FormToStore f = new FormToStore();
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertOkSuccessful();
  }

  @Test
  public void testDefaultCancel() throws Exception {
    FormToStore f = new FormToStore();
    f.startModify();
    assertEquals("isSaveNeeded [before]", false, f.isSaveNeeded());
    f.clickCancel();
    assertEquals("isFormStored", false, f.isFormStored());
    assertEquals("isSaveNeeded", false, f.isSaveNeeded());
    assertEquals("isFormClosed", true, f.isFormClosed()); //form closed
  }

  @Test
  public void testMarkStoredHandlerSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredHandlerOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredFormSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredFormOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertOkSuccessful();
  }

  @Test
  public void testMarkStoredCombinationSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertSaveSuccessful();
  }

  @Test
  public void testMarkStoredCombinationOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.MARK_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertOkSuccessful();
  }

  @Test
  public void testMarkNotStoredHandlerSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredHandlerOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredFormOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertStoreInterrupted();
  }

  @Test
  public void testMarkNotStoredCombinationOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.MARK_STORED); //no effect because of execStored in the form
    f.setExecStoredImplementation(MethodImplementation.MARK_NOT_STORED);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerSave() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickSave();
    f.assertStoreInterrupted();
  }

  @Test
  public void testConsumedVetoHandlerOk() throws Exception {
    FormToStore f = new FormToStore();
    f.setExecStoreHandlerImplementation(MethodImplementation.CONSUMED_VETO_EXCEPTION);
    f.startModify();
    f.touch();
    assertEquals("isSaveNeeded [before]", true, f.isSaveNeeded());
    f.clickOk();
    f.assertStoreInterrupted();
  }
}
