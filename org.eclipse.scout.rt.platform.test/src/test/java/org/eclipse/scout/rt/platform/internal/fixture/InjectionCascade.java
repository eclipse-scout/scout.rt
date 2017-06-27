/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal.fixture;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.InjectBean;

@Bean
public class InjectionCascade {
  private final BeanWithInjections m_d1;
  private final EmptyCtorBean m_d2;

  /**
   * A bean can only have one injectable constructor
   */
  @InjectBean
  public InjectionCascade(BeanWithInjections d1, EmptyCtorBean d2) {
    this.m_d1 = d1;
    this.m_d2 = d2;
  }

  public void assertInit() {
    m_d1.assertInit();
    m_d2.assertInit();
  }

}
