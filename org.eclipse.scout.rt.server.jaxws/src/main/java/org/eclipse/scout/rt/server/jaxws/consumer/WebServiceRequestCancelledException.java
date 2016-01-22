/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Indicates that the result of a web service request cannot be retrieved because the request was cancelled.
 *
 * @since 5.2
 */
public class WebServiceRequestCancelledException extends PlatformException {

  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link PlatformException}
   */
  public WebServiceRequestCancelledException(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public WebServiceRequestCancelledException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
