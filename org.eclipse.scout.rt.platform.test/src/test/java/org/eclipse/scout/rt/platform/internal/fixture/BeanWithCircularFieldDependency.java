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
import org.junit.Assert;

/**
 * Sample bean for testing constructor circular dependency
 */
@Bean
public class BeanWithCircularFieldDependency {

  @InjectBean
  private BeanWithCircularFieldDependency m_field;

  public BeanWithCircularFieldDependency() {
  }

  public void assertInit() {
    Assert.assertNull(m_field);
  }
}
