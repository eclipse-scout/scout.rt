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
package org.eclipse.scout.rt.ui.rap.ext.tabfolder;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

/**
 * <h3>CTabFolderEx</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 *        XXX remove when bug 342250 is fixed.
 */
public class CTabItemEx extends CTabItem {
  private static final long serialVersionUID = 1L;

  private boolean m_tabItemVisible;

  public CTabItemEx(CTabFolder parent, int style) {
    super(parent, style);
  }

  @Override
  protected void checkSubclass() {

  }

  @Override
  public boolean isShowing() {
    return m_tabItemVisible && super.isShowing();
  }

  /**
   * @param tabItemVisible
   *          the tabItemVisible to set
   */
  public void setTabItemVisible(boolean tabItemVisible) {
    m_tabItemVisible = tabItemVisible;
  }

  /**
   * @return the tabItemVisible
   */
  public boolean isTabItemVisible() {
    return m_tabItemVisible;
  }

}
