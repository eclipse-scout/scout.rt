/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static org.eclipse.scout.rt.platform.util.EnumerationUtility.asStream;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.exception.PlatformException;

public class ClasspathWebResourceResolver extends AbstractWebResourceResolver {

  private final ClassLoader m_classLoader = getClass().getClassLoader();

  @Override
  protected Stream<URL> getResourceImpl(String resourcePath) {
    try {
      return asStream(m_classLoader.getResources(resourcePath));
    }
    catch (IOException e) {
      throw new PlatformException("Error getting resources for path '{}' from classpath.", resourcePath, e);
    }
  }
}
