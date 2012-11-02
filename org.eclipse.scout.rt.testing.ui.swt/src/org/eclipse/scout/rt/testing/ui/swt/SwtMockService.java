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
package org.eclipse.scout.rt.testing.ui.swt;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.testing.client.IGuiMock;
import org.eclipse.scout.testing.client.IGuiMockService;

/**
 * Uses SwtBot
 */
public class SwtMockService extends AbstractService implements IGuiMockService {

  @Override
  public UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.SWT, UiDeviceType.DESKTOP);
  }

  @Override
  public IGuiMock createMock(IClientSession session) {
    return new SwtMock(session);
  }
}
