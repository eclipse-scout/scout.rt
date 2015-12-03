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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.AbstractEventHistory;

/**
 * The default implementation is created in {@link AbstractTable#createEventHistory()} and tracks
 * <ul>
 * <li>{@link TableEvent#TYPE_REQUEST_FOCUS}</li>
 * <li>{@link TableEvent#TYPE_REQUEST_FOCUS_IN_CELL}</li>
 * <li>{@link TableEvent#TYPE_SCROLL_TO_SELECTION}</li>
 * </ul>
 * <p>
 * This object is thread safe.
 * 
 * @since 3.8
 */
public class DefaultTableEventHistory extends AbstractEventHistory<TableEvent> {

  /**
   * @param timeToLiveMillis
   */
  public DefaultTableEventHistory(long timeToLiveMillis) {
    super(timeToLiveMillis);
  }

  @Override
  public void notifyEvent(TableEvent event) {
    switch (event.getType()) {
      case TableEvent.TYPE_REQUEST_FOCUS:
      case TableEvent.TYPE_REQUEST_FOCUS_IN_CELL:
      case TableEvent.TYPE_SCROLL_TO_SELECTION: {
        addToCache(event.getType(), event);
        break;
      }
    }
  }

}
