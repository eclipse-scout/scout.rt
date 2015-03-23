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
package org.eclipse.scout.rt.shared.job;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Provider used in 'shared' Plug-Ins to work on concrete job inputs, e.g. for lookup calls.
 */
@ApplicationScoped
public interface IJobInputProvider {

  /**
   * Creates a job input with a "snapshot" of the current calling context.
   */
  JobInput defaults();

  /**
   * Creates an empty job input.
   */
  JobInput empty();
}
