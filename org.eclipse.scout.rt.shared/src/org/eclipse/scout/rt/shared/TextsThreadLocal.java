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

/**
 * This class represents the nls texts set for the current thread
 */
public class TextsThreadLocal {
  private static final ThreadLocal<ScoutTexts> THREAD_LOCAL = new ThreadLocal<ScoutTexts>();

  private TextsThreadLocal() {
  }

  public static ScoutTexts get() {
    return THREAD_LOCAL.get();
  }

  public static void set(ScoutTexts t) {
    THREAD_LOCAL.set(t);
  }
}
