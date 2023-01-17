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

import java.util.ArrayList;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.job.IFuture;
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
  public void testStartAndWait_Dialog() {
    testStartAndWaitImpl(true, IForm.DISPLAY_HINT_DIALOG, null);
  }

  @Test
  public void testStartAndWait_View() {
    testStartAndWaitImpl(true, IForm.DISPLAY_HINT_DIALOG, null);
  }

  protected void testStartAndWaitImpl(final boolean modal, final int displayHint, final String viewId) {
    final ArrayList<Integer> testSequence = new ArrayList<>();
    DynamicGroupBox mainBox = new DynamicGroupBox(
        new DynamicStringField("f1", "First Name"),
        new DynamicOkButton(),
        new DynamicCancelButton());
    final DynamicForm form = new DynamicForm("Form1", mainBox);
    form.setModal(modal);
    form.setDisplayHint(displayHint);
    form.setDisplayViewId(viewId);

    testSequence.add(0);

    IFuture<Void> future = ModelJobs.schedule(() -> {
      testSequence.add(3);
      form.doClose();
      testSequence.add(4);

      LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl(...).new ClientSyncJob() {...}.runVoid() finished");
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
