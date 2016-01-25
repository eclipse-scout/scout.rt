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
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.AbstractEventHistory;

/**
 * The default implementation is created in {@link AbstractForm#createEventHistory()} and tracks
 * <ul>
 * <li>{@link FormEvent#TYPE_REQUEST_FOCUS}</li>
 * <li>{@link FormEvent#TYPE_TO_BACK}</li>
 * <li>{@link FormEvent#TYPE_TO_FRONT}</li>
 * </ul>
 * <p>
 * This object is thread safe.
 *
 * @since 3.8
 */
public class DefaultFormEventHistory extends AbstractEventHistory<FormEvent> {

  /**
   * @param timeToLiveMillis
   */
  public DefaultFormEventHistory(long timeToLiveMillis) {
    super(timeToLiveMillis);
  }

  @Override
  public void notifyEvent(FormEvent event) {
    switch (event.getType()) {
      case FormEvent.TYPE_REQUEST_FOCUS:
      case FormEvent.TYPE_TO_BACK:
      case FormEvent.TYPE_TO_FRONT: {
        addToCache(event.getType(), event);
        break;
      }
    }
  }
}
