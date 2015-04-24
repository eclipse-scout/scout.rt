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
package org.eclipse.scout.rt.client.ui.form.fields.plannerfield;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.IPlannerFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadResourcesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateResourcesChain;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

@ClassId("9520b5cc-221e-4d0f-8cc3-5c4a1ba06b77")
public abstract class AbstractPlannerField<P extends IPlanner<RI, AI>, RI, AI> extends AbstractFormField implements IPlannerField<P> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPlannerField.class);

  private IPlannerFieldUIFacade m_uiFacade;
  private P m_planner;

  public AbstractPlannerField() {
    this(true);
  }

  public AbstractPlannerField(boolean callInitializer) {
    super(callInitializer);
  }

  private Class<? extends IPlanner> getConfiguredPlanner() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IPlanner.class);
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredSplitterPosition() {
    return 168;
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
    loadResources();
  }

  /**
   * Load data matrix with the maximum of the following columns:
   * <ul>
   * <li>resourceId of type RI
   * <li>activityId of type AI
   * <li>startTime of type {@link Date}
   * <li>endTime of type {@link Date}
   * <li>text of type {@link String}
   * <li>tooltipText of type {@link String}
   * <li>iconId of type {@link String}
   * <li>majorValue of type {@link Number}
   * <li>minorValue of type {@link Number}
   * </ul>
   */
  @ConfigOperation
  @Order(10)
  protected List<Resource<RI>> execLoadResources() throws ProcessingException {
    return null;
  }

  /**
   * Interceptor is called after data was fetched from LookupCall and is adding
   * a table row for every LookupRow using IListBoxTable.createTableRow(row) and
   * ITable.addRows()
   * <p>
   * For most cases the override of just {@link #execLoadTableData()} is sufficient
   *
   * <pre>
   * Object[][] data = execLoadResourceTableData();
   * getResourceTable().replaceRowsByMatrix(data);
   * </pre>
   *
   * Load activity data<br>
   * By default loads data using {@link #interceptLoadPlannerData(List, List)}, transforms to {@link Activity}, maps to
   * resources using the resourceId, and sets the {@link Activity}s on the corresponding activtyRow.
   */
  @ConfigOperation
  @Order(20)
  protected void execPopulateResources() throws ProcessingException {
    List<Resource<RI>> resources = interceptLoadResources();
    getPlanner().replaceResources(resources);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initConfig() {
    m_uiFacade = new P_PlannerFieldUIFacade();
    super.initConfig();
    setSplitterPosition(getConfiguredSplitterPosition());

    List<IPlanner> contributedPlanners = m_contributionHolder.getContributionsByClass(IPlanner.class);
    m_planner = (P) CollectionUtility.firstElement(contributedPlanners);
    if (m_planner == null) {
      Class<? extends IPlanner> configuredPlanner = getConfiguredPlanner();
      if (configuredPlanner != null) {
        try {
          m_planner = (P) ConfigurationUtility.newInnerInstance(this, configuredPlanner);
        }
        catch (Exception e) {
          BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + configuredPlanner.getName() + "'.", e));
        }
      }
    }

    if (m_planner == null) {
      throw new IllegalStateException("No planner found.");
    }
    if (m_planner instanceof AbstractPlanner) {
      ((AbstractPlanner) m_planner).setContainerInternal(this);
    }
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
    getPlanner().initPlanner();
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    getPlanner().disposePlanner();
  }

  @Override
  public int getSplitterPosition() {
    return propertySupport.getPropertyInt(PROP_SPLITTER_POSITION);
  }

  @Override
  public void setSplitterPosition(int splitterPosition) {
    propertySupport.setPropertyInt(PROP_SPLITTER_POSITION, splitterPosition);
  }

  @Override
  public final P getPlanner() {
    return m_planner;
  }

  @Override
  public void loadResources() throws ProcessingException {
    interceptPopulateResources();
  }

  @Override
  public IPlannerFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_PlannerFieldUIFacade implements IPlannerFieldUIFacade {

    @Override
    public void refreshFromUI() {
      try {
        loadResources();
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    @Override
    public void setSplitterPositionFromUI(Integer value) {
      propertySupport.setPropertyNoFire(PROP_SPLITTER_POSITION, value);
    }
  }

  protected final List<Resource<RI>> interceptLoadResources() throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldLoadResourcesChain<P, RI, AI> chain = new PlannerFieldLoadResourcesChain<P, RI, AI>(extensions);
    return chain.execLoadResourceTableData();
  }

  protected final void interceptPopulateResources() throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PlannerFieldPopulateResourcesChain<P, RI, AI> chain = new PlannerFieldPopulateResourcesChain<P, RI, AI>(extensions);
    chain.execPopulateResources();
  }

  protected static class LocalPlannerFieldExtension<P extends IPlanner<RI, AI>, RI, AI, OWNER extends AbstractPlannerField<P, RI, AI>> extends LocalFormFieldExtension<OWNER> implements IPlannerFieldExtension<P, RI, AI, OWNER> {

    public LocalPlannerFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public List<Resource<RI>> execLoadResources(PlannerFieldLoadResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain) throws ProcessingException {
      return getOwner().execLoadResources();
    }

    @Override
    public void execPopulateResources(PlannerFieldPopulateResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain) throws ProcessingException {
      getOwner().execPopulateResources();
    }
  }

  @Override
  protected IPlannerFieldExtension<P, RI, AI, ? extends AbstractPlannerField<P, RI, AI>> createLocalExtension() {
    return new LocalPlannerFieldExtension<P, RI, AI, AbstractPlannerField<P, RI, AI>>(this);
  }

}
