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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@ApplicationScoped
public class ResourceSanitizer {

  /**
   * If there exists an {@link IResourceSanitizerImplementor} that accepts the given {@code res}
   * it will be used to sanitize it
   *
   * @return a new, sanitized {@link BinaryResource} if there exists a corresponding implementor,
   * the provided {@code res} otherwise
   * @throws RejectedResourceException
   *     if the implementor is unable to perform the sanitation
   */
  public BinaryResource sanitize(BinaryResource res) {
    for (IResourceSanitizerImplementor sanitizer : BEANS.all(IResourceSanitizerImplementor.class)) {
      if (sanitizer.accepts(res)) {
        return sanitizer.sanitize(res);
      }
    }
    return res;
  }
}
