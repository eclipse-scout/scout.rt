/*******************************************************************************
 * Copyright (c) 2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.IJsonPropertyReader;
import org.eclipse.scout.rt.platform.config.PropertiesHelper;
import org.eclipse.scout.rt.platform.util.StreamUtility;

/**
 * This {@link IJsonPropertyReader} implementation leverages the {@link IDataObjectMapper} in order to read in a
 * provided property value string and convert it into a {@link Map} that can be processed by the
 * {@link PropertiesHelper}.
 */
public class DataObjectJsonPropertyReader implements IJsonPropertyReader {

  @Override
  public Map<String, String> readJsonPropertyValue(String propertyValue) {
    if (propertyValue == null) {
      return null;
    }

    if (propertyValue.isEmpty()) {
      return Collections.emptyMap();
    }

    IDoEntity entity = BEANS.get(IDataObjectMapper.class).readValue(propertyValue, IDoEntity.class);

    // We can't use Collectors.toMap() here as it contains a bug in several OpenJDK versions that does not allow the valueMapper to return a null value for any key
    // see https://bugs.openjdk.java.net/browse/JDK-8148463
    return entity.all().entrySet().stream()
        .collect(StreamUtility.toMap(HashMap::new, Entry::getKey, entry -> Objects.toString(entry.getValue(), null)));
  }
}
