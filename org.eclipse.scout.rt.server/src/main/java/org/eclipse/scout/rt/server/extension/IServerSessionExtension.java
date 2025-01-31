/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.extension;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.extension.ServerSessionChains.ServerSessionLoadSessionChain;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IServerSessionExtension<OWNER extends AbstractServerSession> extends IExtension<OWNER> {

  void execLoadSession(ServerSessionLoadSessionChain chain);
}
