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
package org.eclipse.scout.rt.ui.swing.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Simplified and generalized version of Swings TransferHandler
 * <p>
 * There is unfortunately no interface or empty basic variant in the swing library
 * <p>
 * This class eliminates the direct component-drag/drop relationship and allows for MVC handling
 * <p>
 * Override {@link #canDrag()}, {@link #createTransferable(JComponent)} and
 * {@link #importDataEx(JComponent, Transferable, Point)}
 */
public class TransferHandlerEx extends TransferHandler {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TransferHandlerEx.class);

  /**
   * Drag
   */
  @Override
  public int getSourceActions(JComponent c) {
    if (canDrag()) {
      return DnDConstants.ACTION_COPY;
    }
    else {
      return DnDConstants.ACTION_NONE;
    }
  }

  /**
   * override and implement
   */
  protected boolean canDrag() {
    return false;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    return null;
  }

  /**
   * Drop
   */
  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    return true;
  }

  @Override
  public final boolean importData(JComponent comp, Transferable t) {
    return importDataEx(comp, t, null);
  }

  public boolean importDataEx(JComponent comp, Transferable t, Point location) {
    return false;
  }

  // used only for MOVE
  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    super.exportDone(source, data, action);
  }

  @Override
  public Icon getVisualRepresentation(Transferable t) {
    return null;
  }

}
