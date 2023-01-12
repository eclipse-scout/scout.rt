/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.userfilter;

import java.io.Serializable;

/**
 * @since 5.1
 */
public interface IUserFilterState extends Serializable {
  String getType();

  void setType(String type);

  /**
   * Computes a key which is used by the table to store the filter.
   */
  Object createKey();

  /**
   * @param obj
   * @return <code>true</code> if the deserialize operation was successful. <code>false</code> otherwise.
   */
  boolean notifyDeserialized(Object obj);

  String getDisplayText();
}
