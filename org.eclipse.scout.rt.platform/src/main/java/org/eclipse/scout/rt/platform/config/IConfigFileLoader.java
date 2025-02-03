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

import java.net.URL;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Support for encoding/decoding, encryption/decryption of the content or parts of the content of config files before
 * the raw properties are processed and passed to the individual {@link CONFIG} properties. The runtime inplementation
 * of this interface is loaded with {@link ServiceLoader}.
 * <p>
 * To use this feature, create a file in
 * src/main/resources/META-INF/services/org.eclipse.scout.rt.platform.config.IConfigFileLoader that contains a single
 * line with the implementation class name.
 *
 * @since 6.1
 */
@FunctionalInterface
public interface IConfigFileLoader {

  /**
   * Read the source and fill up all key/value pairs to the properties file.
   * <p>
   * When no service is defined then the default {@link DefaultConfigFileLoader} is used.
   *
   * @param source
   * @param destination
   */
  void load(URL source, Properties destination);
}
