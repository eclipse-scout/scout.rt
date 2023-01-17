/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
