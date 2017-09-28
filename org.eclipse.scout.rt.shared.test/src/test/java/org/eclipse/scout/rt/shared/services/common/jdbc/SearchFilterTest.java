/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.jdbc;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SearchFilterTest}</h3>
 *
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
