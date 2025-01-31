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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Default implementation of {@link IConfigFileLoader}
 *
 * @since 6.1
 */
public class DefaultConfigFileLoader implements IConfigFileLoader {

  @Override
  public void load(URL source, Properties destination) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream(), StandardCharsets.UTF_8))) {
      destination.load(in);
    }
    catch (Exception t) {
      throw new IllegalArgumentException("Unable to parse properties file from url '" + source.toExternalForm() + "'.", t);
    }
  }
}
