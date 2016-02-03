/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;

import org.eclipse.scout.commons.html.HtmlHelper;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * @since 4.2
 */
public class SwingScoutHtmlValidator implements ISwingScoutHtmlValidator {

  @Override
  public String escape(String text, IHtmlCapable htmlCapable) {
    if (htmlCapable.isHtmlEnabled() || !SwingUtility.VALIDATE_HTML_CAPABLE) { // developer is responsible for validating user input
      return text;
    }
    return HtmlHelper.escape(text);
  }

  @Override
  public boolean removeHtmlRenderer(Object obj, String text, JLabel label) {
    if (!(obj instanceof IHtmlCapable) || !SwingUtility.VALIDATE_HTML_CAPABLE) {
      return false;
    }

    if (!BasicHTML.isHTMLString(text) || ((IHtmlCapable) obj).isHtmlEnabled()) {
      return false;
    }

    label.putClientProperty(BasicHTML.propertyKey, null);
    return true;
  }
}
