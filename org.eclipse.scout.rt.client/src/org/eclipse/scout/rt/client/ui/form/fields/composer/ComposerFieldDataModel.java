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
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

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
    Class<?>[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    List<Class<IDataModelAttribute>> filtered = ConfigurationUtility.filterClasses(all, IDataModelAttribute.class);
    List<Class<? extends IDataModelAttribute>> sortedAndFiltered = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelAttribute.class);
    List<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>(sortedAndFiltered.size());
    for (Class<? extends IDataModelAttribute> c : sortedAndFiltered) {
      try {
        IDataModelAttribute a = ConfigurationUtility.newInnerInstance(m_field, c);
        attributes.add(a);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + c.getName() + "'.", e));
      }
    }
    return attributes;
  }

  @Override
  protected List<IDataModelEntity> createEntities() {
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(m_field.getClass());
    List<Class<IDataModelEntity>> filtered = ConfigurationUtility.filterClasses(all, IDataModelEntity.class);
    List<Class<? extends IDataModelEntity>> sortedAndFiltered = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelEntity.class);
    List<IDataModelEntity> entities = new ArrayList<IDataModelEntity>(sortedAndFiltered.size());
    for (Class<? extends IDataModelEntity> entityClazz : sortedAndFiltered) {
      try {
        entities.add(ConfigurationUtility.newInnerInstance(m_field, entityClazz));
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + entityClazz.getName() + "'.", e));
      }
    }
    return entities;
  }
}
