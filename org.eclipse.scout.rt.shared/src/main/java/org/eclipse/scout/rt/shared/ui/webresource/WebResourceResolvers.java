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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Bean
public class WebResourceResolvers {

  private static final LazyValue<WebResourceResolvers> HELPERS = new LazyValue<>(WebResourceResolvers.class);
  private final FinalValue<IWebResourceResolver> m_helper = new FinalValue<>();

  public static IWebResourceResolver create() {
    return HELPERS.get().getInstance();
  }

  protected IWebResourceResolver getInstance() {
    return m_helper.setIfAbsentAndGet(this::createHelper);
  }

  protected IWebResourceResolver createHelper() {
    if (Platform.get().inDevelopmentMode()) {
      return BEANS.get(FilesystemWebResourceResolver.class);
    }
    return BEANS.get(ClasspathWebResourceResolver.class);
  }

  public static boolean isNewMode() {
    String newModeValue = System.getProperty("newMode");
    return !StringUtility.hasText(newModeValue) || "true".equalsIgnoreCase(newModeValue);
  }
}
