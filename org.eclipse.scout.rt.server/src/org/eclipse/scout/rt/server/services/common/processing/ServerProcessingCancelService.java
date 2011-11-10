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
import org.eclipse.scout.rt.server.transaction.internal.ActiveTransactionRegistry;
import org.eclipse.scout.rt.shared.services.common.processing.IServerProcessingCancelService;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class ServerProcessingCancelService extends AbstractService implements IServerProcessingCancelService {

  @Override
  public void cancel(long transcationSequence) {
    ActiveTransactionRegistry.cancel(transcationSequence);
  }
}
