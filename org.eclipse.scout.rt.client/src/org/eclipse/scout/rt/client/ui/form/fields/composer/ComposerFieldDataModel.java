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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.ArrayList;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * create a data model based on inner classes of the composer field
 */
public class ComposerFieldDataModel extends AbstractDataModel {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ComposerFieldDataModel.class);
  private static final long serialVersionUID = 1L;

  private final IComposerField m_field;

  public ComposerFieldDataModel(IComposerField field) {
    super(false);
    m_field = field;
    callInitializer();
  }

  @Override
  protected IDataModelAttribute[] createAttributes() {
    ArrayList<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>();
    Class<?>[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    for (Class<? extends IDataModelAttribute> c : ConfigurationUtility.sortFilteredClassesByOrderAnnotation(all, IDataModelAttribute.class)) {
      try {
        IDataModelAttribute a = ConfigurationUtility.newInnerInstance(m_field, c);
        attributes.add(a);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return attributes.toArray(new IDataModelAttribute[attributes.size()]);
  }

  @Override
  protected IDataModelEntity[] createEntities() {
    ArrayList<IDataModelEntity> entities = new ArrayList<IDataModelEntity>();
    Class<?>[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    for (Class<? extends IDataModelEntity> c : ConfigurationUtility.sortFilteredClassesByOrderAnnotation(all, IDataModelEntity.class)) {
      try {
        IDataModelEntity e = ConfigurationUtility.newInnerInstance(m_field, c);
        entities.add(e);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return entities.toArray(new IDataModelEntity[entities.size()]);
  }

}
