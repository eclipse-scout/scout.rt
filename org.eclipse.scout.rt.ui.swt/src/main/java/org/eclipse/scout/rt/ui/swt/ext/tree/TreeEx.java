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
package org.eclipse.scout.rt.ui.swt.ext.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeEx extends Tree {

  private static final String BACKUPED_MENU = "backupedMenu";
  private boolean m_readOnly = false;
  private TreeItem m_contextItem;

  public TreeEx(Composite parent, int style) {
    super(parent, style);
    addListener(SWT.MouseDown, new Listener() {
      @Override
      public void handleEvent(Event event) {
        m_contextItem = getItem(new Point(event.x, event.y));
      }
    });
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  /**
   * @return the contextItem
   */
  public TreeItem getContextItem() {
    return m_contextItem;
  }

  public void setReadOnly(boolean readOnly) {
    if (m_readOnly != readOnly) {
      m_readOnly = readOnly;
      if (readOnly) {
        setData(BACKUPED_MENU, getMenu());
      }
      else {
        setMenu((Menu) getData(BACKUPED_MENU));
      }
    }
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

}
