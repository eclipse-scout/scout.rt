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
package org.eclipse.scout.rt.client.ui.basic.tree;

import org.eclipse.scout.rt.client.ui.AbstractEventHistory;

/**
 * The default implementation is created in {@link AbstractTree#createEventHistory()} and tracks
 * <ul>
 * <li>{@link TreeEvent#TYPE_NODE_ENSURE_VISIBLE}</li>
 * <li>{@link TreeEvent#TYPE_NODE_REQUEST_FOCUS}</li>
 * <li>{@link TreeEvent#TYPE_REQUEST_FOCUS}</li>
 * <li>{@link TreeEvent#TYPE_SCROLL_TO_SELECTION}</li>
 * </ul>
 * <p>
 * This object is thread safe.
 * 
 * @since 3.8
 */
public class DefaultTreeEventHistory extends AbstractEventHistory<TreeEvent> {

  /**
   * @param timeToLiveMillis
   */
  public DefaultTreeEventHistory(long timeToLiveMillis) {
    super(timeToLiveMillis);
  }

  @Override
  public void notifyEvent(TreeEvent event) {
    switch (event.getType()) {
      case TreeEvent.TYPE_NODE_ENSURE_VISIBLE:
      case TreeEvent.TYPE_NODE_REQUEST_FOCUS:
      case TreeEvent.TYPE_REQUEST_FOCUS:
      case TreeEvent.TYPE_SCROLL_TO_SELECTION: {
        addToCache(event.getType(), event);
        break;
      }
    }
  }

}
