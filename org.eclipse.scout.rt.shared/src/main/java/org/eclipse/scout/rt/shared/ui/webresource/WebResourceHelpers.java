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

@Bean
public class WebResourceHelpers {

  private static final LazyValue<WebResourceHelpers> HELPERS = new LazyValue<>(WebResourceHelpers.class);
  private final FinalValue<IWebResourceHelper> m_helper = new FinalValue<>();

  public static IWebResourceHelper create() {
    return HELPERS.get().get();
  }

  protected IWebResourceHelper get() {
    return m_helper.setIfAbsentAndGet(this::createHelper);
  }

  protected IWebResourceHelper createHelper() {
    if (Platform.get().inDevelopmentMode()) {
      return BEANS.get(FilesystemWebResourceHelper.class);
    }
    return BEANS.get(ClasspathWebResourceHelper.class);
  }
  
  public static boolean isNewMode() {
    return Boolean.parseBoolean(System.getProperty("newMode")); // TODO [mvi]: remove
  }
}
