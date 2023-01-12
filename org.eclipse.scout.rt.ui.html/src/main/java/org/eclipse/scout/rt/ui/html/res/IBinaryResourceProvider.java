/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * {@link IJsonAdapter}s can implement {@link IBinaryResourceProvider} in order to provide public {@link URL} calling
 * back to them.
 * <p>
 * URLs that call back to this method are defined using
 * {@link BinaryResourceUrlUtility#createDynamicAdapterResourceUrl(IJsonAdapter, String)}
 */
@FunctionalInterface
public interface IBinaryResourceProvider {

  BinaryResourceHolder provideBinaryResource(String filename);
}
