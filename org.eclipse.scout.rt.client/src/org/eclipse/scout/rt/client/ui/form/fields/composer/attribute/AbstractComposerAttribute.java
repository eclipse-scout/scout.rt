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
package org.eclipse.scout.rt.client.ui.form.fields.composer.attribute;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;

/**
 * @deprecated use {@link AbstractDataModelAttribute}. Will be removed in the M-Release.
 */
@SuppressWarnings("deprecation")
@Deprecated
public abstract class AbstractComposerAttribute extends AbstractDataModelAttribute implements IComposerAttribute {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerAttribute.class);
  private static final long serialVersionUID = 1L;

  private String m_id;

  public AbstractComposerAttribute() {
  }

  /*
   * Configuration
   */

  /**
   * @deprecated the id must always by the class simple name
   *             for dynamic attributes use {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)}.
   *             Will be removed in the 5.0 Release.
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredId() {
    return null;
  }

  @Override
  protected void initConfig() {
    setId(getConfiguredId());
    super.initConfig();
  }

  /*
   * Runtime
   */

  @Override
  protected void injectOperators() {
    if (getLegacyStatement() != null) {
      new LegacyComposerAttributeInjector().injectOperators(this);
    }
    else {
      super.injectOperators();
    }
  }

  @Override
  protected void injectAggregationTypes() {
    if (getLegacyStatement() != null) {
      new LegacyComposerAttributeInjector().injectAggregationTypes(this);
    }
    else {
      super.injectAggregationTypes();
    }
  }

  @Override
  public String getId() {
    if (m_id != null) {
      return m_id;
    }
    else {
      return getClass().getSimpleName();
    }
  }

  @Override
  public void setId(String s) {
    m_id = s;
  }

  /**
   * @deprecated processing logic belongs to server. Will be removed in the 5.0 Release.
   */
  @Deprecated
  protected String getConfiguredStatement() {
    return null;
  }

  /**
   * @deprecated processing logic belongs to server. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public String getLegacyStatement() {
    return getConfiguredStatement();
  }

}
