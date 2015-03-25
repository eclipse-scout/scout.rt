/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.extension;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;

/**
 *
 */
public final class FormFieldExtensions {

  private final Map<Class<?> /*model */, Class<?> /*ui or factory */> m_extensions;

  public final static FormFieldExtensions INSTANCE = new FormFieldExtensions();

  private FormFieldExtensions() {
    m_extensions = new HashMap<>();
  }

  public Map<Class<?>, Class<?>> getFormFieldExtensions() {
    return CollectionUtility.copyMap(m_extensions);
  }

  public void put(Class<?> model, Class<?> ui) {
    m_extensions.put(model, ui);
  }
}
