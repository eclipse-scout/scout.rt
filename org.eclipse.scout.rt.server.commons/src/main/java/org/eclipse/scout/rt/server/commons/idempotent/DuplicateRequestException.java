/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.idempotent;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;

/**
 * Thrown when a request was sent multiple times.
 *
 * @see ServiceTunnelRequest
 * @since 9.0
 */
public class DuplicateRequestException extends PlatformException {
  private static final long serialVersionUID = 1L;

  public DuplicateRequestException(String message, Object... args) {
    super(message, args);
  }

  public static DuplicateRequestException create(String context, long requestSeq) {
    return new DuplicateRequestException("request {} of {} was already sent, this is a duplicate or retry submission; check if some proxy or agents have retry-post enabled.", requestSeq, context);
  }
}
