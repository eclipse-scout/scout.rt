/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
