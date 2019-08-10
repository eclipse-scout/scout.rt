/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.fixtures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.ui.html.fixtures.SessionStoreTestForm.MainBox.CloseButton;

/**
 * @since 5.2
 */
@ClassId("ee54fa42-9b5c-4a0e-9a76-6043c539da20")
public class SessionStoreTestForm extends AbstractForm {

  private final CloseAction m_closeAction;
  private final CountDownLatch m_doFinallyEntered;
  private final CountDownLatch m_doFinallyCompleted;

  public enum CloseAction {
    DO_NOTHING, WAIT_FOR_ANOTHER_FORM, WAIT_FOR_MESSAGE_BOX, WAIT_FOR_JOB, WAIT_FOR_LOOP;
  }

  public SessionStoreTestForm(CloseAction closeAction) {
    m_closeAction = closeAction;
    m_doFinallyEntered = new CountDownLatch(1);
    m_doFinallyCompleted = new CountDownLatch(1);
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public boolean isFinallyCompleted() {
    return m_doFinallyCompleted.getCount() == 0L;
  }

  public boolean awaitDoFinallyEntered(long timeout, TimeUnit unit) throws InterruptedException {
    return m_doFinallyEntered.await(timeout, unit);
  }

  public boolean awaitDoFinallyCompleted(long timeout, TimeUnit unit) throws InterruptedException {
    return m_doFinallyCompleted.await(timeout, unit);
  }

  @Override
  public void doFinally() {
    m_doFinallyEntered.countDown();
    System.out.println("# " + getClass().getSimpleName() + ".doFinally  " + m_closeAction + "...");
    switch (m_closeAction) {
      case WAIT_FOR_ANOTHER_FORM:
        SessionStoreTestForm form = new SessionStoreTestForm(CloseAction.DO_NOTHING);
        form.start();
        form.waitFor();
        break;
      case WAIT_FOR_MESSAGE_BOX:
        MessageBoxes
            .createOk()
            .withHeader("Session Shutdown Test")
            .show();
        break;
      case WAIT_FOR_JOB:
        Jobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            SleepUtil.sleepSafe(5, TimeUnit.SECONDS);
          }
        }, Jobs.newInput()).awaitDone();
        break;
      case WAIT_FOR_LOOP:
        long runUntil = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
        int counter = 0;
        while (System.currentTimeMillis() <= runUntil) {
          // do nothing
          if (counter % 100000000 == 0) {
            System.out.println("# for-loop in state " + IFuture.CURRENT.get().getState() + ", ttl=" + (runUntil - System.currentTimeMillis()));
          }
          counter++;
        }
        break;
      case DO_NOTHING:
      default:
        // do nothing
    }
    m_doFinallyCompleted.countDown();
    System.out.println("# " + getClass().getSimpleName() + ".doFinally done.");
  }

  @ClassId("2f7fcb35-be5a-458b-a4f0-263869aa6a55")
  public class MainBox extends AbstractGroupBox {

    @Order(1000)
    @ClassId("9cd4a8fc-5f7d-4c8e-bdb6-2e0e981e0ee0")
    public class CloseButton extends AbstractCloseButton {
    }
  }
}
