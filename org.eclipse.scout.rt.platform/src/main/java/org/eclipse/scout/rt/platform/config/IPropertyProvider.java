/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import java.util.List;
import java.util.Map.Entry;

public interface IPropertyProvider {

  /**
   * The identifier is used to ensure every property provider is only parsed once in case of circular imports of
   * property providers.
   */
  Object getPropertiesIdentifier();

  /**
   * @return all the properties of this provider.
   */
  List<Entry<String, String>> readProperties();
}
