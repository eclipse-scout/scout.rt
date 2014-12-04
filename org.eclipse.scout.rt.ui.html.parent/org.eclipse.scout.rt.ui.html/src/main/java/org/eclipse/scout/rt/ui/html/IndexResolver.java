/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpServletRequest;

public class IndexResolver {
  public static final String INDEX_HTML = "/index.html";
  public static final String MOBILE_INDEX_HTML = "/index-mobile.html";

  public String resolve(HttpServletRequest request) {
    BrowserInfo browserInfo = new BrowserInfoBuilder().createBrowserInfo(request);
    if (browserInfo.isMobile()) {
      return MOBILE_INDEX_HTML;
    }

    return INDEX_HTML;
  }
}
