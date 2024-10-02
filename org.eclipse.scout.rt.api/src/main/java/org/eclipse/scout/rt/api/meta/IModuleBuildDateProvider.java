/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.meta;

import java.util.Date;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Optional provider for the build date of an application module.
 */
@ApplicationScoped
public interface IModuleBuildDateProvider {

  /**
   * @return Build date of application module.
   */
  Date getBuildDate();
}
