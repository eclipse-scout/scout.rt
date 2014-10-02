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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.ITypeWithClassId;
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
public class PrintScreenshotsFormListener implements FormListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PrintScreenshotsFormListener.class);

  /** All scout objects that need to be printed */
  private final Queue<ITypeWithClassId> m_printQueue = new LinkedBlockingDeque<ITypeWithClassId>();

  private final FormScreenshotPrinter m_formPrinter;

  private final List<File> m_printedFiles = new ArrayList<File>();

  // reference to outer top level form, needed in case of embedded forms within a form
  private IForm m_topLevelForm;

  public PrintScreenshotsFormListener(FormScreenshotPrinter formPrinter) {
    m_formPrinter = formPrinter;
  }

  @Override
  public void formChanged(FormEvent e) throws ProcessingException {
    if (e.getType() == FormEvent.TYPE_ACTIVATED) {
      m_topLevelForm = e.getForm();
      enqueuePrintObjects(m_topLevelForm);
      scheduleNextPrintJob();
    }
    else if (e.getType() == FormEvent.TYPE_PRINTED) {
      // TODO ASA check if file exists and size > 0 (because FormEvent.TYPE_PRINTED is also thrown when an exception occurs)
      m_printedFiles.add(e.getPrintedFile());
      if (m_printQueue.isEmpty()) {
        LOG.info("Closing form : {}", m_topLevelForm);
        m_topLevelForm.doClose();
      }
      else {
        scheduleNextPrintJob();
      }
    }
  }

  /**
   * Schedules a job for a scout form or field
   */
  protected void scheduleNextPrintJob() {
    new ClientSyncJob("Printing", ClientSyncJob.getCurrentSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) {
        ITypeWithClassId next = m_printQueue.remove();
        m_formPrinter.print(next);
      }
    }.schedule();
  }

  /**
   * Adds form and fields to print queue.
   * <p>
   * Collects hidden tab boxes (not selected) to be printed, because they do not appear on the print of the form, and
   * add them to the print queue.
   * </p>
   * 
   * @param form
   *          form containing tab boxes
   */
  protected void enqueuePrintObjects(IForm form) {
    List<ITypeWithClassId> printObjects = m_formPrinter.getPrintObjects(form);
    for (ITypeWithClassId o : printObjects) {
      m_printQueue.add(o);
      LOG.info("Adding object to print: {}", o);
    }
  }

  public List<File> getPrintedFiles() {
    return m_printedFiles;
  }
}
