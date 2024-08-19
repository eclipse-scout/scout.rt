/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Represents errors that occur during serialization/deserialization in the {@link IdCodec}.
 *
 * @since 24.2
 */
public class IdCodecException extends PlatformException {

  private static final long serialVersionUID = 1L;

  /**
   * see {@link PlatformException#PlatformException(String, Object...)}
   */
  public IdCodecException(final String message, final Object... args) {
    super(message, args);
  }
}
