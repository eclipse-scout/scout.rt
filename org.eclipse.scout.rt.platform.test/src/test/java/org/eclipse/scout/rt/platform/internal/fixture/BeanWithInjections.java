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

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.InjectBean;
import org.junit.Assert;

/**
 * Sample bean for testing with injected constructor, method and field
 */
@Bean
public class BeanWithInjections {
  private final BeanWithStaticInstanceCounter m_c1;

  private final BeanWithStaticInstanceCounter m_c2;

  @InjectBean
  private BeanWithStaticInstanceCounter m_field1;

  @InjectBean
  private BeanWithStaticInstanceCounter m_field2;

  private BeanWithStaticInstanceCounter m_init1;

  private BeanWithStaticInstanceCounter m_init2;

  private BeanWithStaticInstanceCounter m_postConstruct;

  /**
   * A bean can only have one injectable constructor
   */
  @InjectBean
  public BeanWithInjections(BeanWithStaticInstanceCounter c1, BeanWithStaticInstanceCounter c2) {
    m_c1 = c1;
    m_c2 = c2;
  }

  @PostConstruct
  private void postConstruct() {
    m_postConstruct = BEANS.get(BeanWithStaticInstanceCounter.class);
  }

  @InjectBean
  public void init1(BeanWithStaticInstanceCounter b) {
    m_init1 = b;
  }

  @InjectBean
  private void init2(BeanWithStaticInstanceCounter b) {
    m_init2 = b;
  }

  public void assertInit() {
    int offset = m_c1.getInstanceIndex();
    Assert.assertEquals(offset + 0, m_c1.getInstanceIndex());
    Assert.assertEquals(offset + 1, m_c2.getInstanceIndex());
    Assert.assertEquals(offset + 2, m_field1.getInstanceIndex());
    Assert.assertEquals(offset + 3, m_field2.getInstanceIndex());
    Assert.assertEquals(offset + 4, Math.min(m_init1.getInstanceIndex(), m_init2.getInstanceIndex()));
    Assert.assertEquals(offset + 5, Math.max(m_init1.getInstanceIndex(), m_init2.getInstanceIndex()));
    Assert.assertEquals(offset + 6, m_postConstruct.getInstanceIndex());
  }

}
