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
package org.eclipse.scout.rt.client.ui.basic.tree;

import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;

/**
 * A buffer for table events ({@link TreeEvent}s) with coalesce functionality:
 * <p>
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * </p>
 * Not thread safe, to be accessed in client model job.
 *
 * @param T
 *          event type
 */
public class TreeEventBuffer extends AbstractEventBuffer<TreeEvent> {

  @Override
  protected List<TreeEvent> coalesce(List<TreeEvent> events) {
    //traverse the list in reversed order
    //previous events may be deleted from the list
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;

      TreeEvent event = events.get(i);
      int type = event.getType();

      //remove all previous events of the same type
      if (isIgnorePrevious(type)) {
        remove(type, events.subList(0, i));
      }
    }
    return events;
  }

  /**
   * @param type
   *          {@link TreeEvent} type
   * @return <code>true</code>, if previous events of the same type can be ignored. <code>false</code> otherwise
   */
  protected boolean isIgnorePrevious(int type) {
    switch (type) {
      case TreeEvent.TYPE_NODES_SELECTED:
      case TreeEvent.TYPE_BEFORE_NODES_SELECTED:
      case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
        return true;
      }
      default: {
        return false;
      }
    }
  }
}
