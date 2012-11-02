/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench.util.listener;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;

/**
 * <h3>DndAdapter</h3>
 * 
 * @since 1.0.9 15.07.2008
 */
public abstract class DndAdapter implements DragSourceListener, DropTargetListener {
  private static final long serialVersionUID = 1L;

  @Override
  public void dragFinished(DragSourceEvent event) {
  }

  @Override
  public void dragSetData(DragSourceEvent event) {
  }

  @Override
  public void dragStart(DragSourceEvent event) {
  }

  @Override
  public void dragEnter(DropTargetEvent event) {
  }

  @Override
  public void dragLeave(DropTargetEvent event) {
  }

  @Override
  public void dragOperationChanged(DropTargetEvent event) {
  }

  @Override
  public void dragOver(DropTargetEvent event) {
  }

  @Override
  public void drop(DropTargetEvent event) {
  }

  @Override
  public void dropAccept(DropTargetEvent event) {
  }

}
