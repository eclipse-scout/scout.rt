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
   * @see IWebResourceResolver#resolveScriptResource(String, boolean, String)
   */
  public static Optional<WebResourceDescriptor> resolveScriptResource(String path, boolean minified, String theme) {
    return WEB_RESOURCES.get().resolveScriptResourceInternal(path, minified, theme);
  }

  /**
   * @see IWebResourceResolver#resolveWebResource(String, boolean)
   */
  public static Optional<WebResourceDescriptor> resolveWebResource(String path, boolean minified) {
    return WEB_RESOURCES.get().resolveWebResourceInternal(path, minified);
  }

  protected Optional<WebResourceDescriptor> resolveScriptResourceInternal(String path, boolean minified, String theme) {
    return resolveResource(resolver -> resolver.resolveScriptResource(cleanPath(path), minified, theme));
  }

  protected Optional<WebResourceDescriptor> resolveWebResourceInternal(String path, boolean minified) {
    return resolveResource(resolver -> resolver.resolveWebResource(cleanPath(path), minified));
  }

  protected Optional<WebResourceDescriptor> resolveResource(Function<IWebResourceResolver, Optional<WebResourceDescriptor>> callFunc) {
    if (CONFIG.getPropertyValue(LoadWebResourcesFromFilesystemConfigProperty.class)) {
      Optional<WebResourceDescriptor> resFromFilesystem = callFunc.apply(FS_RESOLVER.get());
      if (resFromFilesystem.isPresent()) {
        return resFromFilesystem;
      }
    }
    return callFunc.apply(CP_RESOLVER.get());
  }

  protected String cleanPath(String path) {
    if (!StringUtility.hasText(path)) {
      return null;
    }
    return path.trim();
  }

  /**
   * TODO: delete this method
   */
  public static boolean isNewMode() {
    String newModeValue = System.getProperty("newMode");
    return !StringUtility.hasText(newModeValue) || "true".equalsIgnoreCase(newModeValue);
  }
}
