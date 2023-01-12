/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * This handler is used as root handler. Root handler is called also for a path for which no other handler matches.
 */
public class RootBinaryRefHandler implements IBinaryRefHandler {

  @Override
  public String getRegistrationPath() {
    return "";
  }

  @Override
  public BinaryResource loadBinaryResource(URI uri) {
    return null;
  }

  @Override
  public void getFilenames(Map<URI, String> resultCollector, Collection<URI> uris) {
    // resource not found - no filename
  }
}
