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
package org.eclipse.scout.rt.server.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.transaction.BasicTransaction;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.junit.Test;

/**
 * @since 5.2
 */
public abstract class AbstractTransactionalMapTest {
  private final static String TRANSACTION_MEMBER_ID = "TEST_ID";

  protected abstract <K, V> Map<K, V> createTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> initialMap);

  protected ITransaction createNewTransaction() {
    return new BasicTransaction();
  }

  protected void commitTransaction(ITransaction tr) {
    assertTrue(tr.commitPhase1());
    tr.commitPhase2();
    tr.release();
  }

  protected void rollbackTransaction(ITransaction tr) {
    tr.rollback();
    tr.release();
  }

  @SuppressWarnings("unchecked")
  protected <K, V> void assertMapStateSimple(Map<K, V> actualMap, Object... expectedKeyValuePairs) {
    Map<K, V> expectedMap = new HashMap<K, V>();
    for (int i = 0; i < expectedKeyValuePairs.length; i = i + 2) {
      expectedMap.put((K) expectedKeyValuePairs[i], (V) expectedKeyValuePairs[i + 1]);
    }
    assertMapState(actualMap, expectedMap);
  }

  protected <K, V> void assertMapState(Map<K, V> actualMap, Map<K, V> expectedMap) {
    // test size method
    assertEquals(expectedMap.size(), actualMap.size());
    // test map iterator
    for (Iterator<Entry<K, V>> iterator = actualMap.entrySet().iterator(); iterator.hasNext();) {
      Entry<K, V> entry = iterator.next();
      assertEquals(expectedMap.get(entry.getKey()), entry.getValue());
    }
    for (Iterator<Entry<K, V>> iterator = expectedMap.entrySet().iterator(); iterator.hasNext();) {
      Entry<K, V> entry = iterator.next();
      // test map containsKey
      assertTrue(actualMap.containsKey(entry.getKey()));
      // test map getter
      assertEquals(entry.getValue(), actualMap.get(entry.getKey()));
    }
  }

  @Test
  public void testPut() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(1, "1");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, false, initalMap);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1");

    // tr1
    ITransaction.CURRENT.set(tr1);
    assertMapStateSimple(map, 1, "1");
    map.put(2, "2");
    assertMapStateSimple(map, 1, "1", 2, "2");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1");

    // tr2
    ITransaction.CURRENT.set(tr2);
    assertMapStateSimple(map, 1, "1");
    map.put(3, "3");
    assertMapStateSimple(map, 1, "1", 3, "3");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1");

    commitTransaction(tr1);
    assertMapStateSimple(map, 1, "1", 2, "2");

    commitTransaction(tr2);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");
  }

  @Test
  public void testPutAll() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, false, new HashMap<Integer, String>());

    // tr1
    ITransaction.CURRENT.set(tr1);
    Map<Integer, String> tr1Map = new HashMap<Integer, String>();
    tr1Map.put(1, "1");
    tr1Map.put(2, "2");
    map.putAll(tr1Map);
    assertMapState(map, tr1Map);

    // tr2
    ITransaction.CURRENT.set(tr2);
    Map<Integer, String> tr2Map = new HashMap<Integer, String>();
    tr2Map.put(2, "3");// transactional concurrent modification
    tr2Map.put(3, "3");
    tr2Map.put(4, "4");
    map.putAll(tr2Map);
    assertMapState(map, tr2Map);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapState(map, Collections.<Integer, String> emptyMap());

    commitTransaction(tr1);
    assertMapState(map, tr1Map);

    commitTransaction(tr2);
    tr1Map.putAll(tr2Map);
    tr1Map.remove(2);// not in shared map because of concurrent modification
    assertMapState(map, tr1Map);
  }

  @Test
  public void testRemove() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(1, "1");
    initalMap.put(2, "2");
    initalMap.put(3, "3");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, false, initalMap);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    // tr1
    ITransaction.CURRENT.set(tr1);
    map.remove(1);
    map.remove(2);
    assertMapStateSimple(map, 3, "3");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    // tr2
    ITransaction.CURRENT.set(tr2);
    map.remove(2);
    map.remove(3);
    assertMapStateSimple(map, 1, "1");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    commitTransaction(tr1);
    assertMapStateSimple(map, 3, "3");

    commitTransaction(tr2);
    assertMapState(map, Collections.<Integer, String> emptyMap());
  }

  @Test
  public void testConcurrentModifications() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(1, "1");
    initalMap.put(2, "2");
    initalMap.put(3, "3");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, false, initalMap);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    // tr1
    ITransaction.CURRENT.set(tr1);
    map.remove(1);
    map.put(1, "1.1");
    map.remove(2);
    map.put(2, "2.1");
    assertMapStateSimple(map, 1, "1.1", 2, "2.1", 3, "3");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    // tr2
    ITransaction.CURRENT.set(tr2);
    map.remove(2);
    map.put(2, "2.2");
    map.remove(3);
    map.put(3, "3.2");
    map.put(4, "4");
    assertMapStateSimple(map, 1, "1", 2, "2.2", 3, "3.2", 4, "4");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    commitTransaction(tr1);
    assertMapStateSimple(map, 1, "1.1", 2, "2.1", 3, "3");

    commitTransaction(tr2);
    assertMapStateSimple(map, 1, "1.1", 3, "3.2", 4, "4");
  }

  @Test
  public void testFastForward() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(1, "1");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, true, initalMap);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1");

    // tr1
    ITransaction.CURRENT.set(tr1);
    assertMapStateSimple(map, 1, "1");
    map.put(2, "2");
    assertMapStateSimple(map, 1, "1", 2, "2");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2");

    // tr2
    ITransaction.CURRENT.set(tr2);
    assertMapStateSimple(map, 1, "1", 2, "2");
    map.put(3, "3");
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");
    map.put(2, "2.2");
    assertMapStateSimple(map, 1, "1", 2, "2.2", 3, "3");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    commitTransaction(tr1);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    commitTransaction(tr2);
    assertMapStateSimple(map, 1, "1", 2, "2.2", 3, "3");
  }

  @Test
  public void testRollback() {
    ITransaction tr1 = createNewTransaction();
    ITransaction tr2 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(1, "1");
    initalMap.put(2, "2");
    initalMap.put(3, "3");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, true, initalMap);

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    // tr1
    ITransaction.CURRENT.set(tr1);
    map.put(2, "2.2");
    assertMapStateSimple(map, 1, "1", 2, "2.2", 3, "3");

    // tr2
    ITransaction.CURRENT.set(tr2);
    map.remove(3);
    assertMapStateSimple(map, 1, "1", 2, "2");

    // shared
    ITransaction.CURRENT.set(null);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    rollbackTransaction(tr1);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");

    rollbackTransaction(tr2);
    assertMapStateSimple(map, 1, "1", 2, "2", 3, "3");
  }

  @Test
  public void testIteratorRemove() {
    ITransaction tr1 = createNewTransaction();

    Map<Integer, String> initalMap = new HashMap<Integer, String>();
    initalMap.put(2, "2");
    initalMap.put(3, "3");
    Map<Integer, String> map = createTransactionalMap(TRANSACTION_MEMBER_ID, false, initalMap);
    ITransaction.CURRENT.set(tr1);
    map.put(1, "1");
    map.put(2, "2.2");
    map.put(4, "4");

    // remove entries while iterating (no concurrent modification exception is thrown)
    for (Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
      iterator.next();
      iterator.remove();
    }

    assertTrue("Map must be empty", map.isEmpty());
  }
}
