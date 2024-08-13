/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.server.commons.GlobalTrustManager;

@Replace
public class ClientGlobalTrustManager extends GlobalTrustManager {

  @Override
  protected List<X509Certificate> getTrustedCertificatesInRemoteFiles() {
    // no access to remote files possible because backend might not be reachable yet
    return Collections.emptyList();
  }
}
