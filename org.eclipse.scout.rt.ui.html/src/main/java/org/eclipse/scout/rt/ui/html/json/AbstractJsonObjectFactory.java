/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
