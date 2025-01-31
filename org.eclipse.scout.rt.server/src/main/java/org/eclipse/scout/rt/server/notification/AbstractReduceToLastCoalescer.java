/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.notification;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Reduce the list to the last element
 */
public abstract class AbstractReduceToLastCoalescer<T> implements ICoalescer<T> {

  @Override
  public List<T> coalesce(List<T> list) {
    return CollectionUtility.arrayList(CollectionUtility.lastElement(list));
  }
}
