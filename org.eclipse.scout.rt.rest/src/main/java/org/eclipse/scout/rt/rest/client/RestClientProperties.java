/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client;

public final class RestClientProperties {

  /**
   * A value of {@code true} enables cookies.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * </p>
   * <p>
   * The default value is {@code false}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String ENABLE_COOKIES = "scout.rest.client.enableCookies";

  /**
   * A value of {@code true} disables chunked transfer encoding.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * <p>
   * The default value is {@code false}.
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String DISABLE_CHUNKED_TRANSFER_ENCODING = "scout.rest.client.disableChunkedTransferEncoding";
}
