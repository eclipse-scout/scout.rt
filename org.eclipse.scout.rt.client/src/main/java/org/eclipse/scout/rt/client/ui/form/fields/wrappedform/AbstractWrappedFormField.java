/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform.IWrappedFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform.WrappedFormFieldChains.WrappedFormFieldInnerFormChangedChain;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.w3c.dom.Element;

@ClassId("535cfd11-39cf-4804-beef-2bc1bc3d34cc")
public abstract class AbstractWrappedFormField<FORM extends IForm> extends AbstractFormField implements IWrappedFormField<FORM> {

  private FORM m_innerForm;
  private boolean m_manageInnerFormLifeCycle;
  private P_InnerFormPropertyChangeListener m_innerFormPropertyListener;
  private P_InnerFormSubtreePropertyChangeListener m_innerFormSubtreePropertyListener;
  private P_InnerFormListener m_innerFormListener;

  public AbstractWrappedFormField() {
    this(true);
  }

  public AbstractWrappedFormField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  @Override
  protected void setFormOnChildren(IForm form) {
    // do not propagate the outer form into the form-fields of the inner form.
  }

  /**
   * If set, an instance of this form class is created on field initialization and is then set as inner form. The form's
   * life cycle is managed automatically by the field (i.e. it is started and closed).
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(200)
  protected Class<? extends IForm> getConfiguredInnerForm() {
    return null;
  }

  /**
   * @return {@code true} if the inner form should request the initial focus once loaded, {@code false} otherwise.
   *         Default is {@code false}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(210)
  protected boolean getConfiguredInitialFocusEnabled() {
    return false;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected boolean execIsSaveNeeded() {
    boolean saveNeeded = super.execIsSaveNeeded();
    if (saveNeeded) {
      return true;
    }
    return getInnerForm() != null && getInnerForm().isSaveNeeded();
  }

  @Override
  protected void execMarkSaved() {
    if (getInnerForm() != null) {
      getInnerForm().markSaved();
    }
  }

  @Override
  protected boolean execIsEmpty() {
    if (getInnerForm() != null) {
      return getInnerForm().isEmpty();
    }
    return super.execIsEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initConfig() {
    super.initConfig();
    m_innerFormPropertyListener = new P_InnerFormPropertyChangeListener();
    m_innerFormSubtreePropertyListener = new P_InnerFormSubtreePropertyChangeListener();
    m_innerFormListener = new P_InnerFormListener();
    setInitialFocusEnabled(getConfiguredInitialFocusEnabled());
    if (getConfiguredInnerForm() != null) {
      try {
        setInnerForm((FORM) getConfiguredInnerForm().getConstructor().newInstance(), true);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + getConfiguredInnerForm().getName() + "'.", e));
      }
    }
  }

  @Override
  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    widgetsToDispose.remove(getInnerForm()); // is disposed using IForm#doClose in disposeDefaultDetailForm()
    super.disposeChildren(widgetsToDispose);
  }

  @Override
  protected void initChildren(List<? extends IWidget> widgets) {
    widgets.remove(getInnerForm()); // is initialized later
    super.initChildren(widgets);
  }

  @Override
  protected void disposeFieldInternal() {
    // Remove listeners, close the form if life cycle is not externally managed
    uninstallInnerForm();
    super.disposeFieldInternal();
  }

  @Override
  public final FORM getInnerForm() {
    return m_innerForm;
  }

  @Override
  public void setInnerForm(FORM form) {
    setInnerForm(form, true);
  }

  @Override
  public void setInnerForm(FORM form, boolean manageFormLifeCycle) {
    if (m_innerForm == form) {
      return;
    }

    // TODO [7.0] bsh: Add assertion to ensure Form is not started yet; currently, that cannot be done because of AbstractPageField.
    // --> Assertions.assertFalse(form != null && form.isFormStarted(), "Inner Form must not be started yet [wrappedFormField=%s, innerForm=%s]", this, form)
    // TODO [7.0] bsh: Check if the above to-do would better be solved using "form.isShowing()". But what about forms that are started and contained in wrapper field 1 and are added to wrapper field 2?
    // Example:  Would work with "isFormStarted()":                      Would _not_ work with "isFormStarted()", but would work with "isShowing()".
    //           ----------------------------------                      ---------------------------------------------------------------------------
    //           TestForm form = new TestForm();                         TestForm form = new TestForm()
    //           form.setHandler(form.new TestHandler());                form.setHandler(form.new TestHandler())
    //           form.start();                                           form.setShowOnStart(false) // <--
    //           getWrappedFormField().setInnerForm(form, false);        form.start()
    //                                                                   getWrappedFormField().setInnerForm(form, false)

    FORM oldInnerForm = m_innerForm;
    uninstallInnerForm();
    m_innerForm = form;
    m_manageInnerFormLifeCycle = manageFormLifeCycle;
    installInnerForm();

    propertySupport.setProperty(PROP_INNER_FORM, m_innerForm);
    calculateVisibleInternal();
    checkSaveNeeded();
    checkEmpty();
    if (m_innerForm != null) {
      fireSubtreePropertyChange(new PropertyChangeEvent(m_innerForm.getRootGroupBox(), PROP_PARENT_WIDGET, null, null));
      if (m_manageInnerFormLifeCycle && m_innerForm.isFormStartable()) { // TODO [7.0] bsh: Remove 'started check' once assertion is in place
        m_innerForm.start();
      }
    }

    // Inform parent form (update layout etc.)
    if (getForm() != null) {
      getForm().structureChanged(this);
    }

    interceptInnerFormChanged(oldInnerForm, m_innerForm);
  }

  @Override
  public boolean isManageInnerFormLifeCycle() {
    return m_manageInnerFormLifeCycle;
  }

  @Override
  public boolean isInitialFocusEnabled() {
    return propertySupport.getPropertyBool(PROP_INITIAL_FOCUS_ENABLED);
  }

  @Override
  public void setInitialFocusEnabled(boolean initialFocusEnabled) {
    propertySupport.setPropertyBool(PROP_INITIAL_FOCUS_ENABLED, initialFocusEnabled);
  }

  protected void installInnerForm() {
    if (m_innerForm == null) {
      return;
    }

    m_innerForm.setShowOnStart(false);
    m_innerForm.setParentInternal(this);
    m_innerForm.getRootGroupBox().setBorderVisible(false);
    m_innerForm.getRootGroupBox().updateKeyStrokes();
    m_innerForm.addPropertyChangeListener(m_innerFormPropertyListener);
    m_innerForm.getRootGroupBox().addSubtreePropertyChangeListener(m_innerFormSubtreePropertyListener);
    m_innerForm.addFormListener(m_innerFormListener);
  }

  protected void uninstallInnerForm() {
    if (m_innerForm == null) {
      return;
    }

    fireSubtreePropertyChange(new PropertyChangeEvent(m_innerForm.getRootGroupBox(), PROP_PARENT_WIDGET, null, null));
    m_innerForm.removePropertyChangeListener(m_innerFormPropertyListener);
    m_innerForm.getRootGroupBox().removeSubtreePropertyChangeListener(m_innerFormSubtreePropertyListener);
    m_innerForm.removeFormListener(m_innerFormListener);
    m_innerForm.setParentInternal(null);
    if (m_manageInnerFormLifeCycle && !m_innerForm.isFormClosed()) {
      m_innerForm.doClose();
    }
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getInnerForm()));
  }

  @Override
  public void loadFromXml(Element x) {
    super.loadFromXml(x);
    if (getInnerForm() != null) {
      getInnerForm().loadFromXml(x);
    }
  }

  @Override
  public void storeToXml(Element x) {
    super.storeToXml(x);
    if (getInnerForm() != null) {
      getInnerForm().storeToXml(x);
    }
  }

  // group box is only visible when it has at least one visible item
  protected void handleFieldVisibilityChanged() {
    calculateVisibleInternal();
  }

  /**
   * Method invoked once the inner Form is changed.
   *
   * @param oldInnerForm
   *          the old inner {@link IForm}; might be <code>null</code>.
   * @param newInnerForm
   *          the new inner {@link IForm}; might be <code>null</code>.
   */
  @ConfigOperation
  protected void execInnerFormChanged(FORM oldInnerForm, FORM newInnerForm) {
  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not groups)
   */
  private class P_InnerFormPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
        // fire group box visibility
        handleFieldVisibilityChanged();
      }
      else if (e.getPropertyName().equals(IFormField.PROP_SAVE_NEEDED)) {
        checkSaveNeeded();
      }
      else if (e.getPropertyName().equals(IFormField.PROP_EMPTY)) {
        checkEmpty();
      }
    }
  }// end private class

  private class P_InnerFormSubtreePropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      fireSubtreePropertyChange(e);
    }
  }// end private class

  private class P_InnerFormListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) {
      if (e.getType() == FormEvent.TYPE_CLOSED && m_manageInnerFormLifeCycle) {
        setInnerForm(null, true);
      }
      else if (e.getType() == FormEvent.TYPE_LOAD_COMPLETE && !m_manageInnerFormLifeCycle) {
        propertySupport.setPropertyAlwaysFire(PROP_INNER_FORM, m_innerForm);
      }
    }
  }// end private class

  protected final void interceptInnerFormChanged(FORM oldInnerForm, FORM newInnerForm) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    WrappedFormFieldInnerFormChangedChain<FORM> chain = new WrappedFormFieldInnerFormChangedChain<>(extensions);
    chain.execInnerFormChanged(oldInnerForm, newInnerForm);
  }

  protected static class LocalWrappedFormFieldExtension<FORM extends IForm, OWNER extends AbstractWrappedFormField<FORM>> extends LocalFormFieldExtension<OWNER> implements IWrappedFormFieldExtension<FORM, OWNER> {

    public LocalWrappedFormFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInnerFormChanged(WrappedFormFieldInnerFormChangedChain<FORM> chain, FORM oldInnerForm, FORM newInnerForm) {
      getOwner().execInnerFormChanged(oldInnerForm, newInnerForm);
    }
  }

  @Override
  protected IWrappedFormFieldExtension<FORM, ? extends AbstractWrappedFormField<FORM>> createLocalExtension() {
    return new LocalWrappedFormFieldExtension<>(this);
  }
}
