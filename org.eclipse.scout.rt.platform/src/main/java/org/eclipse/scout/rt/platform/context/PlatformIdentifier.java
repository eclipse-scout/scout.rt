/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.context;

import javax.management.MXBean;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Provides the platform identification.
 * <p>
 * The scope of the platform identifier is per root classloader, whereas the {@link NodeIdentifier} typically is defined
 * per JRE.
 * <p>
 * If the platform is started using the WebappEventListener, then the platform identifier is
 * ServletContext#getContextPath().
 * <p>
 * The platform identifier can be used to distinguish multiple platforms (WAR containers) inside the same JRE. This
 * distinction is also useful when dealing with {@link MXBean}.
 * <p>
 * Note that the platform identifier is not a {@link Bean}, its default value is available once the WebappEventListener
 * starts the application.
 *
 * @see 9.0
 */
public final class PlatformIdentifier {
  private static String platformIdentifier;

  private PlatformIdentifier() {
  }

  /**
   * @return the platform identifier
   */
  public static String get() {
    return platformIdentifier;
  }

  /**
   * set the platform identifier
   */
  public static void set(String s) {
    platformIdentifier = s;
  }

}
