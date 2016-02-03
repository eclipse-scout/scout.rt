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
package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.scout.commons.html.HtmlHelper;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;

/**
 * @since 4.2
 */
public class RwtScoutHtmlValidator implements IRwtScoutHtmlValidator {

  @Override
  public String escape(String text, IHtmlCapable htmlCapable) {
    if (htmlCapable.isHtmlEnabled() || !RwtUtility.VALIDATE_HTML_CAPABLE) { // developer is responsible for validating user input
      return text;
    }
    return HtmlHelper.escape(text);
  }
}
