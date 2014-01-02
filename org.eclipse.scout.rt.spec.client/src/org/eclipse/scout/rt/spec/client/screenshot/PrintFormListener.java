/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.screenshot;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * A listener that
 * <ul>
 * <li>schedules a print job when a form is activated</li>
 * <li>closes the form when it is printed</li>
 * <ul>
 */
public class PrintFormListener implements FormListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PrintFormListener.class);

  /** All scout objects that need to be printed */
  private final Queue<Object> m_printQueue = new LinkedBlockingDeque<Object>();

  private final FormPrinter m_formPrinter;

  /**
   *
   */
  public PrintFormListener(FormPrinter formPrinter) {
    m_formPrinter = formPrinter;
  }

  @Override
  public void formChanged(FormEvent e) throws ProcessingException {
    if (e.getType() == FormEvent.TYPE_ACTIVATED) {
      enqueueFields(e.getForm());
      scheduleNextPrintJob();
    }
    else if (e.getType() == FormEvent.TYPE_PRINTED) {
      if (m_printQueue.isEmpty()) {
        IForm form = e.getForm();
        LOG.info("Closing form : {}", form);
        form.doClose();
      }
      else {
        scheduleNextPrintJob();
      }
    }
  }

  /**
   * Schedules a job for a scout form or field
   */
  private void scheduleNextPrintJob() {
    new ClientSyncJob("Printing", ClientSyncJob.getCurrentSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) {
        Object next = m_printQueue.remove();
        m_formPrinter.print(next);
      }
    }.schedule();
  }

  /**
   * Adds form and fields to print queues.
   * <p>
   * Collects hidden tab boxes (not selected) to be printed, because they do not appear on the print of the form, and
   * add them to the print queue.
   * </p>
   * 
   * @param form
   *          form containing tab boxes
   */
  private void enqueueFields(IForm form) {
    Map<File, Object> printObjects = m_formPrinter.getPrintObjects(form);
    for (Entry<File, Object> e : printObjects.entrySet()) {
      m_printQueue.add(e.getValue());
      LOG.info("Adding files to print: {}", e.getKey().getName());
    }
  }

}
