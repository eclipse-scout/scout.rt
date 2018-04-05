/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.event;

import java.util.List;

/**
 * Thread-safe event listener list of one listener type
 *
 * @since 8.0
 */
public interface IFastListenerList<LISTENER> {

  boolean isEmpty();

  void add(LISTENER listener);

  void add(LISTENER listener, boolean weak);

  void remove(LISTENER listener);

  List<LISTENER> list();
}
