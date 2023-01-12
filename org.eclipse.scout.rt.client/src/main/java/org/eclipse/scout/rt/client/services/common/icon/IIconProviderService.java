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

import org.eclipse.scout.rt.platform.service.IService;

/**
 * see {@link IconLocator#instance()}
 */
public interface IIconProviderService extends IService {

  /**
   * the icon lookup can be called with a full icon name (with extension) e.g. 'myIcon.gif' or with a simple icon name
   * e.g. 'myIcon'. In case of calling this method without an extension the list of extensions will be looped and added
   * to the simple icon name. The first found icon is returned.
   *
   * @return the icon specification to the icon or null if not found.
   */
  IconSpec getIconSpec(String name);
}
