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
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public abstract class AbstractWrappedFormField<T extends IForm> extends AbstractFormField implements IWrappedFormField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractWrappedFormField.class);

  private T m_innerForm;
  private P_InnerFormPropertyChangeListener m_innerFormPropertyListener;
  private P_InnerFormSubtreePropertyChangeListener m_innerFormSubtreePropertyListener;

  public AbstractWrappedFormField() {
    super();
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.FORM)
  @Order(200)
  @ConfigPropertyValue("null")
  protected Class<? extends IForm> getConfiguredInnerForm() {
    return null;
  }

  @ConfigPropertyValue("1")
  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  @Override
  @ConfigPropertyValue("true")
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected void execInitField() throws ProcessingException {
    IForm f = getInnerForm();
    if (f != null && !f.isFormOpen()) {
      if (f instanceof ISearchForm) {
        ((ISearchForm) f).startSearch();
      }
    }
  }

  @Override
  protected boolean execIsSaveNeeded() throws ProcessingException {
    return getInnerForm() != null && getInnerForm().isSaveNeeded();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initConfig() {
    super.initConfig();
    m_innerFormPropertyListener = new P_InnerFormPropertyChangeListener();
    m_innerFormSubtreePropertyListener = new P_InnerFormSubtreePropertyChangeListener();
    if (getConfiguredInnerForm() != null) {
      try {
        setInnerForm((T) getConfiguredInnerForm().newInstance());
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
  }

  /**
   * propagate both enabled levels to inner form
   */
  @Override
  protected void calculateEnabled() {
    super.calculateEnabled();
    if (getInnerForm() != null) {
      getInnerForm().setEnabledGranted(isEnabledGranted());
      getInnerForm().setAllEnabled(getEnabledProperty());
    }
  }

  public final T getInnerForm() {
    return m_innerForm;
  }

  public void setInnerForm(T form) {
    if (m_innerForm == form) {
      return;
    }
    if (m_innerForm != null) {
      fireSubtreePropertyChange(new PropertyChangeEvent(m_innerForm.getRootGroupBox(), IFormField.PROP_PARENT_FIELD, null, null));
      m_innerForm.removePropertyChangeListener(m_innerFormPropertyListener);
      m_innerForm.getRootGroupBox().removeSubtreePropertyChangeListener(m_innerFormSubtreePropertyListener);
      m_innerForm.setWrapperFieldInternal(null);
      m_innerForm = null;
    }
    m_innerForm = form;
    if (m_innerForm != null) {
      if (!m_innerForm.isFormOpen()) {
        m_innerForm.setModal(false);
        m_innerForm.setAutoAddRemoveOnDesktop(false);
      }
      m_innerForm.setWrapperFieldInternal(this);
      m_innerForm.getRootGroupBox().setBorderVisible(false);
      m_innerForm.getRootGroupBox().updateKeyStrokes();
      m_innerForm.addPropertyChangeListener(m_innerFormPropertyListener);
      m_innerForm.getRootGroupBox().addSubtreePropertyChangeListener(m_innerFormSubtreePropertyListener);
    }
    boolean changed = propertySupport.setProperty(PROP_INNER_FORM, m_innerForm);
    calculateVisibleInternal();
    if (m_innerForm != null) {
      fireSubtreePropertyChange(new PropertyChangeEvent(m_innerForm.getRootGroupBox(), IFormField.PROP_PARENT_FIELD, null, null));
    }
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    if (getInnerForm() != null) {
      return getInnerForm().getRootGroupBox().visitFields(visitor, startLevel);
    }
    return true;
  }

  @Override
  public void loadXML(SimpleXmlElement x) throws ProcessingException {
    super.loadXML(x);
    if (getInnerForm() != null) {
      getInnerForm().loadXML(x);
    }
  }

  @Override
  public void storeXML(SimpleXmlElement x) throws ProcessingException {
    super.storeXML(x);
    if (getInnerForm() != null) {
      getInnerForm().storeXML(x);
    }
  }

  /*
   * Do not make wrapped form field auto-invisible when no form is hosted. For
   * example in wizard forms this leads to (correct) but unexpected layout flow.
   * The following line might be added by developers in their own subclass to
   * have auto-invisible behaviour.
   */
  /*
   * @Override protected boolean execCalculateVisible() { return getInnerForm()
   * != null; }
   */

  // group box is only visible when it has at least one visible item
  protected void handleFieldVisibilityChanged() {
    calculateVisibleInternal();
  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not
   * groups)
   */
  private class P_InnerFormPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
        // fire group box visibility
        handleFieldVisibilityChanged();
      }
      else if (e.getPropertyName().equals(IFormField.PROP_SAVE_NEEDED)) {
        checkSaveNeeded();
      }
    }
  }// end private class

  private class P_InnerFormSubtreePropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      fireSubtreePropertyChange(e);
    }
  }// end private class

}
