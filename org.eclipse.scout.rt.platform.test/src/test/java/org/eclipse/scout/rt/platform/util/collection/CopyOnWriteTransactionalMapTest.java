/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

import java.util.Map;

/**
 * @since 5.2
 */
public class CopyOnWriteTransactionalMapTest extends AbstractTransactionalMapTest {

  @Override
  protected <K, V> Map<K, V> createTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> initialMap) {
    return new CopyOnWriteTransactionalMap<>(transactionMemberId, fastForward, initialMap);
  }
}
