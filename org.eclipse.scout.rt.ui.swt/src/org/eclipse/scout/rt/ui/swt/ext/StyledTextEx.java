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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.scout.rt.ui.swt.util.listener.DndAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * <h3>StyledTextEx</h3> ...
 * 
 * @since 1.0.0 15.04.2008
 */
public class StyledTextEx extends StyledText {
  private final Listener m_traversHandlingListener = new P_TraverseHandlingListener();

  public StyledTextEx(Composite parent, int style) {
    super(parent, style);
    if ((style & SWT.V_SCROLL) != 0) {
      addListener(SWT.Modify, m_traversHandlingListener);
      updateVerticalScrollbarVisibility();
    }
    if ((style & SWT.MULTI) != 0) {
      attachMultiLineListeners();
    }

    // DND
    P_DndListener dndListener = new P_DndListener();
    Transfer[] types = new Transfer[]{TextTransfer.getInstance()};
    int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
    DropTarget dropTarget = new DropTarget(this, operations);
    dropTarget.getTransfer();
    dropTarget.setTransfer(types);
    dropTarget.addDropListener(dndListener);
    DragSource dragSource = new DragSource(this, operations);
    dragSource.setTransfer(types);
    dragSource.addDragListener(dndListener);

    // Make sure that the menus are initially enabled
    setEnabled(true);
  }

  @Override
  protected void checkSubclass() {
  }

  protected void attachMultiLineListeners() {
    addListener(SWT.Traverse, m_traversHandlingListener);
    addListener(SWT.Verify, m_traversHandlingListener);
  }

  protected void dettachMultiLineListeners() {
    removeListener(SWT.Traverse, m_traversHandlingListener);
    removeListener(SWT.Verify, m_traversHandlingListener);
  }

  @Override
  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    updateVerticalScrollbarVisibility();
  }

  protected void updateVerticalScrollbarVisibility() {
    Rectangle clientArea = getClientArea();
    Point size = computeSize(clientArea.width, SWT.DEFAULT, false);
    ScrollBar vBar = getVerticalBar();
    if (vBar != null && !vBar.isDisposed()) {
      vBar.setVisible(size.y > clientArea.height);
    }
  }

  private class P_TraverseHandlingListener implements Listener {
    private long m_timestamp;

    @Override
    public void handleEvent(Event event) {
      if (isDisposed()) {
        return;
      }
      if (event.time == m_timestamp) {
        return;
      }
      switch (event.type) {
        case SWT.Traverse:
          if (event.keyCode == SWT.TAB && event.stateMask == SWT.CONTROL) {
            m_timestamp = event.time;
            event.doit = false;
          }
          break;
        case SWT.Verify: {
          // handle the tab key as traverse
          if (event.text.equals("\t") && event.stateMask == 0) {
            m_timestamp = event.time;
            event.doit = false;
            traverse(SWT.TRAVERSE_TAB_NEXT);
          }
          break;
        }
        case SWT.Modify: {
          updateVerticalScrollbarVisibility();
          break;
        }
        default:
          break;
      }
    }
  }

  private class P_DndListener extends DndAdapter {
    @Override
    public void drop(DropTargetEvent event) {
      if (getEnabled() && getEditable()
          && TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
        String text = (String) event.data;
        if (text != null) {
          insert(text);
        }
      }
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
      event.data = getSelectionText();
    }
  }
}
