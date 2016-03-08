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
