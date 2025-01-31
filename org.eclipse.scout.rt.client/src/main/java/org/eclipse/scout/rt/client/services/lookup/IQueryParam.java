/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

public interface IQueryParam<T> {

  enum QueryBy {
    ALL,
    TEXT,
    KEY,
    REC
  }

  QueryBy getQueryBy();

  boolean is(QueryBy parentKey);

  T getKey();

  String getText();
}
