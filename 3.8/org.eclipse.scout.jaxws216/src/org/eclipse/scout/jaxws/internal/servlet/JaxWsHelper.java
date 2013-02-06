/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

public final class JaxWsHelper {

  private JaxWsHelper() {
  }

  /**
   * To get the base address of the application
   * 
   * @param request
   * @return
   */
  public static String getBaseAddress(HttpServletRequest request, boolean includeContextPath) {
    StringBuilder builder = new StringBuilder();
    builder.append(request.getScheme());
    builder.append("://");
    builder.append(request.getServerName());
    builder.append(':');
    builder.append(request.getServerPort());
    if (includeContextPath) {
      builder.append(request.getContextPath());
    }
    return builder.toString();
  }

  /**
   * Clones the given header map
   * 
   * @param headers
   * @return
   */
  public static Map<String, List<String>> cloneHeaderMap(Map<String, List<String>> headers) {
    Map<String, List<String>> clone = new HashMap<String, List<String>>();

    for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
      String name = headerEntry.getKey();
      List<String> values = headerEntry.getValue();

      clone.put(name, new ArrayList<String>(values));
    }
    return clone;
  }
}
