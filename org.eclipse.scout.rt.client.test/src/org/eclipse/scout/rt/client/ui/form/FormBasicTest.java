/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.testing.client.form.DynamicCancelButton;
import org.eclipse.scout.rt.testing.client.form.DynamicForm;
import org.eclipse.scout.rt.testing.client.form.DynamicGroupBox;
import org.eclipse.scout.rt.testing.client.form.DynamicOkButton;
import org.eclipse.scout.rt.testing.client.form.DynamicStringField;
import org.eclipse.scout.rt.testing.client.form.FormHandler;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class FormBasicTest {

  @Test
  public void testStartAndWait_Dialog() throws Exception {
    testStartAndWaitImpl(true, IForm.DISPLAY_HINT_DIALOG, null);
  }

  @Test
  public void testStartAndWait_View() throws Exception {
    testStartAndWaitImpl(false, IForm.DISPLAY_HINT_VIEW, IForm.VIEW_ID_CENTER);
  }

  protected void testStartAndWaitImpl(final boolean modal, final int displayHint, final String viewId) throws Exception {
    final ArrayList<Integer> testSequence = new ArrayList<Integer>();
    DynamicGroupBox mainBox = new DynamicGroupBox(
        new DynamicStringField("f1", "First Name"),
        new DynamicStringField("f2", "Last Name"),
        new DynamicStringField("f3", "Address"),
        new DynamicOkButton(),
        new DynamicCancelButton()
        );
    final DynamicForm f = new DynamicForm("Form1", mainBox);
    f.setModal(modal);
    f.setDisplayHint(displayHint);
    f.setDisplayViewId(viewId);
    testSequence.add(0);
    testSequence.add(1);
    //emulate that gui clicks on ok button
    ClientSyncJob closeJob = new ClientSyncJob("Close", ClientSyncJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        testSequence.add(2);
        f.getButton("ok").getUIFacade().fireButtonClickedFromUI();
        Thread.sleep(200L);
        testSequence.add(3);
      }
    };
    closeJob.schedule(200L);
    try {
      f.start(new FormHandler());
      f.waitFor();
      testSequence.add(4);
      closeJob.join();
      ScoutAssert.assertOrder(new Integer[]{0, 1, 2, 3, 4}, testSequence.toArray());
    }
    finally {
      f.doClose();
    }
  }
}
