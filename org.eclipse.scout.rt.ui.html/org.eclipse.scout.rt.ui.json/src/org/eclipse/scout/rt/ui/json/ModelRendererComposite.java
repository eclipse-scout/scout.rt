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

class ModelRendererComposite {

  private final String m_id;

  private final IJsonRenderer<?> m_jsonRenderer;

  private final Object m_modelObject;

  ModelRendererComposite(String id, Object modelObject, IJsonRenderer<?> jsonRenderer) {
    m_id = id;
    m_modelObject = modelObject;
    m_jsonRenderer = jsonRenderer;
  }

  public String getId() {
    return m_id;
  }

  public IJsonRenderer<?> getJsonRenderer() {
    return m_jsonRenderer;
  }

  public Object getModelObject() {
    return m_modelObject;
  }

}
