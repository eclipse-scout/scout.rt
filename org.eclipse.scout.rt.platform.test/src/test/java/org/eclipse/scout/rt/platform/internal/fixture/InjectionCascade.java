/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
