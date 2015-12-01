/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Test;

/**
 * Tests for {@link CodeTypeNotificationCoalescer}
 *
 * @deprecated replaced with {@link InvalidateCacheNotification}. Will be removed in Scout 6.1. See {@link ICache}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class CodeTypeNotificationCoalescerTest {

  @Test
  public void testCoalesceEmptySet() {
    CodeTypeNotificationCoalescer coalescer = new CodeTypeNotificationCoalescer();
    List<CodeTypeChangedNotification> res = coalescer.coalesce(new ArrayList<CodeTypeChangedNotification>());
    assertTrue(res.isEmpty());
  }

  @Test
  public void testCoalesceNotificationsSet() {
    CodeTypeNotificationCoalescer coalescer = new CodeTypeNotificationCoalescer();
    List<Class<? extends ICodeType<?, ?>>> testTypes1 = new ArrayList<>();
    List<Class<? extends ICodeType<?, ?>>> testTypes2 = new ArrayList<>();
    testTypes1.add(CodeType1.class);
    testTypes1.add(CodeType2.class);
    testTypes2.add(CodeType2.class);
    List<CodeTypeChangedNotification> testList = CollectionUtility.arrayList(
        new CodeTypeChangedNotification(testTypes1),
        new CodeTypeChangedNotification(testTypes1));
    CollectionUtility.arrayList(CodeType2.class);
    List<CodeTypeChangedNotification> res = coalescer.coalesce(testList);
    assertEquals(1, res.size());
    CodeTypeChangedNotification firstNotification = res.iterator().next();
    ScoutAssert.assertSetEquals(testTypes1, firstNotification.getCodeTypes());
  }

  class CodeType1 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 0L;
    }
  }

  class CodeType2 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 0L;
    }
  }

}
