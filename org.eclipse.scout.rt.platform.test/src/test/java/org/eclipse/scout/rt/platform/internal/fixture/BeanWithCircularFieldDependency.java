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
