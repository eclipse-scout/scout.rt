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
package org.eclipse.scout.rt.platform.job;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Represents a mutex object to achieve mutual exclusion among jobs of the same mutex object. Mutual exclusion means,
 * that at any given time, there is only one job active for the same mutex.
 *
 * @since 5.2
 */
@Bean
public interface IMutex {

  /**
   * Returns whether the given task is the current mutex owner.
   */
  boolean isMutexOwner(IFuture<?> task);

  /**
   * Returns the number of tasks currently competing for the mutex. That is the task currently owning the mutex, plus
   * all tasks waiting for the mutex to become available.
   */
  int getCompetitorCount();
}
