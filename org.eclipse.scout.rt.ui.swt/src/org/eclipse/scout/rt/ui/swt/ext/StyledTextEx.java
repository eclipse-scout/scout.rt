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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
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
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * <h3>StyledTextEx</h3> ...
 * 
 * @since 1.0.0 15.04.2008
 */
public class StyledTextEx extends StyledText {
  private Listener m_traversHandlingListener = new P_TraverseHandlingListener();
  private Menu m_copyPasteMenu;
  private MenuItem m_cutItem;
  private MenuItem m_copyItem;
  private MenuItem m_pasteItem;

  public StyledTextEx(Composite parent, int style) {
    super(parent, style);
    if ((style & SWT.V_SCROLL) != 0) {
      addListener(SWT.Modify, m_traversHandlingListener);
      updateVerticalScrollbarVisibility();
    }
    if ((style & SWT.MULTI) != 0) {
      attachListeners();
    }

    m_copyPasteMenu = new Menu(getShell(), SWT.POP_UP);

    m_copyPasteMenu.addMenuListener(new MenuListener() {
      public void menuHidden(MenuEvent e) {
      }

      public void menuShown(MenuEvent e) {
        if (isEnabled()) {
          m_cutItem.setEnabled(StringUtility.hasText(getSelectionText()));
          m_copyItem.setEnabled(StringUtility.hasText(getSelectionText()));
        }
      }
    });

    m_cutItem = new MenuItem(m_copyPasteMenu, SWT.PUSH);
    m_cutItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        cut();
      }
    });
    m_cutItem.setText(ScoutTexts.get("Cut"));

    m_copyItem = new MenuItem(m_copyPasteMenu, SWT.PUSH);
    m_copyItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (isEnabled()) {
          copy();
        }
        else {
          //Ticket 86'427: Kopieren - Einfügen
          boolean hasSelection = StringUtility.hasText(getSelectionText());
          if (hasSelection) {
            copy();
          }
          else {
            setSelection(0, getText().length());
            copy();
            setCaretOffset(0);
          }
        }
      }
    });
    m_copyItem.setText(ScoutTexts.get("Copy"));

    m_pasteItem = new MenuItem(m_copyPasteMenu, SWT.PUSH);
    m_pasteItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        paste();
      }
    });
    m_pasteItem.setText(ScoutTexts.get("Paste"));

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

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (!isDisposed()) {
      if (enabled) {
        m_cutItem.setEnabled(true);
        m_copyItem.setEnabled(true);
        m_pasteItem.setEnabled(true);
        setMenu(m_copyPasteMenu);
      }
      else {
        setMenu(null);
      }
    }

    Composite parent = getParent();
    if (parent != null && !parent.isDisposed()) {
      if (enabled) {
        parent.setMenu(null);
      }
      else {
        m_cutItem.setEnabled(false);
        m_copyItem.setEnabled(true);
        m_pasteItem.setEnabled(false);
        parent.setMenu(m_copyPasteMenu);
      }
    }
  }

  protected void attachListeners() {
    addListener(SWT.Traverse, m_traversHandlingListener);
    addListener(SWT.Verify, m_traversHandlingListener);
  }

  protected void dettachListeners() {
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
    Point size = computeSize(clientArea.width, SWT.DEFAULT);
    ScrollBar vBar = getVerticalBar();
    if (vBar != null && !vBar.isDisposed()) {
      vBar.setVisible(size.y > clientArea.height);
    }
  }

  private class P_TraverseHandlingListener implements Listener {
    private long m_timestamp;

    public void handleEvent(Event event) {
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
          if (event.text.equals("\t") && event.stateMask == 0) {
            m_timestamp = event.time;
            event.doit = false;
            traverse(SWT.TRAVERSE_TAB_NEXT);
          }
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
