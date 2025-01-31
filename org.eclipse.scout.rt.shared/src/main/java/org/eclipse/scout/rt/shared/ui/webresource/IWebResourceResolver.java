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

import java.util.List;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public interface IWebResourceResolver {

  /**
   * Tries to load the specified relative path in the script resources folders. Depending on the minified parameter this
   * is in the "prod" or "dev" folder.
   *
   * @param path
   *     The relative file path
   * @param minified
   *     If the resource should be loaded in a minified or normal version
   * @param cacheEnabled
   *     Specifies if the cache is enabled
   * @param theme
   *     The theme to use. If the resource is a css file the file that matches the theme is returned instead of the
   *     normal file. For this the file must end with the theme name.
   * @return A {@link List} holding the {@link WebResourceDescriptor} instances found for the given path or an empty
   * {@link List} if the resource could not be found.
   */
  List<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, boolean cacheEnabled, String theme);

  /**
   * Tries to load the specified relative path in the web resources folder ("res" folder).
   *
   * @param path
   *     The relative file path
   * @param minified
   *     If the resource should be loaded in a minified (if existing) or normal version
   * @param cacheEnabled
   *     Specifies if the cache is enabled
   * @return A {@link List} holding the {@link WebResourceDescriptor} instances or an empty {@link List} if the resource
   * could not be found.
   */
  List<WebResourceDescriptor> resolveWebResource(String path, boolean minified, boolean cacheEnabled);
}
