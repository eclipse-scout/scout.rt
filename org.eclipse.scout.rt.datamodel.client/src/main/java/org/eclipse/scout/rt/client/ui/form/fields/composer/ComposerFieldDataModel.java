/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.List;

import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * create a data model based on inner classes of the composer field
 */
@SuppressWarnings({"serial", "squid:S2057"})
public class ComposerFieldDataModel extends AbstractDataModel {

  private final IComposerField m_field;

  public ComposerFieldDataModel(IComposerField field) {
    super(false);
    m_field = field;
    callInitializer();
  }

  @Override
  protected List<IDataModelAttribute> createAttributes() {
    return createAttributes(m_field);
  }

  @Override
  protected List<IDataModelEntity> createEntities() {
    return createEntities(m_field);
  }
}
