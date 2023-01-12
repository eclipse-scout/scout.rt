/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.icon;

import java.net.URL;

import org.eclipse.scout.rt.client.ResourceBase;
import org.eclipse.scout.rt.platform.Order;

@Order(5500)
public class ScoutIconProviderService extends AbstractIconProviderService {

  @Override
  protected URL findResource(String relativePath) {
    return ResourceBase.class.getResource("icons/" + relativePath);
  }
}
