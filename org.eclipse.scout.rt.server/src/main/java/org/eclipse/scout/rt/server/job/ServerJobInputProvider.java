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
package org.eclipse.scout.rt.server.job;

import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.job.IJobInputProvider;

/**
 * Provides a {@link ServerJobInput}.
 */
public class ServerJobInputProvider implements IJobInputProvider {

  @Override
  public JobInput defaults() {
    return ServerJobInput.defaults();
  }

  @Override
  public JobInput empty() {
    return ServerJobInput.empty();
  }
}
