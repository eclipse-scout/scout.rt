/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.busy;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * The busy manager service is global and is the primary place to register/unregister {@link IBusyHandler}s per
 * {@link IClientSession}
 * 
 * @author imo
 * @since 3.8
 */
public interface IBusyManagerService extends IService {

  void register(IClientSession session, IBusyHandler handler);

  void unregister(IClientSession session);

  IBusyHandler getHandler(IClientSession session);

}
