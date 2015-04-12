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

/**
 * Callback to be notified once a job completes.
 *
 * @since 5.1
 * @see IFuture#whenDone(IDoneCallback)
 */
public interface IDoneCallback<RESULT> {

  /**
   * Method invoked once a job completes.
   *
   * @param event
   *          event with completion information.
   */
  void onDone(DoneEvent<RESULT> event);
}
