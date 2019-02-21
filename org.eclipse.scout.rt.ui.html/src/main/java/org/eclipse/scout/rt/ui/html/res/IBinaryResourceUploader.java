/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * {@link IJsonAdapter}s can implements {@link IBinaryResourceUploader} in order to consume files from the UI (file
 * upload). Other than IBinaryResourceConsumer it returns a link to the uploaded resource, so we can use that link
 * directly in the HTTP response to the upload request.
 */
public interface IBinaryResourceUploader extends IBinaryResourceHandler {

  List<String> uploadBinaryResources(List<BinaryResource> binaryResources, Map<String, String> uploadProperties);

}
