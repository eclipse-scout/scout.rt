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
package org.eclipse.scout.rt.client.ui.form.fields.composer.entity;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;

/**
 * @deprecated use {@link AbstractDataModelEntity}
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractComposerEntity extends AbstractDataModelEntity implements IComposerEntity {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerEntity.class);

  private String m_id;

  public AbstractComposerEntity() {
  }

  /**
   * @deprecated the id must always by the class simple name
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  @ConfigPropertyValue("null")
  protected String getConfiguredId() {
    return null;
  }

  @Override
  protected void initConfig() {
    setId(getConfiguredId());
    super.initConfig();
  }

  public String getId() {
    if (m_id != null) return m_id;
    else return getClass().getSimpleName();
  }

  public void setId(String s) {
    m_id = s;
  }

  /**
   * @deprecated processing logic belongs to server
   */
  @Deprecated
  protected String getConfiguredStatement() {
    return null;
  }

  /**
   * @deprecated processing logic belongs to server
   */
  @Deprecated
  public String getLegacyStatement() {
    return getConfiguredStatement();
  }

}
