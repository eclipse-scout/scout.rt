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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * {@link IJsonAdapter}s can implements {@link IBinaryResourceUploader} in order to consume files from the UI (file
 * upload). Other than IBinaryResourceConsumer it returns a link to the uploaded resource, so we can use that link
 * directly in the HTTP response to the upload request.
 */
public interface IBinaryResourceUploader extends IUploadable {

  List<String> uploadBinaryResources(List<BinaryResource> binaryResources, Map<String, String> uploadProperties);

}
