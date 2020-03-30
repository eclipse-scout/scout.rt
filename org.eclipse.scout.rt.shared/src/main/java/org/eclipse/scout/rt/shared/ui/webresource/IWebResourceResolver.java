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

import java.util.Optional;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public interface IWebResourceResolver {

  /**
   * Tries to load the specified relative path in the script resources folders. Depending on the minified parameter this
   * is in the "prod" or "dev" folder.
   *
   * @param path
   *          The relative file path
   * @param minified
   *          If the resource should be loaded in a minified or normal version
   * @param theme
   *          The theme to use. If the resource is a css file the file that matches the theme is returned instead of the
   *          normal file. For this the file must end with the theme name.
   * @return An {@link Optional} holding the {@link WebResourceDescriptor} or an empty {@link Optional} if the resource
   *         could not be found.
   */
  Optional<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, String theme);

  /**
   * Tries to load the specified relative path in the web resources folder ("res" folder).
   *
   * @param path
   *          The relative file path
   * @param minified
   *          If the resource should be loaded in a minified (if existing) or normal version
   * @return An {@link Optional} holding the {@link WebResourceDescriptor} or an empty {@link Optional} if the resource
   *         could not be found.
   */
  Optional<WebResourceDescriptor> resolveWebResource(String path, boolean minified);

  Optional<WebResourceDescriptor> resolveIndexFile(String path);
}
