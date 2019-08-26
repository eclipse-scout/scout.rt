/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
