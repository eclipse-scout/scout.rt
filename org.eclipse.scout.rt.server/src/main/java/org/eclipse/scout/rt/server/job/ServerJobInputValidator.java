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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.context.ServerRunContext;

/**
 * Validator for {@link JobInput} used for server jobs.
 *
 * @since 5.1
 * @see JobInput
 * @see ServerRunContext
 */
@ApplicationScoped
public class ServerJobInputValidator {

  /**
   * Validates the given {@link JobInput} and {@link ServerRunContext}.
   */
  public void validate(final JobInput input) {
    Assertions.assertNotNull(input, "For server jobs, 'JobInput' must not be null");
    Assertions.assertNotNull(input.runContext(), "For server jobs, 'RunContext' must not be null");
    Assertions.assertTrue(input.runContext() instanceof ServerRunContext, "For server jobs, 'RunContext' must be of the type 'ServerRunContext'");

    final ServerRunContext serverRunContext = (ServerRunContext) input.runContext();
    Assertions.assertNotNull(serverRunContext.session(), "For server jobs, 'serverSession' must not be null");
  }
}
