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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>TextEx</h3> Added disabled copy menu
 * 
 * @since 1.0.0 07.05.2008
 */
public class TextEx extends Text {

  private Menu m_copyPasteMenu;
  private MenuItem m_cutItem;
  private MenuItem m_copyItem;
  private MenuItem m_pasteItem;

  public TextEx(Composite parent, int style) {
    super(parent, style);

    m_copyPasteMenu = new Menu(getShell(), SWT.POP_UP);

    m_copyPasteMenu.addMenuListener(new MenuListener() {
      @Override
      public void menuHidden(MenuEvent e) {
      }

      @Override
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
            clearSelection();
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

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  @Override
  public boolean setFocus() {
    return getEditable();
  }
}
