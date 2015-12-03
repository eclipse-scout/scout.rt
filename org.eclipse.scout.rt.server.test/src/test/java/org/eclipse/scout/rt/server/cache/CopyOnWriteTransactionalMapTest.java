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

import java.util.Map;

/**
 * @since 5.2
 */
public class CopyOnWriteTransactionalMapTest extends AbstractTransactionalMapTest {

  @Override
  protected <K, V> Map<K, V> createTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> initialMap) {
    return new CopyOnWriteTransactionalMap<K, V>(transactionMemberId, fastForward, initialMap);
  }
}
