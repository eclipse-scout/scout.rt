/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  /**
   * @return True if the given path is a valid deep-link path. Only syntax is checked at this point.
   * @param deepLinkPath
   *          The deep-link path in the format <code>[handler name]-[handler data]</code>.
   */
  boolean canHandleDeepLink(String deepLinkPath);

  /**
   * Handles the deep-link by delegating to a handler that can process the given path.
   *
   * @param deepLinkPath
   *          The deep-link path in the format <code>[handler name]-[handler data]</code>.
   * @return whether or not a handler has handled the request
   * @throws IllegalArgumentException
   *           when path is not a valid deep-link (check with canHandleDeepLink() before you call this method)
   * @throws DeepLinkException
   *           when the deep-link couldn't be processed for some reasons (e.g. missing permissions)
   */
  boolean handleDeepLink(String deepLinkPath) throws DeepLinkException;

}
