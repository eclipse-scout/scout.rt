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
package org.eclipse.scout.rt.platform.config;

import java.util.Map;

/**
 * A config property which represents a {@link Map}.
 * <p>
 *
 * <pre>
 * my-map-property[map-key-01]=value-01
 * my-map-property[map-key-02]=value-02
 * my-map-property[map-key-03]=value-03
 * </pre>
 *
 * @see PropertiesHelper
 */
public abstract class AbstractMapConfigProperty extends AbstractConfigProperty<Map<String, String>, Map<String, String>> {

  @Override
  public Map<String, String> readFromSource(String namespace) {
    return ConfigUtility.getPropertyMap(getKey(), null, namespace);
  }

  @Override
  protected Map<String, String> parse(Map<String, String> value) {
    return value;
  }
}
