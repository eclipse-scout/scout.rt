/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.platform.fixture.TestBeanProducer;
import org.eclipse.scout.rt.platform.fixture.ThrowingTestBeanProducer;
import org.junit.Test;

/**
 * Tests for {@link BeanMetaData}
 */
public class BeanMetaDataTest {

  @Test
  public void testProducer() {
    BeanMetaData md = new BeanMetaData(A.class);
    assertNotNull(md.getProducer());
    assertEquals(TestBeanProducer.class, md.getProducer().getClass());
  }

  @Test(expected = RuntimeException.class)
  public void testProducerWithException() {
    BeanMetaData md = new BeanMetaData(B.class);
    assertNotNull(md.getProducer());
  }

  @Bean
  @BeanProducer(TestBeanProducer.class)
  public class A {
  }

  @Bean
  @BeanProducer(ThrowingTestBeanProducer.class)
  public class B {
  }

}
