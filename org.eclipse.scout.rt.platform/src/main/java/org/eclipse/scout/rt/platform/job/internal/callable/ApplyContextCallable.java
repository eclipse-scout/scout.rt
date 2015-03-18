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
package org.eclipse.scout.rt.platform.job.internal.callable;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.context.Context;

/**
 * Processor to run the subsequent sequence of actions on behalf of the given {@link Context}.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ApplyContextCallable<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

  private final Callable<RESULT> m_next;
  private final Context m_context;

  public ApplyContextCallable(final Callable<RESULT> next, final Context context) {
    m_next = Assertions.assertNotNull(next);
    m_context = Assertions.assertNotNull(context);
  }

  @Override
  public RESULT call() throws Exception {
    return m_context.invoke(m_next);
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
