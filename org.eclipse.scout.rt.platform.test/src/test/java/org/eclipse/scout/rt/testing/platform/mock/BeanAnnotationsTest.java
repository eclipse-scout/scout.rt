/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.mock;

import static org.junit.Assert.*;

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
  public void testClearEmptyBA() {
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
