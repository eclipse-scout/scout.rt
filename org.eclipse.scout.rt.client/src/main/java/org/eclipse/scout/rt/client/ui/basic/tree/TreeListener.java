/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.EventListener;
import java.util.List;

/**
 * @see TreeAdapter
 */
@FunctionalInterface
public interface TreeListener extends EventListener {

  void treeChanged(TreeEvent e);

  /**
   * batch event for fast processing of batch changes
   *
   * @deprecated in order for better performance the most efficient way is to only register for the events that are
   *             really handled. see the vararg parameter to {@link ITree#addTreeListener(TreeListener, int...)}
   *             <p>
   *             This method will be removed in 9.x
   */
  @Deprecated
  default void treeChangedBatch(List<? extends TreeEvent> events) {
    for (TreeEvent event : events) {
      treeChanged(event);
    }
  }
}
