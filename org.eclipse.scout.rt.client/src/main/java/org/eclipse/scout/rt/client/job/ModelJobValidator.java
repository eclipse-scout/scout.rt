/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Validator for {@link JobInput} used for model jobs.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ModelJobValidator {

  /**
   * Validates the given {@link RunContext} to be valid for model jobs.
   */
  public void validateRunContext(final RunContext runContext) {
    Assertions.assertTrue(runContext instanceof ClientRunContext, "A model job requires a ClientRunContext");
    Assertions.assertNotNull(((ClientRunContext) runContext).getSession(), "A model job requires a ClientSession in the ClientRunContext");
    Assertions.assertEquals(1, ((ClientRunContext) runContext).getSession().getModelJobSemaphore().getPermits(), "A model job requires a semaphore with permit size 1 for mutual exclusion");
  }

  /**
   * Validates the given {@link JobInput} to be valid for model jobs.
   */
  public void validateJobInput(final JobInput input) {
    Assertions.assertNotNull(input, "A model job requires a job input");
    validateRunContext(input.getRunContext());
    Assertions.assertSame(((ClientRunContext) input.getRunContext()).getSession().getModelJobSemaphore(), input.getExecutionSemaphore(), "A model job requires the session's model job semaphore");
  }
}
