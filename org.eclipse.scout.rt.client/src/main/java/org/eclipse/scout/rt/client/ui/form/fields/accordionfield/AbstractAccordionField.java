/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.accordionfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("2ede595d-acc7-43ef-bda7-288cc5fcdc91")
public abstract class AbstractAccordionField<T extends IAccordion> extends AbstractFormField implements IAccordionField<T> {

  public AbstractAccordionField() {
    this(true);
  }

  public AbstractAccordionField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setAccordion(createAccordion());
  }

  @SuppressWarnings("unchecked")
  protected T createAccordion() {
    List<IAccordion> contributedFields = m_contributionHolder.getContributionsByClass(IAccordion.class);
    IAccordion result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return (T) result;
    }

    Class<? extends IAccordion> configuredAccordion = getConfiguredAccordion();
    if (configuredAccordion != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredAccordion);
    }
    return null;
  }

  private Class<? extends IAccordion> getConfiguredAccordion() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IAccordion.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getAccordion() {
    return (T) propertySupport.getProperty(PROP_ACCORDION);
  }

  @Override
  public void setAccordion(T accordion) {
    T oldAccordion = getAccordion();
    if (oldAccordion == accordion) {
      return;
    }

    if (oldAccordion instanceof AbstractAccordion) {
      ((AbstractAccordion) oldAccordion).setContainerInternal(null);
    }
    propertySupport.setProperty(PROP_ACCORDION, accordion);
    if (accordion instanceof AbstractAccordion) {
      ((AbstractAccordion) accordion).setContainerInternal(this);
    }
  }

  @Override
  protected void initFieldInternal() {
    super.initFieldInternal();
    if (getAccordion() != null) {
      getAccordion().init();
    }
  }

  @Override
  protected void postInitConfigInternal() {
    super.postInitConfigInternal();
    if (getAccordion() != null) {
      getAccordion().postInitConfig();
    }
  }

  @Override
  protected void disposeFieldInternal() {
    if (getAccordion() != null) {
      getAccordion().dispose();
    }
    super.disposeFieldInternal();
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a accordion field is 3.
   */
  @Override
  protected int getConfiguredGridH() {
    return 3;
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected boolean execIsEmpty() {
    return getAccordion().getGroups().isEmpty();
  }

}
