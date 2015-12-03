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
package org.eclipse.scout.rt.platform.job.internal;

/**
 * Callback to be notified once the mutex is acquired.
 *
 * @since 5.2
 */
public interface IMutexAcquiredCallback {

  /**
   * Method invoked once the mutex is acquired, and is invoked from the thread passing the mutex. Hence, the implementor
   * should execute any long running operation asynchronously in another thread. Also, the implementor is responsible
   * for passing the mutex to the next waiting task if not needed anymore.
   */
  void onMutexAcquired();
}
