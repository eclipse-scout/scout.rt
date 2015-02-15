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
 *
 * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
 *             {@link ScoutTexts#CURRENT} instead; will be removed in 5.2.0;
 */
@Deprecated
public class TextsThreadLocal {

  private TextsThreadLocal() {
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ScoutTexts#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static ScoutTexts get() {
    return ScoutTexts.CURRENT.get();
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ScoutTexts#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void set(ScoutTexts texts) {
    ScoutTexts.CURRENT.set(texts);
  }
}
