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
package org.eclipse.scout.testing.client;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.IService2;

/**
 * This interface is used to support gui testing with an abstraction layer.
 * <p>
 * Therefore swt and swing gui tests can be programmed just once in the client and run with both guis.
 */
public interface IGuiMockService extends IService2 {

  UserAgent initUserAgent();

  IGuiMock createMock(IClientSession session);

}
