/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import java.util.EventListener;

/**
 * <h3>{@link IConfigChangedListener}</h3><br>
 * Interface for listeners that want to be notified if the state or value of a {@link IConfigProperty} changes.
 */
@FunctionalInterface
public interface IConfigChangedListener extends EventListener {
  /**
   * Callback indicating that the given event occurred.
   * 
   * @param event
   *          The event describing the change.
   */
  void configPropertyChanged(ConfigPropertyChangeEvent event);
}
