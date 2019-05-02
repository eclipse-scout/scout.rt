/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
