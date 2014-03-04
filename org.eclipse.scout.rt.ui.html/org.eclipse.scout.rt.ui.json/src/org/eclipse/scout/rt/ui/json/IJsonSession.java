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
package org.eclipse.scout.rt.ui.json;

import org.eclipse.scout.rt.client.IClientSession;

public interface IJsonSession {

  void init() throws JsonUIException;

  IClientSession getClientSession();

  String createUniqueIdFor(IJsonRenderer renderer);

  void registerJsonRenderer(String id, IJsonRenderer renderer);

  void unregisterJsonRenderer(String id);

  IJsonRenderer getJsonRenderer(String id);

  /**
   * @return the current ui response that is collecting changes for the next {@link #processRequest(Jsoneventuest)}
   *         cycle
   */
  JsonResponse currentJsonResponse();

  JsonResponse processRequest(JsonRequest req) throws JsonUIException;

}
