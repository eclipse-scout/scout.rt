/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IDefaultValuesFilterService;

/**
 * This class loads and parses JSON files from WebContent/ folder.
 */
public class DefaultValuesLoader extends AbstractResourceLoader {

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    return BEANS.get(IDefaultValuesFilterService.class).getCombinedDefaultValuesConfigurationFile(pathInfo);
  }
}
