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
 * A task of this type is notified if being rejected by the executor. This may occur when being scheduled and no more
 * threads or queue slots are available, or upon shutdown of the executor.
 *
 * @since 5.1
 */
@Internal
public interface IRejectable {

  /**
   * Method invoked if this task was rejected by the executor from being scheduled. When being invoked and this task is
   * a mutual exclusive task, it is the mutex owner.
   */
  void reject();
}
