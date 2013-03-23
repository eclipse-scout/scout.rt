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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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

/**
 * Bugzilla 352472 - Possible Deadlock at IForm#waitFor
 */
@RunWith(ScoutClientTestRunner.class)
public class FormWaitForTest {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormWaitForTest.class);

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
        new DynamicCancelButton()
        );
    final DynamicForm form = new DynamicForm("Form1", mainBox);
    form.setModal(modal);
    form.setDisplayHint(displayHint);
    form.setDisplayViewId(viewId);

    testSequence.add(0);

    ClientSyncJob closeJob = new ClientSyncJob("Close", ClientSyncJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        testSequence.add(3);
        form.doClose();
        testSequence.add(4);

        LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl(...).new ClientSyncJob() {...}.runVoid() finished");
      }
    };
    closeJob.schedule();

    LOG.debug("ClientSyncJob.getCurrentSession()");
    LOG.debug("ClientSyncJob.getCurrentSession().getDesktop()");

    testSequence.add(1);
    form.start(new FormHandler());
    testSequence.add(2);

    LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl() before waitFor");
    form.waitFor();

    LOG.debug("ClientSyncWaitForTest.testStartAndWaitImpl() after waitFor");
    testSequence.add(5);

    closeJob.join();
    ScoutAssert.assertOrder(new Integer[]{0, 1, 2, 3, 4, 5}, testSequence.toArray());
  }
}
