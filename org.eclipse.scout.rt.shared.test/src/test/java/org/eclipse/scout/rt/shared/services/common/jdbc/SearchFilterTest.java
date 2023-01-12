/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.jdbc;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthias Villiger
 */
public class SearchFilterTest {
  @Test
  public void testDisplayTestPlain() {
    SearchFilter f = new SearchFilter();
    f.addDisplayText("ab");
    f.addDisplayText("be");
    f.addDisplayText(null);
    f.addDisplayText("cd");
    Assert.assertEquals("ab\nbe\ncd", f.getDisplayTextsPlain());

    Assert.assertEquals("", new SearchFilter().getDisplayTextsPlain());

    SearchFilter f2 = new SearchFilter();
    f2.addDisplayText("ab");
    Assert.assertEquals("ab", f2.getDisplayTextsPlain());

    SearchFilter f3 = new SearchFilter();
    f3.addDisplayText(null);
    f3.addDisplayText(null);
    f3.addDisplayText("ab");
    Assert.assertEquals("ab", f3.getDisplayTextsPlain());
  }
}
