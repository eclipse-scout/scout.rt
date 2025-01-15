/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Handler to be invoked upon transition into 'done' state, which is either due to cancellation, or upon completion.
 *
 * @see IFuture#whenDone(IDoneHandler, RunContext)
 * @since 5.1
 */
@FunctionalInterface
public interface IDoneHandler<RESULT> {

  /**
   * Method invoked upon transition into 'done' state.
   */
  void onDone(DoneEvent<RESULT> event);
}
