/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;

public class OutlineEvent extends TreeEvent {

  /**
   * A page has changed (for instance visibility of detailForm or detailTable).
   */
  public static final int TYPE_PAGE_CHANGED = 1001;

  public static final int TYPE_PAGE_BEFORE_DATA_LOADED = 1002;

  /**
   * Fired after the page's child data is loaded. Note that this event is also fired when the loading fails with an
   * exception. Event consumers can't rely on a valid state of the loaded data.
   */
  public static final int TYPE_PAGE_AFTER_DATA_LOADED = 1003;

  public static final int TYPE_PAGE_AFTER_TABLE_INIT = 1004;

  public static final int TYPE_PAGE_AFTER_PAGE_INIT = 1005;

  public static final int TYPE_PAGE_AFTER_SEARCH_FORM_START = 1006;

  public static final int TYPE_PAGE_AFTER_DISPOSE = 1007;

  public static final int TYPE_PAGE_ACTIVATED = 1008;

  private static final long serialVersionUID = 1L;

  private boolean m_buffered;

  public OutlineEvent(ITree source, int type, boolean buffered) {
    super(source, type);
    m_buffered = buffered;
  }

  public OutlineEvent(ITree source, int type, ITreeNode node, boolean buffered) {
    super(source, type, node);
    m_buffered = buffered;
  }

  public boolean isBuffered() {
    return m_buffered;
  }

  public void setBuffered(boolean buffered) {
    m_buffered = buffered;
  }

}
