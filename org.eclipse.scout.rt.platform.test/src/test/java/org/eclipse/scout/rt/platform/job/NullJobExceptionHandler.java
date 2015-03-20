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
package org.eclipse.scout.rt.platform.job;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * Handler used to run job tests to not spam the log output.
 */
@Priority(10)
public class NullJobExceptionHandler extends JobExceptionHandler {

  @Override
  public void handleException(final JobInput job, final Throwable t) {
    // NOOP
  }
}
