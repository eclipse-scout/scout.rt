/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

public interface ServiceTunnelConstants {

  String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  /**
   * Marker header for session-less requests.
   */
  String WITHOUT_SESSION_HEADER = "X-WithoutSession";

  /**
   * Path used for service tunnel
   */
  String PROCESS_PATH = "process";
}
