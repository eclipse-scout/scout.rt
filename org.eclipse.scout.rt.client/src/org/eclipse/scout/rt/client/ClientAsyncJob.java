/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

/**
 * <p>
 * Convenience for a job operating on a {@link IClientSession} which is not synchronized and does not block other job
 * operating on the same session.
 * </p>
 * <p>
 * Normally this kind of job is used for read-only ui operations in the background, be careful when performing
 * concurrent and parallel processing on a session.
 * </p>
 * see {@link ClientJob}
 */
public class ClientAsyncJob extends ClientJob {

  /**
   * see {@link ClientJob#ClientJob(String, IClientSession, boolean)}
   */
  public ClientAsyncJob(String name, IClientSession session) {
    this(name, session, true);
  }

  /**
   * see {@link ClientJob#ClientJob(String, IClientSession, boolean, boolean)}
   */
  public ClientAsyncJob(String name, IClientSession session, boolean system) {
    super(name, session, false, system);
  }

  /**
   * {@link ClientAsyncJob}s belong to the family of type {@link ClientJob}.class and {@link ClientAsyncJob}.class
   */
  @Override
  public boolean belongsTo(Object family) {
    if (super.belongsTo(family)) return true;
    if (family == ClientAsyncJob.class) {
      return true;
    }
    return false;
  }
}
