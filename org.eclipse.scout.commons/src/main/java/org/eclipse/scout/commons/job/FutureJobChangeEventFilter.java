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
package org.eclipse.scout.commons.job;

/**
 *
 */
public class FutureJobChangeEventFilter implements IJobChangeEventFilter {

  private final IFuture<?> m_future;

  public FutureJobChangeEventFilter(IFuture<?> future) {
    m_future = future;
  }

  @Override
  public boolean accept(IJobChangeEvent event) {
    return event.getFuture() == m_future;
  }

}
