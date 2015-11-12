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
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Validator for {@link JobInput} used for server jobs.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ServerJobInputValidator {

  /**
   * Validates the given {@link JobInput} and {@link ServerRunContext}.
   */
  public void validate(final JobInput input) {
    Assertions.assertNotNull(input, "ServerJob requires a 'JobInput'");
    Assertions.assertTrue(input.getRunContext() instanceof ServerRunContext, "ServerJob requires a 'ServerRunContext'");
    Assertions.assertEqual(TransactionScope.REQUIRES_NEW, ((ServerRunContext) input.getRunContext()).getTransactionScope(), "ServerJob requires the transaction scope 'REQUIRES_NEW'");
  }
}
