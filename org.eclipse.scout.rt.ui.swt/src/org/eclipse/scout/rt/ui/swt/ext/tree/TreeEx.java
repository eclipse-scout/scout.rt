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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

public class TreeEx extends Tree {

  private static final String BACKUPED_MENU = "backupedMenu";
  private boolean m_readOnly = false;

  public TreeEx(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
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
