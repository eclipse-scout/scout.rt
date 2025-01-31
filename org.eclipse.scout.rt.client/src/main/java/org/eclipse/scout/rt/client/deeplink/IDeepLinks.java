/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  /**
   * @param deepLinkPath
   *     The deep-link path in the format <code>[handler name]-[handler data]</code>.
   * @return True if the given path is a valid deep-link path. Only syntax is checked at this point.
   */
  boolean canHandleDeepLink(String deepLinkPath);

  /**
   * Handles the deep-link by delegating to a handler that can process the given path.
   *
   * @param deepLinkPath
   *     The deep-link path in the format <code>[handler name]-[handler data]</code>.
   * @return whether or not a handler has handled the request
   * @throws IllegalArgumentException
   *     when path is not a valid deep-link (check with canHandleDeepLink() before you call this method)
   * @throws DeepLinkException
   *     when the deep-link couldn't be processed for some reasons (e.g. missing permissions)
   */
  boolean handleDeepLink(String deepLinkPath) throws DeepLinkException;
}
