/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.servicetunnel.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * This utility class is used to create URL instances with a checked ProcessingException.
 * 
 * @author awe
 */
public final class HttpUtility {

  private HttpUtility() {
  }

  /**
   * Returns an URL instance for the given URL string.
   * 
   * @param url
   * @return
   * @throws ProcessingException
   */
  public static URL createURL(String url) throws ProcessingException {
    if (url == null) {
      return null;
    }
    try {
      return new URL(url);
    }
    catch (MalformedURLException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

}
