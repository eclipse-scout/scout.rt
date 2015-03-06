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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;

/**
 *
 */
@ApplicationScoped
public final class FormFieldExtensions {

  private final List<IFormFieldExtension> m_extensions;

  public FormFieldExtensions() {
    m_extensions = new ArrayList<>();
  }

  public List<IFormFieldExtension> getFormFieldExtensions() {
    return CollectionUtility.arrayList(m_extensions);
  }

  void add(IFormFieldExtension ext) {
    m_extensions.add(ext);
  }
}
