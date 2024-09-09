/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.IApiExposedCodeTypeDoProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class ApiExposedCodeTypeDoProviderTest {
  @Test
  public void testProvideNoFilter() {
    Set<CodeTypeDo> codeTypes = BEANS.get(IApiExposedCodeTypeDoProvider.class).provide();
    assertEquals(2, codeTypes.size());
  }

  @Test
  public void testProvideMatchingFilter() {
    Set<CodeTypeDo> codeTypes = BEANS.get(IApiExposedCodeTypeDoProvider.class).provide(Collections.singleton("3"));
    assertEquals(1, codeTypes.size());
    assertEquals("3", codeTypes.iterator().next().getId());

    codeTypes = BEANS.get(IApiExposedCodeTypeDoProvider.class).provide(new HashSet<>(Arrays.asList("1", "2", "3")));
    assertEquals(2, codeTypes.size());
  }

  @Test
  public void testProvideNoMatchingFilter() {
    Set<CodeTypeDo> codeTypes = BEANS.get(IApiExposedCodeTypeDoProvider.class).provide(Collections.singleton("5"));
    assertTrue(codeTypes.isEmpty());
  }

  /**
   * Valid CodeType with id "1"
   */
  @ApiExposed
  public static class ApiExposedFixtureCodeType1 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 1L;
    }
  }

  /**
   * Is skipped because cannot be converted to a DO.
   */
  @ApiExposed
  public static class ApiExposedFixtureCodeType2 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 2L;
    }

    @Override
    public CodeTypeDo toDo() {
      return null;
    }
  }

  /**
   * Valid CodeType with id "3"
   */
  @ApiExposed
  public static class ApiExposedFixtureCodeType3 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 3L;
    }
  }

  /**
   * Is skipped because the ID is null
   */
  @ApiExposed
  public static class ApiExposedFixtureCodeType4 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return null;
    }
  }
}
