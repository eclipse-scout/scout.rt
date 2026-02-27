/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.session.SessionId;

@ApplicationScoped
public class SessionNonce {

  public String provide() {
    return provide(SessionId.CURRENT.get());
  }

  public String provide(String sessionId) {
    return "sessionNonce:" + sessionId;
  }
}
