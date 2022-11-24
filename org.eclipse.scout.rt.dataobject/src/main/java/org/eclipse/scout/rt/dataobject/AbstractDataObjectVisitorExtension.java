/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
