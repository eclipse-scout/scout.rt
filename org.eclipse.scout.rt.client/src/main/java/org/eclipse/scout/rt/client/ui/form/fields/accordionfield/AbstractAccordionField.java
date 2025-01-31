/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.accordionfield;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AccordionFieldChains.AccordionFieldDropRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.IAccordionFieldExtension;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("2ede595d-acc7-43ef-bda7-288cc5fcdc91")
public abstract class AbstractAccordionField<T extends IAccordion> extends AbstractFormField implements IAccordionField<T> {
  private IAccordionFieldUIFacade<T> m_uiFacade;

  public AbstractAccordionField() {
    this(true);
  }

  public AbstractAccordionField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

    super.initConfig();
    setAccordion(createAccordion());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setDropType(getConfiguredDropType());
    setDragType(getConfiguredDragType());
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

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(10)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * Configures the drop support of this field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(20)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the drag support of this field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(30)
  protected int getConfiguredDragType() {
    return 0;
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

    if (oldAccordion != null) {
      oldAccordion.setParentInternal(null);
    }
    propertySupport.setProperty(PROP_ACCORDION, accordion);
    if (accordion != null) {
      accordion.setParentInternal(this);
    }
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(IDNDSupport.PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(IDNDSupport.PROP_DROP_TYPE);
  }

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(IDNDSupport.PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(IDNDSupport.PROP_DROP_MAXIMUM_SIZE);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getAccordion()));
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
  protected boolean execIsEmpty() {
    if (!super.execIsEmpty()) {
      return false;
    }
    return getAccordion().getGroups().isEmpty();
  }

  @ConfigOperation
  @Order(10)
  protected void execDropRequest(TransferObject transferObject) {
  }

  @ConfigOperation
  @Order(20)
  protected TransferObject execDragRequest() {
    return null;
  }

  /*
   * UI accessible
   */
  @Override
  public IAccordionFieldUIFacade<T> getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IAccordionFieldUIFacade<T> {

    @Override
    public TransferObject fireDragRequestFromUI() {
      TransferObject t = null;
      t = interceptDragRequest();
      return t;
    }

    @Override
    public void fireDropActionFromUI(TransferObject scoutTransferable) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        //can not drop anything into field if its disabled.
        return;
      }
      interceptDropRequest(scoutTransferable);
    }
  }

  @Override
  protected IAccordionFieldExtension<T, ? extends AbstractAccordionField> createLocalExtension() {
    return new LocalAccordionFieldExtension<>(this);
  }

  protected static class LocalAccordionFieldExtension<T extends IAccordion, OWNER extends AbstractAccordionField<T>> extends LocalFormFieldExtension<OWNER> implements IAccordionFieldExtension<T, OWNER> {

    public LocalAccordionFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public TransferObject execDragRequest(AccordionFieldDragRequestChain chain) {
      return getOwner().execDragRequest();
    }

    @Override
    public void execDropRequest(AccordionFieldDropRequestChain chain, TransferObject transferObject) {
      getOwner().execDropRequest(transferObject);
    }
  }

  protected final TransferObject interceptDragRequest() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    AccordionFieldDragRequestChain<T> chain = new AccordionFieldDragRequestChain<>(extensions);
    return chain.execDragRequest();
  }

  protected final void interceptDropRequest(TransferObject transferObject) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    AccordionFieldDropRequestChain<T> chain = new AccordionFieldDropRequestChain<>(extensions);
    chain.execDropRequest(transferObject);
  }
}
