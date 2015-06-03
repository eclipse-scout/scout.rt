/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEventBuffer;

/**
 * A buffer for outline events ({@link OutlineEvent}s and {@link TreeEvent}s)
 */
public class OutlineEventBuffer extends TreeEventBuffer {

  @Override
  protected TreeEvent replaceNodesInEvent(TreeEvent event, Collection<ITreeNode> nodes) {
    if (event instanceof OutlineEvent) {
      return new OutlineEvent(event.getTree(), event.getType(), event.getNode());
    }
    return super.replaceNodesInEvent(event, nodes);
  }
}
