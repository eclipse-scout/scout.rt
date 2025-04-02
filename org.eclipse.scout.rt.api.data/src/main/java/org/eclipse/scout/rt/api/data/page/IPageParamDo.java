/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.page;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeVersionRequired;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Base interface for all page param types.
 */
@TypeVersionRequired
public interface IPageParamDo extends IDoEntity {

  @SuppressWarnings("unchecked")
  default <T> T copy() {
    return (T) BEANS.get(DataObjectHelper.class).clone(this);
  }
}
