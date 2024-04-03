/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.api.data.config.ConfigPropertyDo;
import org.eclipse.scout.rt.api.data.config.IApiExposedConfigPropertyContributor;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;

/**
 * Loader for config.properties coming from the UI server
 */
public class ConfigPropertiesLoader extends AbstractResourceLoader {

  public static final String FILE_NAME = "config-properties.json";

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    Set<ConfigPropertyDo> configProperties = new HashSet<>();
    BEANS.all(IApiExposedConfigPropertyContributor.class).forEach(contributor -> contributor.contribute(configProperties));
    String json = BEANS.get(IDataObjectMapper.class).writeValue(configProperties);
    return BinaryResources.create()
        .withContent(json)
        .withContentType(MimeType.JSON.getType())
        .withCacheMaxAge(0)
        .withFilename(FILE_NAME)
        .build();
  }
}
