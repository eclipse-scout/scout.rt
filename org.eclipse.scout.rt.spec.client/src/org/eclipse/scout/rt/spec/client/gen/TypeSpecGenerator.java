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
package org.eclipse.scout.rt.spec.client.gen;

import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;

public class TypeSpecGenerator {
  private final IDocConfig m_config;
  private String m_id;
  private String m_title;

  public TypeSpecGenerator(IDocConfig config, String id, String title) {
    m_config = config;
    m_id = id;
    m_title = title;
  }

  public IDocSection getDocSection(List<Class<?>> fieldTypes) {

    IDocSection typeSection = DocGenUtility.createDocSection(fieldTypes, m_config.getTypesConfig());
    return new SectionWithTable("org.eclipse.scout.rt.spec.types", TEXTS.get("org.eclipse.scout.rt.spec.types"), typeSection);
  }
}
