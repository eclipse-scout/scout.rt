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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Validator for {@link JobInput} used for jobs.
 *
 * @since 5.1
 * @see JobInput
 * @see RunContext
 */
@ApplicationScoped
public class JobInputValidator {

  /**
   * Validates the given {@link JobInput} and {@link ClientRunContext}.
   */
  public void validate(final JobInput input) {
    Assertions.assertNotNull(input, "'JobInput' must not be null");

    if (input.runContext() != null) {
      Assertions.assertNotNull(input.runContext().runMonitor(), "'RunMonitor' on 'RunContext' must not be null");
    }
  }
}
