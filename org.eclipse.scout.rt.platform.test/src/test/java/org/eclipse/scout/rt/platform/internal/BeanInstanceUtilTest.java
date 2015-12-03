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
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.platform.internal.fixture.EmptyCtorBean;
import org.junit.Test;

/**
 * Tests for {@link BeanInstanceUtil}
 */
public class BeanInstanceUtilTest {

  @Test
  public void testCreate() {
    EmptyCtorBean o = BeanInstanceUtil.create(EmptyCtorBean.class);
    assertNotNull(o);
  }
}
