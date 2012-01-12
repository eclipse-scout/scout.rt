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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

/**
 * Utilities to handle busy and blocking
 * 
 * @author imo
 * @since 3.8
 */
public final class SwtBusyUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBusyUtility.class);

  private SwtBusyUtility() {
  }

  public static void showBusyIndicator(final Display display, final IRunnableWithProgress runnable, final IProgressMonitor monitor) {
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        BusyIndicator.showWhile(display, new Runnable() {
          @Override
          public void run() {
            try {
              //use modal context to prevent freezing of the gui
              ModalContext.run(runnable, true, monitor, display);
            }
            catch (Throwable t) {
              LOG.warn("run modal context", t);
            }
          }
        });
      }
    });
  }

  public static boolean isDialogPart(ISwtScoutPart part) {
    return (part instanceof SwtScoutDialog);
  }

  public static boolean isViewOrEditorPart(ISwtScoutPart part) {
    return (part instanceof AbstractScoutView || part instanceof AbstractScoutEditorPart);
  }

  /**
   * @return all affected parts in the same swt / scout environment (user session). The first part is the active part
   *         and may be null.
   */
  public static List<ISwtScoutPart> findAffectedParts(ISwtEnvironment env) {
    ArrayList<ISwtScoutPart> candidateParts = new ArrayList<ISwtScoutPart>();
    for (ISwtScoutPart part : env.getOpenFormParts()) {
      if (SwtBusyUtility.isDialogPart(part) || SwtBusyUtility.isViewOrEditorPart(part)) {
        candidateParts.add(part);
      }
    }
    ArrayList<ISwtScoutPart> affectedParts = new ArrayList<ISwtScoutPart>();
    //find an active dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find a visible dialog, it would be the only affected item
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isDialogPart(part)) {
        affectedParts.add(part);
        return affectedParts;
      }
    }
    //find an active view, also all other views are affected
    for (ISwtScoutPart part : candidateParts) {
      if (part.isActive() && SwtBusyUtility.isViewOrEditorPart(part)) {
        affectedParts.addAll(candidateParts);
        affectedParts.remove(part);
        affectedParts.add(0, part);
        return affectedParts;
      }
    }
    //all views are affected, none shows a cancel button
    affectedParts.add(null);
    affectedParts.addAll(candidateParts);
    return affectedParts;
  }

}
