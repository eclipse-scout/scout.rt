/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl;

import java.io.IOException;
import java.io.InputStream;

import com.asual.lesscss.loader.ClasspathResourceLoader;

/**
 * This class is used to lookup resources when the @import statement is used in a LESS file.
 * <p>
 * Because Scout compiles all individual .css files to a single .css file which is passed to the LessEngine relative
 * paths used in @import statements do not work as expected when using Asual's standard resource loaders. When LESS
 * interprets the single .css file for the first time the context-path is empty. When LESS finds an @import statement
 * (e.g. 'scout/style/colors-foo.css'), LESS reads that file and sets the context-path to 'scout/style'. When
 * colors-foo.css has another import like 'scout/style/colors.css', the request path for that resource will be
 * 'scout/style/scout/style/colors.css' which obviously isn't found anywhere on the classpath. That's why we must change
 * the {@link #appendPathToResource(String, String)} method, which cannot be overridden easily because everything is
 * private in the StreamResourceLoader class.
 */
public class ScoutClasspathResourceLoader extends ClasspathResourceLoader {

  public ScoutClasspathResourceLoader(ClassLoader classLoader) {
    super(classLoader);
  }

  @Override
  public boolean exists(String resource, String[] paths) throws IOException {
    if (resource != null && exists(resource)) {
      return true;
    }
    return false;
  }

  @Override
  public String load(String resource, String[] paths, String charset) throws IOException {
    String content = load(resource, charset);
    if (content != null) {
      return content;
    }
    throw new IOException("No such file " + resource);
  }

  @SuppressWarnings("resource")
  private String load(String resourcePath, String charset) throws IOException {
    if (resourcePath != null) {
      InputStream is = openStream(resourcePath);
      if (is != null) {
        return readStream(is, charset);
      }
    }
    return null;
  }

  private boolean exists(String resourcePath) throws IOException {
    try (InputStream stream = openStream(resourcePath)) {
      if (stream != null) {
        return true;
      }
    }
    return false;
  }

}
