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
    try (BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream(), StandardCharsets.ISO_8859_1))) {
      destination.load(in);
    }
    catch (Exception t) {
      throw new IllegalArgumentException("Unable to parse properties file from url '" + source.toExternalForm() + "'.", t);
    }
  }

}
