/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Abstract data object visitor extension that implements {@link IDataObjectVisitorExtension#valueClass()} based on the
 * generic type.
 */
public abstract class AbstractDataObjectVisitorExtension<T> implements IDataObjectVisitorExtension<T> {

  @Override
  public Class<T> valueClass() {
    // noinspection unchecked
    return TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractDataObjectVisitorExtension.class);
  }
}
