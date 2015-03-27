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
 * Validator for {@link JobInput} used for client jobs.
 *
 * @since 5.1
 * @see JobInput
 * @see ClientRunContext
 */
@ApplicationScoped
public class ClientJobInputValidator {

  /**
   * Validates the given {@link JobInput} and {@link ClientRunContext}.
   */
  public void validate(final JobInput input) {
    Assertions.assertNotNull(input, "For client jobs, JobInput must not be null");
    Assertions.assertTrue(input.getRunContext() instanceof ClientRunContext, "For client jobs, RunContext must be of the type 'ClientRunContext'");
    Assertions.assertFalse(input.getMutex() instanceof IClientSession, "For client jobs, mutex object must not be the 'ClientSession' to not interfere with model jobs");
    input.getRunContext().validate();
  }
}
