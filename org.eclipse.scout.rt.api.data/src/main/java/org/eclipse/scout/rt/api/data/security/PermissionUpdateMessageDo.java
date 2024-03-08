/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.security;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.PermissionUpdateMessage")
public class PermissionUpdateMessageDo extends DoEntity {
  public DoValue<Long> reloadDelayWindow() {
    return doValue("reloadDelayWindow");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PermissionUpdateMessageDo withReloadDelayWindow(Long reloadDelayWindow) {
    reloadDelayWindow().set(reloadDelayWindow);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getReloadDelayWindow() {
    return reloadDelayWindow().get();
  }
}
