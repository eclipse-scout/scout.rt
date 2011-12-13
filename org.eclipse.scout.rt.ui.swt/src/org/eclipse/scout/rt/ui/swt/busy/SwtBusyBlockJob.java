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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.BusyJob;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Default SWT busy handler for a {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public class SwtBusyBlockJob extends BusyJob {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyBlockJob.class);

  private final Collection<ISwtScoutPart> m_parts;

  public SwtBusyBlockJob(String name, SwtBusyHandler handler, Collection<ISwtScoutPart> parts) {
    super(name, handler);
    setSystem(true);
    m_parts = parts;
  }

  @Override
  protected SwtBusyHandler getBusyHandler() {
    return (SwtBusyHandler) super.getBusyHandler();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (getBusyHandler().isBusy()) {
      runBlocking(monitor);
    }
    return Status.OK_STATUS;
  }

  /**
   * Show a stop button in the active form parts header section and block all parts of the specific session
   * (environment).
   * <p>
   * Do not show a wait cursor anymore.
   */
  @Override
  protected void runBlocking(final IProgressMonitor monitor) {
    if (m_parts == null || m_parts.size() == 0) {
      return;
    }
    final ArrayList<SwtScoutPartBusyDecorator> decoList = new ArrayList<SwtScoutPartBusyDecorator>();
    final Display display = getBusyHandler().getDisplay();
    try {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          ISwtScoutPart ap = getActivePart(m_parts);
          for (ISwtScoutPart p : m_parts) {
            decoList.add(new SwtScoutPartBusyDecorator(p, p == ap));
          }
          for (SwtScoutPartBusyDecorator deco : decoList) {
            try {
              deco.attach(monitor);
            }
            catch (Exception e1) {
              LOG.warn("attach", e1);
            }
          }
        }
      });
      //
      SwtBusyBlockJob.super.runBlocking(monitor);
      //
    }
    finally {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          for (SwtScoutPartBusyDecorator deco : decoList) {
            try {
              deco.detach();
            }
            catch (Exception e1) {
              LOG.warn("detach", e1);
            }
          }
        }
      });
    }
  }

  protected ISwtScoutPart getActivePart(Collection<ISwtScoutPart> parts) {
    for (ISwtScoutPart part : parts) {
      if (part instanceof SwtScoutDialog && part.isActive()) {
        return part;
      }
    }
    for (ISwtScoutPart part : parts) {
      if ((part instanceof AbstractScoutView || part instanceof AbstractScoutEditorPart) && part.isActive()) {
        return part;
      }
    }
    return null;
  }

}
