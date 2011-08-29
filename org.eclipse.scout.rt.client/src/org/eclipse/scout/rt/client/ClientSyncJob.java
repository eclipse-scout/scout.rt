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
 * Convenience for job operating on a {@link IClientSession} which blocks all
 * other ClientJobs operating on the same session. see {@link ClientJob}
 */
public class ClientSyncJob extends ClientJob {

  /**
   * see {@link ClientJob#ClientJob(String, IClientSession, boolean)}
   */
  public ClientSyncJob(String name, IClientSession session) {
    this(name, session, true);
  }

  /**
   * see {@link ClientJob#ClientJob(String, IClientSession, boolean, boolean)}
   */
  public ClientSyncJob(String name, IClientSession session, boolean system) {
    super(name, session, true, system);
  }

  /**
   * {@link ClientSyncJob}s belong to the family of type {@link ClientJob}.class and {@link ClientSyncJob}.class
   */
  @Override
  public boolean belongsTo(Object family) {
    if (super.belongsTo(family)) {
      return true;
    }
    if (family == ClientSyncJob.class) {
      return true;
    }
    return false;
  }

}
