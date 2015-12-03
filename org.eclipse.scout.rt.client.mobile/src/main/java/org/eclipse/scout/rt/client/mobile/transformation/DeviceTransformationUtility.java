/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * @since 3.9.0
 */
public class DeviceTransformationUtility {

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
