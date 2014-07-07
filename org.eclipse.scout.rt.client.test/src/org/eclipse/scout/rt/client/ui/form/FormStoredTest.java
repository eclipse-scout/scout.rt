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

import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore;
import org.eclipse.scout.rt.client.ui.form.fixture.FormToStore.MethodImplementation;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link AbstractForm} with different implementation of {@link AbstractFormHandler#execStore()}.
 * 
 * @since 4.1.0
 */
@RunWith(ScoutClientTestRunner.class)
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
