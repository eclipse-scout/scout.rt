/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

/**
 * Default implementation of a {@link IWebContentService} that searches on the classpath.
 */
public class WebContentService implements IWebContentService {

  protected String stripLeadingSlash(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  @Override
  public URL getScriptSource(String path) {
    if (path == null) {
      return null;
    }
    return getResourceImpl(stripLeadingSlash(path));
  }

  @Override
  public URL getWebContentResource(String path) {
    if (path == null) {
      return null;
    }
    return getResourceImpl("WebContent/" + stripLeadingSlash(path));
  }

  protected URL getResourceImpl(String resourcePath) {
    //disable hacker attacks using '..'
    if (resourcePath.contains("..")) {
      throw new IllegalArgumentException("path must not contain any '..'");
    }
    return getClass().getClassLoader().getResource(resourcePath);
  }
}
