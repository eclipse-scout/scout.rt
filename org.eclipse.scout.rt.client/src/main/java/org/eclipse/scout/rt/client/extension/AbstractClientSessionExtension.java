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
package org.eclipse.scout.rt.client.extension;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionLoadSessionChain;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionStoreSessionChain;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractClientSessionExtension<OWNER extends AbstractClientSession> extends AbstractExtension<OWNER> implements IClientSessionExtension<OWNER> {

  public AbstractClientSessionExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execStoreSession(ClientSessionStoreSessionChain chain) {
    chain.execStoreSession();
  }

  @Override
  public void execLoadSession(ClientSessionLoadSessionChain chain) {
    chain.execLoadSession();
  }

}
