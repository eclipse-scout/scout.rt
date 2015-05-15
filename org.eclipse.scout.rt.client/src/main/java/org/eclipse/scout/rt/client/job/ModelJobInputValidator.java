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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Validator for {@link JobInput} used for model jobs.
 *
 * @since 5.1
 * @see JobInput
 * @see ClientRunContext
 */
@ApplicationScoped
public class ModelJobInputValidator {

  /**
   * Validates the given {@link JobInput} and {@link ClientRunContext}.
   */
  public void validate(final JobInput input) {
    Assertions.assertNotNull(input, "For model jobs, 'JobInput' must not be null");
    Assertions.assertNotNull(input.runContext(), "For model jobs, 'RunContext' must not be null");
    Assertions.assertTrue(input.runContext() instanceof ClientRunContext, "For model jobs, 'RunContext' must be of the type 'ClientRunContext'");

    final IClientSession clientSession = ((ClientRunContext) input.runContext()).session();
    Assertions.assertNotNull(clientSession, "For model jobs, 'clientSession' must not be null");
    Assertions.assertSame(clientSession, input.mutex(), "For model jobs, mutex object must be the 'clientSession'");

    Assertions.assertNotNull(input.runContext().runMonitor(), "For model jobs, 'RunMonitor' on 'RunContext' must not be null");
  }
}
