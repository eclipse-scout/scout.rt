/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

/**
 * Specifies, if it is possible to use HTML that is rendered in the UI.
 * <p>
 * If HTML is enabled, the user needs to make sure that any user input (or other insecure input) is encoded (security).
 *
 * @since 5.1
 */
public interface IHtmlCapable {
  String PROP_HTML_ENABLED = "htmlEnabled";

  /**
   * Enable or disable the rendering of HTML.<br>
   * Make sure that any user input (or other insecure input) is encoded (security)
   */
  void setHtmlEnabled(boolean htmlEnabled);

  /**
   * @return true, if the cell may contain html that needs to be rendered. false otherwise.
   */
  boolean isHtmlEnabled();
}
