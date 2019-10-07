/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.net.URL;
import java.util.Optional;

public abstract class AbstractWebResourceHelper implements IWebResourceHelper {

  public static final String OUTPUT_FOLDER_NAME = "dist";
  public static final String DEV_FOLDER_NAME = "dev";
  public static final String MIN_FOLDER_NAME = "prod";
  public static final String WEB_RESOURCE_FOLDER_NAME = "res";

  public static String stripLeadingSlash(String path) {
    if (path == null) {
      return null;
    }
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  protected String getWebResourceFolder(boolean minified) {
    return minified ? MIN_FOLDER_NAME : DEV_FOLDER_NAME;
  }

  @Override
  public Optional<URL> getScriptResource(String path, boolean minified) {
    if (path == null) {
      return Optional.empty();
    }
    URL url = getResourceImpl(getWebResourceFolder(minified) + "/" + stripLeadingSlash(path));
    return Optional.ofNullable(url);
  }

  @Override
  public Optional<URL> getWebResource(String path) {
    if (path == null) {
      return Optional.empty();
    }
    URL url = getResourceImpl(WEB_RESOURCE_FOLDER_NAME + "/" + stripLeadingSlash(path));
    return Optional.ofNullable(url);
  }

  /**
   * @return The {@link URL} or {@code null}.
   */
  protected abstract URL getResourceImpl(String resourcePath);
}
