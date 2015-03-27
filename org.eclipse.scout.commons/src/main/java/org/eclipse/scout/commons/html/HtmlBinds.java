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
package org.eclipse.scout.commons.html;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.html.internal.AbstractBinds;

/**
 * HTML Binds <br>
 */
public class HtmlBinds extends AbstractBinds {

  /**
   * Replace bind names with encoded values.
   */
  public String applyBindParameters(IHtmlElement... htmls) {
    String res = "";
    for (IHtmlElement html : htmls) {
      res += replace(html);
    }
    return res;
  }

  /**
   * Replace bind names with encoded values.
   */
  public String applyBindParameters(List<? extends IHtmlElement> htmls) {
    String res = "";
    for (IHtmlElement html : htmls) {
      res += replace(html);
    }
    return res;
  }

  /**
   * Replace bind names with encoded values.
   */
  private String replace(IHtmlElement html) {
    String res = html.toString();
    for (Entry<String, Object> b : getBinds().entrySet()) {
      res = res.replaceAll(b.getKey(), encode(b.getValue()));
    }
    return res;
  }

  /**
   * @return the encoded bind value.
   */
  protected String encode(Object value) {
    return HTMLUtility.encodeText(StringUtility.emptyIfNull(value).toString());
  }

}
