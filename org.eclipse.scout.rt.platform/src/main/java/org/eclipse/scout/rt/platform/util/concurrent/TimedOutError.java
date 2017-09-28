/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Indicates that the maximal wait time elapsed while waiting for some condition to become <code>true</code>, e.g. while
 * waiting a job to complete.
 *
 * @since 6.0
 */
public class TimedOutError extends AbstractInterruptionError {

  private static final long serialVersionUID = 1L;

  public TimedOutError(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public TimedOutError withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
