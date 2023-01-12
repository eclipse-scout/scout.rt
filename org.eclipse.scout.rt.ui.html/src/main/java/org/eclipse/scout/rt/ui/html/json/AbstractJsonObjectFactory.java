/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.ui.html.IUiSession;

/**
 * Abstract implementation of {@link IJsonObjectFactory} that returns <code>null</code> on all methods.
 *
 * @since 5.2
 */
public abstract class AbstractJsonObjectFactory implements IJsonObjectFactory {

  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    return null;
  }

  @Override
  public IJsonObject createJsonObject(Object object) {
    return null;
  }
}
