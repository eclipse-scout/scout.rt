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
package org.eclipse.scout.rt.ui.swt.busy;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class SwtBusyJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyJob.class);

  private ISwtScoutPart m_activePart;

  public SwtBusyJob(String name, SwtBusyHandler handler) {
    super(name, handler);
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    //calls runBusy, wait, then
    //calls runBlocking, wait
    return super.run(monitor);
  }

  /**
   * Show a wait cursor until long operation timeout
   */
  @Override
  protected void runBusy(final IProgressMonitor monitor) {
    final Display display = getBusyHandler().getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        //find active part
        m_activePart = findActivePartComposite();
        BusyIndicator.showWhile(display, new Runnable() {
          @Override
          public void run() {
            try {
              ModalContext.run(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
                  SwtBusyJob.super.runBusy(monitor2);
                }
              }, true, new NullProgressMonitor(), display);
            }
            catch (Exception e) {
              LOG.warn("run modal context", e);
            }
          }
        });
      }
    });
  }

  /**
   * Show a progress and a stop button in the active form parts header section.
   * <p>
   * Do not show a wait cursor.
   */
  @Override
  protected void runBlocking(IProgressMonitor monitor) {
    final Display display = getBusyHandler().getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        if (m_activePart == null) {
          return;
        }
        SwtScoutPartBusyDecorator deco = new SwtScoutPartBusyDecorator(m_activePart);
        try {
          final IProgressMonitor partMonitor = deco.attach();
          //
          try {
            ModalContext.run(new IRunnableWithProgress() {
              @Override
              public void run(IProgressMonitor monitor2) throws InvocationTargetException, InterruptedException {
                SwtBusyJob.super.runBlocking(monitor2);
              }
            }, true, partMonitor, display);
          }
          catch (Exception e) {
            LOG.warn("run modal context", e);
          }
        }
        finally {
          deco.detach();
        }
      }
    });
  }

  protected ISwtScoutPart findActivePartComposite() {
    Collection<ISwtScoutPart> parts = getBusyHandler().getSwtEnvironment().getOpenFormParts();
    //find dialog
    for (ISwtScoutPart part : parts) {
      if (part instanceof SwtScoutDialog && part.isActive()) {
        return part;
      }
    }
    //find view/editor
    for (ISwtScoutPart part : parts) {
      if ((part instanceof AbstractScoutView || part instanceof AbstractScoutEditorPart) && part.isActive()) {
        return part;
      }
    }
    return null;
  }

}
