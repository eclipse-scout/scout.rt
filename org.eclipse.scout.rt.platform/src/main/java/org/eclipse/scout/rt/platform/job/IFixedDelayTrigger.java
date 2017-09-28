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
package org.eclipse.scout.rt.platform.job;

import org.quartz.Trigger;

/**
 * {@link Trigger} strategy to run a job periodically with a fixed delay between the termination of one execution and
 * the commencement of the next execution.
 *
 * @since 5.2
 */
public interface IFixedDelayTrigger extends Trigger {

  /**
   * Indicates to repeat infinitely, unless the end time of the trigger is reached.
   */
  long REPEAT_INDEFINITELY = -1;

  /**
   * Returns the maximal repetition count, after which it will be automatically deleted.
   */
  long getRepeatCount();

  /**
   * Returns the fixed delay between successive runs.
   */
  long getFixedDelay();
}
