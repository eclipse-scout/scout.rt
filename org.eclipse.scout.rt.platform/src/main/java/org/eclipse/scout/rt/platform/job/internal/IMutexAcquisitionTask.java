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
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.commons.annotations.Internal;

/**
 * This task is used to compete for the mutex.
 *
 * @since 5.1
 */
@Internal
public interface IMutexAcquisitionTask {

  /**
   * Invoke to pass the mutex to this acquisition task.
   */
  void mutexAcquired();
}
