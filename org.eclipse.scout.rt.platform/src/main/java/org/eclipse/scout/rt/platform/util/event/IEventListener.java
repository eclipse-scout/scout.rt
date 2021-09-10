/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.event;

import java.util.EventObject;

/**
 * Represents an action that is triggered by an {@link IEventEmitter} when an event is fired.
 *
 * @param <EVENT>
 *          Event type that is fired.
 * @since 22.0
 */
@FunctionalInterface
public interface IEventListener<EVENT extends EventObject> {

  void fireEvent(EVENT event);
}
