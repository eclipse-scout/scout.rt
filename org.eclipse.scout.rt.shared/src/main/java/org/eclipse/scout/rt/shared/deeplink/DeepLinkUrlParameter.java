/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.deeplink;

public final class DeepLinkUrlParameter {

  /**
   * Name of the URL parameter which contains the deep-link path in the format
   * <code>[handler name]-[handler data]</code>.
   */
  public static final String DEEP_LINK = "dl";

  /**
   * Name of the optional URL parameter which contains a human readable, informative text about the deep-link.
   */
  public static final String INFO = "i";

  /**
   * Name of the parameter used to enable/disable deep link handling
   */
  public static final String HANDLE_DEEP_LINK = "handleDeepLink";

  private DeepLinkUrlParameter() {
  }
}
