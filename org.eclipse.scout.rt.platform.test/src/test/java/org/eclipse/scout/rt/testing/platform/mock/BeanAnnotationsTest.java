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
package org.eclipse.scout.rt.testing.platform.mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Test for {@link BeanAnnotations}
 */
public class BeanAnnotationsTest {

  @Test
  public void testNoFields() {
    BeanAnnotations ba = new BeanAnnotations();
    NoFieldsFixture a = new NoFieldsFixture();
    ba.init(a);
    assertNull(a.m_myMock);
    assertNull(a.m_s);
  }

  @Test
  public void testPrivateBeanField() {
    BeanAnnotations ba = new BeanAnnotations();
    BeanFieldFixture a = new BeanFieldFixture();
    ba.init(a);
    assertNotNull(a.m_myMock);
    assertNotNull(BEANS.opt(TA.class));
  }

  @Test
  public void testRegistrationCleared() {
    BeanAnnotations ba = new BeanAnnotations();
    BeanFieldFixture a = new BeanFieldFixture();
    ba.init(a);
    ba.clear();
    assertNull(BEANS.opt(TA.class));
  }

  @Test
  public void testClearEmptyBA() throws Exception {
    BeanAnnotations ba = new BeanAnnotations();
    ba.clear();
  }

  //test fixtures

  class BeanFieldFixture {
    @BeanMock
    private TA m_myMock;
  }

  class NoFieldsFixture {
    @Mock
    private TA m_myMock;
    private String m_s;
  }

  @Bean
  interface TA {
  }

}
