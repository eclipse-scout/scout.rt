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

import java.util.ArrayList;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.job.IFuture;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bugzilla 352472 - Possible Deadlock at IForm#waitFor
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormWaitForTest {
  private static final Logger LOG = LoggerFactory.getLogger(FormWaitForTest.class);

  @Test
  public void testStartAndWait_Dialog() throws Exception {
    testStartAndWaitImpl(true, IForm.DISPLAY_HINT_DIALOG, null);
  }

  @Test
  public void testStartAndWait_View() throws Exception {
    testStartAndWaitImpl(true, IForm.DISPLAY_HINT_DIALOG, null);
  }

  protected void testStartAndWaitImpl(final boolean modal, final int displayHint, final String viewId) throws Exception {
    final ArrayList<Integer> testSequence = new ArrayList<Integer>();
    DynamicGroupBox mainBox = new DynamicGroupBox(
        new DynamicStringField("f1", "First Name"),
        new DynamicOkButton(),
        new DynamicCancelButton());
    final DynamicForm form = new DynamicForm("Form1", mainBox);
    form.setModal(modal);
    form.setDisplayHint(displayHint);
    form.setDisplayViewId(viewId);

    testSequence.add(0);

    IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        testSequence.add(3);
        form.doClose();
        testSequence.add(4);

        LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl(...).new ClientSyncJob() {...}.runVoid() finished");
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));

    LOG.debug("ClientSessionProvider.currentSession()");
    LOG.debug("ClientSessionProvider.currentSession().getDesktop()");

    testSequence.add(1);
    form.start(new FormHandler());
    testSequence.add(2);

    LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl() before waitFor");
    form.waitFor();

    LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl() after waitFor");
    testSequence.add(5);

    future.awaitDoneAndGet();
    ScoutAssert.assertOrder(new Integer[]{0, 1, 2, 3, 4, 5}, testSequence.toArray());
  }
}
