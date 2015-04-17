/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

// Implementation note: Do _not_ add @Bean annotation to this interface, only to implementations!
// Reason: There are cases where we want to pass an instance of this interface to UiSession.newJsonAdapter()
// without registering it with the bean manager.
public interface IJsonObjectFactory {

  /**
   * This factory creates IJsonAdapter instances for a given model object. The instance must not be initialized (using
   * <code>Init()</code>), this has to be done by the caller.
   */
  IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent);

  IJsonObject createJsonObject(Object object);
}
