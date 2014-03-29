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
package org.eclipse.scout.rt.ui.json.tree;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.ui.json.AbstractEventFilter;

public class TreeEventFilter extends AbstractEventFilter<TreeEvent> {

  private ITree m_source;

  public TreeEventFilter(ITree source) {
    m_source = source;
  }

  /**
   * Computes whether the event should be returned to the GUI. There are three cases:
   * <ul>
   * <li>No filtering happens: The original event is returned. <br>
   * This is the case if {@link #m_ignorableModelEvents} does not contain an event with the same type as the original
   * event.</li>
   * <li>Partial filtering happens: A new event with a subset of tree nodes is returned.<br>
   * This is the case if the {@link #m_ignorableModelEvents} contains a relevant event but has different nodes than the
   * original event.
   * <li>Complete filtering happens: Null is returned.<br>
   * This is the case if the event should be filtered for every node in the original event
   */
  @Override
  public TreeEvent filterIgnorableModelEvent(TreeEvent event) {
    for (TreeEvent eventToIgnore : getIgnorableModelEvents()) {
      if (eventToIgnore.getType() == event.getType()) {
        Collection<ITreeNode> nodes = new ArrayList<>(event.getNodes());
        nodes.removeAll(eventToIgnore.getNodes());
        if (nodes.size() == 0) {
          //Event should be ignored if no nodes remain or if the event contained no nodes at all
          return null;
        }

        TreeEvent newEvent = new TreeEvent(m_source, event.getType(), event.getCommonParentNode(), nodes);
        return newEvent;
      }
    }
    return event;
  }
}
