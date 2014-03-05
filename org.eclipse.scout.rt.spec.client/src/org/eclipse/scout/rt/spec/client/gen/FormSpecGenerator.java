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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityConfig;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.out.IDocTable;
import org.eclipse.scout.rt.spec.client.out.internal.SectionWithTable;

/**
 * Creates Specification data from Form fields
 */
public class FormSpecGenerator {
  private final IDocConfig m_config;

  public FormSpecGenerator(IDocConfig config) {
    m_config = config;
  }

  public IDocSection getDocSection(IForm form) {
    //general form info
    IDocEntityConfig<IForm> formConfig = m_config.getFormConfig();
    IDocTable formSpec = DocGenUtility.createDocTable(form, formConfig);

    //fields
    IDocFormFieldVisitor[] visitors = new IDocFormFieldVisitor[]{
        new FormFieldSpecsVisitor(m_config),
        new TableFieldVisitor(m_config),
        new SmartFieldVisitor(m_config)
    };
    IDocSection[] subSections = getSubSections(form, visitors);
    String title = formConfig.getTitleExtractor().getText(form);
    return new SectionWithTable(title, formSpec, subSections);
  }

  private IDocSection[] getSubSections(IForm form, IDocFormFieldVisitor... visitors) {
    List<IDocSection> subSections = new ArrayList<IDocSection>();
    for (IDocFormFieldVisitor v : visitors) {
      subSections.addAll(getTableFields(form, v));
    }
    return CollectionUtility.toArray(subSections, IDocSection.class);
  }

  private List<IDocSection> getTableFields(IForm form, IDocFormFieldVisitor visitor) {
    form.visitFields(visitor);
    return visitor.getDocSections();
  }

}
