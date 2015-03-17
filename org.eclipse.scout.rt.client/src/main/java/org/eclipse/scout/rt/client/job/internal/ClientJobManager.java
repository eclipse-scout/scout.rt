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

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Default implementation of {@link IClientJobManager}.
 *
 * @see IClientJobManager
 * @since 5.1
 */
@ApplicationScoped
public class ClientJobManager extends JobManager<ClientJobInput> implements IClientJobManager {

  public ClientJobManager() {
    super("scout-client-thread");
  }

  @Override
  protected ClientJobInput createDefaultJobInput() {
    return ClientJobInput.defaults();
  }

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final ClientJobInput input) {
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, (input.getSession() != null ? input.getSession().getTexts() : ScoutTexts.CURRENT.get()));
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, UserAgent.CURRENT, input.getUserAgent());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, ISession.CURRENT, input.getSession());
    final Callable<RESULT> c1 = super.interceptCallable(c2, input);

    return c1;
  }
}
