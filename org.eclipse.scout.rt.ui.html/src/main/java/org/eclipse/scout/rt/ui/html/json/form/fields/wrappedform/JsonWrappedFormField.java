/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonWrappedFormField<WRAPPED_FORM_FIELD extends IWrappedFormField<? extends IForm>> extends JsonFormField<WRAPPED_FORM_FIELD> {

  private final FormListener m_innerFormListener = this::handleInnerFormEvent;

  public JsonWrappedFormField(WRAPPED_FORM_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WrappedFormField";
  }

  @Override
  protected void initJsonProperties(WRAPPED_FORM_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<WRAPPED_FORM_FIELD>(IWrappedFormField.PROP_INNER_FORM, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }

      @Override
      protected IForm modelValue() {
        return getModel().getInnerForm();
      }
    });
    putJsonProperty(new JsonProperty<WRAPPED_FORM_FIELD>(IWrappedFormField.PROP_INITIAL_FOCUS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInitialFocusEnabled();
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    final IForm innerForm = getModel().getInnerForm();
    if (innerForm != null) {
      innerForm.addFormListener(m_innerFormListener);
    }
  }

  @Override
  protected void detachModel() {
    final IForm innerForm = getModel().getInnerForm();
    if (innerForm != null) {
      innerForm.removeFormListener(m_innerFormListener);
    }
    super.detachModel();
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    super.handleModelPropertyChange(event);
    if (IWrappedFormField.PROP_INNER_FORM.equals(event.getPropertyName())) {
      if (event.getOldValue() instanceof IForm) {
        ((IForm) event.getOldValue()).removeFormListener(m_innerFormListener);
      }
      if (event.getNewValue() instanceof IForm) {
        ((IForm) event.getNewValue()).addFormListener(m_innerFormListener);
      }
    }
  }

  protected void handleInnerFormEvent(FormEvent e) {
    if (e.getType() == FormEvent.TYPE_CLOSED) {
      // If the inner form is closed, it is disposed automatically. Therefore, from the UI's perspective, the JsonWrappedFormField
      // has behave as if the inner form was removed, although the actual reference might still be present (if the inner form's
      // life-cycle is externally managed). To do so, we generate a artificial event with PROP_INNER_FORM=null.
      handleModelPropertyChange(new PropertyChangeEvent(getModel(), IWrappedFormField.PROP_INNER_FORM, e.getForm(), null));
    }
  }
}
