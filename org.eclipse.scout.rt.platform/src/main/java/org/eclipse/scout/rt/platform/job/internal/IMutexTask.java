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

import java.util.concurrent.RunnableFuture;

import org.eclipse.scout.commons.annotations.Internal;

/**
 * A task that might be executed in mutually exclusive manner.
 *
 * @since 5.1
 */
@Internal
interface IMutexTask<RESULT> extends RunnableFuture<RESULT> {

  /**
   * Indicates whether this task is to be executed in mutually exclusive manner.
   *
   * @return <code>true</code> if this is a mutex task, meaning to be executed in sequence among tasks with the same
   *         mutex object.
   */
  boolean isMutexTask();

  /**
   * Returns the mutex object, or <code>null</code> if not being a mutually exclusive task.
   */
  Object getMutexObject();
}
