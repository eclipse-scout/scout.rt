/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.fixture;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 * @since 4.2
 */
public class TestingExtensibleObject implements IExtensibleObject {

  @Override
  public List<? extends IExtension<?>> getAllExtensions() {
    return Collections.emptyList();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    throw new UnsupportedOperationException("not implemented");
  }
}
