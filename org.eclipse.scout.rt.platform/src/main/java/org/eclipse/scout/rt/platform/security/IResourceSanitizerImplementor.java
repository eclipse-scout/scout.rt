/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@ApplicationScoped
public interface IResourceSanitizerImplementor {

  /**
   * Sanitizes the given {@code res}
   *
   * @return a new, sanitized {@link BinaryResource}
   * @throws RejectedResourceException
   *     if the implementor is unable to perform the sanitation
   */
  BinaryResource sanitize(BinaryResource res);

  /**
   * @return {@code true} if the sanitizer accepts the given {@code res}
   */
  boolean accepts(BinaryResource res);
}
