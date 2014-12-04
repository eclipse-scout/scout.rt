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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;

/**
 * Process script tags in html files and enable automatic version and cache control handling
 * <p>
 * All script urls matching {@link ScriptBuilder#NON_FRAGMENT_PATH_PATTERN} and that contain the "qualifier" text are
 * replaced
 */
public class HtmlFileHandler {

  public void handle(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, URL url) throws ServletException, IOException {
    //TODO imo
  }

}
