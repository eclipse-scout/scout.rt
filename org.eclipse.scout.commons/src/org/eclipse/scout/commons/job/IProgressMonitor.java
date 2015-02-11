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
 * Monitor to track the progress of an activity.
 *
 * @since 5.0
 */
public interface IProgressMonitor {

  /**
   * @return <code>true</code> if this job was cancelled and the job should terminate its work.
   */
  boolean isCancelled();
}
