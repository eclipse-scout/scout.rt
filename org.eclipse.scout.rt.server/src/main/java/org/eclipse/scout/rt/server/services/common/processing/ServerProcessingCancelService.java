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
package org.eclipse.scout.rt.server.services.common.processing;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.filter.JobFilter;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.filter.ServerSessionFilter;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class ServerProcessingCancelService extends AbstractService implements IServerProcessingCancelService {

  @Override
  public boolean cancel(final long requestSequence) {
    final IFilter<IFuture<?>> filterJobId = new JobFilter(String.valueOf(requestSequence));
    final IFilter<IFuture<?>> filterCurrentSession = new ServerSessionFilter((IServerSession) IServerSession.CURRENT.get());

    // Cancel the job and its associated transaction.
    return OBJ.one(IServerJobManager.class).cancel(new AndFilter<>(filterJobId, filterCurrentSession), true);
  }
}
