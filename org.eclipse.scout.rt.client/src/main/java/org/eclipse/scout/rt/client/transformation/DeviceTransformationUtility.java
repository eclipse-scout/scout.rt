/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * @since 3.9.0
 */
public final class DeviceTransformationUtility {

  private DeviceTransformationUtility() {
  }

  /**
   * @return a reference to the active device transformation configuration.
   */
  public static DeviceTransformationConfig getDeviceTransformationConfig() {
    IDeviceTransformationService service = BEANS.get(IDeviceTransformationService.class);
    if (service != null && service.getDeviceTransformer() != null) {
      return service.getDeviceTransformer().getDeviceTransformationConfig();
    }

    return null;
  }
}
