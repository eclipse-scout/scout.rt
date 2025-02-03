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

import static org.eclipse.scout.rt.platform.util.CollectionUtility.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.SharedConfigProperties.LoadWebResourcesFromFilesystemConfigProperty;

@Bean
public class WebResources {

  private static final LazyValue<WebResources> WEB_RESOURCES = new LazyValue<>(WebResources.class);

  private static final LazyValue<FilesystemWebResourceResolver> FS_RESOLVER = new LazyValue<>(FilesystemWebResourceResolver.class);
  private static final LazyValue<ClasspathWebResourceResolver> CP_RESOLVER = new LazyValue<>(ClasspathWebResourceResolver.class);

  /**
   * Tries to load the specified relative path in the script resources folders. Depending on the minified parameter this
   * is in the "prod" or "dev" folder.
   *
   * @param path
   *     The relative file path
   * @param minified
   *     If the resource should be loaded in a minified or normal version
   * @param theme
   *     The theme to use. If the resource is a css file the file that matches the theme is returned instead of the
   *     normal file. For this the file must end with the theme name.
   * @param cacheEnabled
   *     Specifies if the cache is enabled
   * @return An {@link Optional} holding the first {@link WebResourceDescriptor} found for the given path or an empty
   * {@link Optional} if the resource could not be found.
   * @see IWebResourceResolver#resolveScriptResource(String, boolean, boolean, String)
   */
  public static Optional<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, boolean cacheEnabled, String theme) {
    return resolveScriptResources(path, minified, cacheEnabled, theme).stream().findFirst();
  }

  /**
   * @see IWebResourceResolver#resolveScriptResource(String, boolean, boolean, String)
   */
  public static List<WebResourceDescriptor> resolveScriptResources(String path, boolean minified, boolean cacheEnabled, String theme) {
    return WEB_RESOURCES.get().resolveScriptResourceInternal(path, minified, cacheEnabled, theme);
  }

  /**
   * Tries to load the specified relative path in the web resources folder ("res" folder).
   *
   * @param path
   *     The relative file path
   * @param minified
   *     If the resource should be loaded in a minified (if existing) or normal version
   * @param cacheEnabled
   *     Specifies if the cache is enabled
   * @return An {@link Optional} holding the first {@link WebResourceDescriptor} found or an empty {@link Optional} if
   * the resource could not be found.
   * @see IWebResourceResolver#resolveWebResource(String, boolean, boolean)
   */
  public static Optional<WebResourceDescriptor> resolveWebResource(String path, boolean minified, boolean cacheEnabled) {
    return resolveWebResources(path, minified, cacheEnabled).stream().findFirst();
  }

  /**
   * @see IWebResourceResolver#resolveWebResource(String, boolean, boolean)
   */
  public static List<WebResourceDescriptor> resolveWebResources(String path, boolean minified, boolean cacheEnabled) {
    return WEB_RESOURCES.get().resolveWebResourceInternal(path, minified, cacheEnabled);
  }

  protected List<WebResourceDescriptor> resolveScriptResourceInternal(String path, boolean minified, boolean cacheEnabled, String theme) {
    return resolveResource(resolver -> resolver.resolveScriptResource(cleanPath(path), minified, cacheEnabled, theme));
  }

  protected List<WebResourceDescriptor> resolveWebResourceInternal(String path, boolean minified, boolean cacheEnabled) {
    return resolveResource(resolver -> resolver.resolveWebResource(cleanPath(path), minified, cacheEnabled));
  }

  protected List<WebResourceDescriptor> resolveResource(Function<IWebResourceResolver, List<WebResourceDescriptor>> callFunc) {
    if (CONFIG.getPropertyValue(LoadWebResourcesFromFilesystemConfigProperty.class)) {
      List<WebResourceDescriptor> resFromFilesystem = arrayListWithoutNullElements(callFunc.apply(FS_RESOLVER.get()));
      if (hasElements(resFromFilesystem)) {
        return resFromFilesystem;
      }
    }
    return arrayListWithoutNullElements(callFunc.apply(CP_RESOLVER.get()));
  }

  protected String cleanPath(String path) {
    if (!StringUtility.hasText(path)) {
      return null;
    }
    return path.trim();
  }
}
