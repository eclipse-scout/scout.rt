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
 * Visitor for visiting jobs.
 *
 * @since 5.0
 */
public interface IJobVisitor {

  /**
   * Is called upon visiting a job.
   *
   * @return <code>true</code>=continue visiting, <code>false</code>=end visiting
   */
  boolean visit(IJob<?> job);
}
