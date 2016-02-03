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
package org.eclipse.scout.rt.client.ui;

/**
 * Specifies, if it is possible to use HTML that is rendered in the UI.
 * <p>
 * If HTML is enabled, the user needs to make sure that any user input (or other insecure input) is encoded (security).
 *
 * @since 5.2 (backported)
 */
public interface IHtmlCapable {
  String PROP_HTML_ENABLED = "htmlEnabled";

  /**
   * Enable or disable the rendering of HTML.<br>
   * Make sure that any user input (or other insecure input) is encoded (security)
   */
  void setHtmlEnabled(boolean enabled);

  /**
   * @return true, if the cell may contain html that needs to be rendered. false otherwise.
   */
  boolean isHtmlEnabled();

}
