/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared;

import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * Indicates whether the client is a web-client or a rich-standalone-client
 * <p>
 * 
 * @deprecated Use {@link UserAgentUtility} instead. Will be removed in Release 3.10.
 */
@Deprecated
public final class WebClientState {
  private static boolean defaultValue = false;
  private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<Boolean>();

  private WebClientState() {
  }

  public static boolean isWebClientDefault() {
    return defaultValue;
  }

  public static boolean isRichClientDefault() {
    return !isWebClientDefault();
  }

  /**
   * Sets the default state. Thread-specific values {@link #setWebClientInCurrentThread()} are not changed by this
   * method.
   */
  public static void setWebClientDefault(boolean b) {
    defaultValue = b;
  }

  public static boolean isWebClientInCurrentThread() {
    Boolean var = THREAD_LOCAL.get();
    if (var != null) {
      return var.booleanValue();
    }
    else {
      return isWebClientDefault();
    }
  }

  public static boolean isRichClientInCurrentThread() {
    return !isWebClientInCurrentThread();
  }

  /**
   * @param b
   *          true, false, null <br>
   *          null clears the thread-specific value and uses
   *          the default of {@link #isWebClientDefault()}
   */
  public static void setWebClientInCurrentThread(Boolean b) {
    THREAD_LOCAL.set(b);
  }

  public static String getFontSizeUnit() {
    if (isWebClientInCurrentThread()) {
      return "px";
    }
    return "pt";
  }
}
