/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.serverbridge;

import org.eclipse.scout.rt.client.servicetunnel.http.MultiSessionCookieStoreInstallListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.Replace;

@Replace
public class BridgeToServerMultiSessionCookieStoreInstallListener extends MultiSessionCookieStoreInstallListener {

  @Override
  public void stateChanged(PlatformEvent event) {
    // NOP - Don't install MultiSessionCookieStore in bridge mode!
  }
}
