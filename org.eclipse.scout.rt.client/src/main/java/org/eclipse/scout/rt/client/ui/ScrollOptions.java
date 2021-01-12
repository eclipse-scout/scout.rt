/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

public class ScrollOptions {
  private boolean m_animate;

  public boolean isAnimate() {
    return m_animate;
  }

  public void setAnimate(boolean animate) {
    m_animate = animate;
  }

  public ScrollOptions withAnimate(boolean animate) {
    setAnimate(animate);
    return this;
  }
}
