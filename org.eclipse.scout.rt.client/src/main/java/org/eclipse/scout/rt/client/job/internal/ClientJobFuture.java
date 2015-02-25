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
package org.eclipse.scout.rt.client.job.internal;

import java.util.concurrent.RunnableScheduledFuture;

import org.eclipse.scout.commons.job.internal.JobFuture;
import org.eclipse.scout.rt.client.job.ClientJobInput;

/**
 * {@link RunnableScheduledFuture} representing a task associated with a {@link ClientJobInput}.
 *
 * @see RunnableScheduledFuture
 * @see ClientJobManager
 * @since 5.1
 */
public class ClientJobFuture<RESULT> extends JobFuture<RESULT> {

  public ClientJobFuture(final RunnableScheduledFuture<RESULT> delegate, final ClientJobInput input) {
    super(delegate, input);
  }

  @Override
  public ClientJobInput getInput() {
    return (ClientJobInput) super.getInput();
  }
}
