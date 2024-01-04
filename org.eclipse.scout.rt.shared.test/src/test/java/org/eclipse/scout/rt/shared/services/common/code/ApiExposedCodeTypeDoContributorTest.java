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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.IApiExposedCodeTypeContributor;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Assert;
import org.junit.Test;

public class ApiExposedCodeTypeDoContributorTest {
  @Test
  public void testContribute() {
    Set<CodeTypeDo> dos = new HashSet<>();
    BEANS.all(IApiExposedCodeTypeContributor.class).forEach(c -> c.contribute(dos));
    Assert.assertEquals(1, dos.size());
    Assert.assertEquals("1", dos.iterator().next().getId());
  }

  @ApiExposed
  public static class ApiExposedFixtureCodeType1 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 1L;
    }
  }

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
}
