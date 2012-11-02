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

import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.transport.http.HttpAdapterList;

public class ServletAdapterFactory extends HttpAdapterList<ServletAdapter> {

  /**
   * Factory to create {@link ServletAdapter}
   */
  @Override
  protected ServletAdapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
    return new ServletAdapter(endpoint, name, urlPattern, this);
  }
}
