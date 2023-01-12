/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEventBuffer;

/**
 * A buffer for outline events ({@link OutlineEvent}s and {@link TreeEvent}s)
 */
public class OutlineEventBuffer extends TreeEventBuffer {

  @Override
  protected boolean isNodesRequired(int type) {
    if (type == OutlineEvent.TYPE_PAGE_CHANGED) {
      return true;
    }
    return super.isNodesRequired(type);
  }
}
