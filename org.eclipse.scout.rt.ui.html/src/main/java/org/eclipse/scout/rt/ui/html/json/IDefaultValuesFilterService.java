/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.service.IService;
import org.json.JSONObject;

public interface IDefaultValuesFilterService extends IService {

  void filter(JSONObject json);

  void filter(JSONObject json, String objectType);

  String getCombinedDefaultValuesConfiguration();

  BinaryResource getCombinedDefaultValuesConfigurationFile(String targetFilename);
}
