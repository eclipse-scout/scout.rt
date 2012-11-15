/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.service.IService;

/**
 * Convenience for creating temporary progress monitors. This is accomplished by
 * creating a virtual Job that represents the progress monitor. The progress is
 * discarded be either calling {@link IProgressMonitor#done()} or {@link #removeProgress(ISimpleProgress)}
 */
@Priority(-3)
public interface ISimpleProgressService extends IService {

  /**
   * can be called from outside model thread
   */
  ISimpleProgress addProgress(String name);

  /**
   * can be called from outside model thread
   */
  void removeProgress(ISimpleProgress progress);

}
