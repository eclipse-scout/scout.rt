/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
