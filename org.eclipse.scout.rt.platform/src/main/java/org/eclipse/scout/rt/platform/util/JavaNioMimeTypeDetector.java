/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;

/**
 * Resolve mime types using {@link Files#probeContentType(Path)}
 * <p>
 * see {@link IMimeTypeDetector}
 *
 * @author BSI AG
 * @since 5.2
 */
@Order(IOrdered.DEFAULT_ORDER)
@ApplicationScoped
public class JavaNioMimeTypeDetector implements IMimeTypeDetector {

  @Override
  public String getMimeType(Path path) {
    try {
      return Files.probeContentType(path);
    }
    catch (IOException e) { // NOSONAR
      //ignore
    }
    return null;
  }
}
