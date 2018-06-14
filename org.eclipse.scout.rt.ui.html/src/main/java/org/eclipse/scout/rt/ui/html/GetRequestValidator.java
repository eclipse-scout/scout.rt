/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public class GetRequestValidator {

  protected static final String PARENT_FOLDER_IDENTIFIER = "..";

  /**
   * path delimiters are slash AND backslash because e.g. ClassLoader or File APIs understand both
   */
  protected static final Pattern REGEX_FOLDER_SPLIT = Pattern.compile("\\\\|/");

  public void validate(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      return;
    }

    String[] folders = REGEX_FOLDER_SPLIT.split(pathInfo);
    if (folders == null || folders.length < 1) {
      return;
    }

    for (String folder : folders) {
      if (PARENT_FOLDER_IDENTIFIER.equals(folder)) {
        throw new IllegalArgumentException("Invalid URI: '" + pathInfo + "'. Parent paths are not allowed.");
      }
    }
  }
}
