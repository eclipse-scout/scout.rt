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
import java.util.Collections;
import java.util.List;

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
  protected List<IDataModelAttribute> createAttributes() {
    List<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>();
    Class<?>[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    List<Class<IDataModelAttribute>> filtered = ConfigurationUtility.filterClasses(all, IDataModelAttribute.class);
    for (Class<? extends IDataModelAttribute> c : ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelAttribute.class)) {
      try {
        IDataModelAttribute a = ConfigurationUtility.newInnerInstance(m_field, c);
        attributes.add(a);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return Collections.unmodifiableList(attributes);
  }

  @Override
  protected List<IDataModelEntity> createEntities() {
    List<IDataModelEntity> entities = new ArrayList<IDataModelEntity>();
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    List<Class<IDataModelEntity>> filtered = ConfigurationUtility.filterClasses(all, IDataModelEntity.class);
    for (Class<? extends IDataModelEntity> entityClazz : ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelEntity.class)) {
      try {
        entities.add(ConfigurationUtility.newInnerInstance(m_field, entityClazz));
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return Collections.unmodifiableList(entities);
  }

}
