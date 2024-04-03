/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.api.data.table.MaxResultsHelper.ResultLimiter;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

public class MaxResultsHelperTest {

  @Test
  public void testLimiterForDataObject() {
    var helper = new MaxResultsHelper();
    var dataObject = new DoEntity();
    dataObject.contribution(MaxRowCountContributionDo.class).withHint(1234);
    var limiter = helper.limiter(dataObject);
    assertEquals(1234, limiter.getMaxResults());
    assertEquals(1235, limiter.getQueryLimit());
  }

  @Test
  public void testLimiterForDataObjectWithoutContribution() {
    var helper = new MaxResultsHelper();
    var dataObject = new DoEntity();
    var limiter = helper.limiter(dataObject);
    assertEquals(2000, limiter.getMaxResults());
    assertEquals(2001, limiter.getQueryLimit());

    var limiter2 = helper.limiter((IDoEntity) null);
    assertEquals(2000, limiter2.getMaxResults());
    assertEquals(2001, limiter2.getQueryLimit());
  }

  @Test
  public void testLimitForDataObject() {
    assertLimitWithResponse(14, true, 2000, new DoEntity(), 20, 14, 2000);
    assertLimitWithResponse(20, false, 2000, new DoEntity(), 20, 0, 2000);
    assertLimitWithResponse(5, true, 5, new DoEntity(), 10, 0, 5);
    assertLimitWithResponse(5, true, 5, new DoEntity(), 30, 20, 5);
  }

  @Test
  public void testLimiterSetters() {
    var limiter = new ResultLimiter(10, 4, 100);
    limiter.setRequestedMaxResults(3);
    limiter.setEstimatedRowCount(200);
    assertEquals(3, limiter.getRequestedMaxResults());
    assertEquals(3, limiter.getMaxResults());
    assertEquals(4, limiter.getQueryLimit());
    var result = new DoEntity();
    var list = CollectionUtility.arrayList("1", "2", "3", "4", "5");
    limiter.limit(list, result);
    assertEquals(3, list.size());
    var contribution = result.getContribution(LimitedResultInfoContributionDo.class);
    assertTrue(contribution.getLimitedResult());
    assertEquals(10, contribution.getMaxRowCount().intValue());
    assertEquals(200, contribution.getEstimatedRowCount().intValue());
  }

  @Test
  public void testGetQueryLimit() {
    assertQueryLimit(2000, null);
    assertQueryLimit(2000, 0);
    assertQueryLimit(2000, -1);
    assertQueryLimit(1000, 1000);
    assertQueryLimit(2000, 2000);
    assertQueryLimit(2000, 2001);
    assertQueryLimit(2000, 5000);
  }

  @Test
  public void testResultLimiterCreateInstance() {
    assertThrows(AssertionException.class, () -> new ResultLimiter(-1, 4));
    assertThrows(AssertionException.class, () -> new ResultLimiter(0, 4));
    assertEquals(4, new ResultLimiter(3, 0).getQueryLimit());
  }

  @Test
  public void testResultLimiterLimit() {
    ResultLimiter limiter = new ResultLimiter(1, 0);

    AtomicReference<Boolean> limitedResult = new AtomicReference<>();
    Consumer<Boolean> consumer = b -> limitedResult.set(b);

    // null list
    assertNull(limiter.limit(null, consumer));
    assertEquals(Boolean.FALSE, limitedResult.get());
    limitedResult.set(null);

    // empty list
    List<String> list = Collections.emptyList();
    assertSame(list, limiter.limit(list, consumer));
    assertEquals(Boolean.FALSE, limitedResult.get());
    limitedResult.set(null);

    // singleton list
    list = Collections.singletonList("a");
    assertSame(list, limiter.limit(list, consumer));
    assertEquals(Boolean.FALSE, limitedResult.get());
    limitedResult.set(null);

    // list with two elements
    list = CollectionUtility.arrayList("a", "b");
    assertSame(list, limiter.limit(list, consumer));
    assertEquals(Collections.singletonList("a"), list);
    assertEquals(Boolean.TRUE, limitedResult.get());
    limitedResult.set(null);

    // list with multiple elements
    list = CollectionUtility.arrayList("a", "b", "c", "d", "e");
    assertSame(list, limiter.limit(list, consumer));
    assertEquals(Collections.singletonList("a"), list);
    assertEquals(Boolean.TRUE, limitedResult.get());
    limitedResult.set(null);

    // no limitedResultConsumer
    list = CollectionUtility.arrayList("a", "b", "c", "d", "e");
    assertSame(list, limiter.limit(list, (Consumer<Boolean>) null));
    assertEquals(Collections.singletonList("a"), list);
    assertNull(limitedResult.get());
  }

  protected void assertLimitWithResponse(int expectedNumData, boolean expectedLimitedResult, int expectedMaxRowCount, IDoEntity response, int numData, int requestedMaxResults, int maxResultsLimit) {
    var data = IntStream.range(0, numData).mapToObj(Long::valueOf).collect(Collectors.toList());
    var limiter = new ResultLimiter(maxResultsLimit, requestedMaxResults);
    List<Long> limit = limiter.limit(data, response);
    assertEquals(expectedNumData, limit.size());
    assertEquals(expectedLimitedResult, response.contribution(LimitedResultInfoContributionDo.class).isLimitedResult());
    assertEquals(expectedMaxRowCount, response.contribution(LimitedResultInfoContributionDo.class).getMaxRowCount().intValue());
  }

  protected void assertQueryLimit(int expectedMaxResults, Integer requestedMaxResults) {
    assertEquals(expectedMaxResults + 1, new MaxResultsHelper().limiter(requestedMaxResults).getQueryLimit());
  }
}
