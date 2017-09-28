/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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

/**
 * A config property which represents a {@link List} of {@link String}.
 * <p>
 *
 * <pre>
 * my-list-property[0]=value-01
 * my-list-property[1]=value-02
 * my-list-property[2]=value-03
 * </pre>
 *
 * @see PropertiesHelper
 */
public abstract class AbstractStringListConfigProperty extends AbstractConfigProperty<List<String>, List<String>> {

  @Override
  public List<String> readFromSource(String namespace) {
    return ConfigUtility.getPropertyList(getKey(), null, namespace);
  }

  @Override
  protected List<String> parse(List<String> value) {
    return value;
  }
}
