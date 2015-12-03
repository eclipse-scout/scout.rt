/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

  private static final long serialVersionUID = 1L;

  public OutlineEvent(ITree source, int type) {
    super(source, type);
  }

  public OutlineEvent(ITree source, int type, ITreeNode node) {
    super(source, type, node);
  }

}
