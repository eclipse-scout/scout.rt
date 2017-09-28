/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.eclipse.scout.testing.client.form.DynamicCancelButton;
import org.eclipse.scout.testing.client.form.DynamicForm;
import org.eclipse.scout.testing.client.form.DynamicGroupBox;
import org.eclipse.scout.testing.client.form.DynamicOkButton;
import org.eclipse.scout.testing.client.form.DynamicStringField;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
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
        new DynamicCancelButton());
    final DynamicForm f = new DynamicForm("Form1", mainBox);
    f.setModal(modal);
    f.setDisplayHint(displayHint);
    f.setDisplayViewId(viewId);
    testSequence.add(0);
    testSequence.add(1);
    //emulate that gui clicks on ok button
    IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        testSequence.add(2);
        f.getButton("ok").getUIFacade().fireButtonClickFromUI();
        Thread.sleep(200L);
        testSequence.add(3);
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(200, TimeUnit.MILLISECONDS)));

    try {
      f.start(new FormHandler());
      f.waitFor();
      testSequence.add(4);
      future.awaitDoneAndGet();
      ScoutAssert.assertOrder(new Integer[]{0, 1, 2, 3, 4}, testSequence.toArray());
    }
    finally {
      f.doClose();
    }
  }
}
