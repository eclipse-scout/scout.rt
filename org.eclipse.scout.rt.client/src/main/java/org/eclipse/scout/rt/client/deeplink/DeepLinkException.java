/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.deeplink;

/**
 * A checked exception which indicates that a deep-link URL could not be processed for some (business logic) reason.
 * There are several cases:
 * <ol>
 * <li>The regex pattern is valid and matches, but no data has been found for the requested deep-link path</li>
 * <li>A resource has been found but the current user has no permissions to read the resource. In that case this
 * exception contains the original VetoException as cause</li>
 * <li>An unexpected error occurred while resolving the deep link (e.g. external system not available). The original
 * exception is available by {@link #getCause()}</li>
 * </ol>
 */
public class DeepLinkException extends Exception {

  private static final long serialVersionUID = 1L;

  public DeepLinkException() {
    super();
  }

  public DeepLinkException(String message) {
    super(message);
  }

  public DeepLinkException(RuntimeException cause) {
    super(cause);
  }
}
