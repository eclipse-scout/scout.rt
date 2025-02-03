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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IClientSessionExtension<OWNER extends AbstractClientSession> extends IExtension<OWNER> {

  void execStoreSession(ClientSessionStoreSessionChain chain);

  void execLoadSession(ClientSessionLoadSessionChain chain);
}
