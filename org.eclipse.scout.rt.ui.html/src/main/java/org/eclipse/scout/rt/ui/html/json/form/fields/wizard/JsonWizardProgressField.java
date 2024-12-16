/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.IWizardProgressField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonWizardProgressField<WIZARD_PROGRESS_FIELD extends IWizardProgressField> extends JsonFormField<WIZARD_PROGRESS_FIELD> {

  private static final String PROP_ACTIVE_STEP_INDEX = "activeStepIndex";
  // from UI
  private static final String EVENT_DO_STEP_ACTION = "doStepAction";

  public JsonWizardProgressField(WIZARD_PROGRESS_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WizardProgressField";
  }

  @Override
  protected void initJsonProperties(WIZARD_PROGRESS_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IWizardProgressField.PROP_STEPS, model) {
      @Override
      protected List<IWizardStep<? extends IForm>> modelValue() {
        return getModel().getSteps();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        @SuppressWarnings("unchecked")
        List<IWizardStep<? extends IForm>> wizardSteps = (List<IWizardStep<? extends IForm>>) value;
        JSONArray jsonSteps = new JSONArray();
        if (wizardSteps != null) {
          for (IWizardStep<? extends IForm> wizardStep : wizardSteps) {
            if (wizardStep.isVisible()) {
              jsonSteps.put(wizardStepToJson(wizardStep));
            }
          }
        }
        return jsonSteps;
      }
    });
    putJsonProperty(new JsonProperty<>(IWizardProgressField.PROP_ACTIVE_STEP, model) {
      @Override
      protected IWizardStep<? extends IForm> modelValue() {
        return getModel().getActiveStep();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        @SuppressWarnings("unchecked")
        IWizardStep<? extends IForm> activeWizardStep = (IWizardStep<? extends IForm>) value;
        return getStepIndex(activeWizardStep);
      }

      @Override
      public String jsonPropertyName() {
        return PROP_ACTIVE_STEP_INDEX;
      }
    });
  }

  protected JSONObject wizardStepToJson(IWizardStep<? extends IForm> wizardStep) {
    JSONObject jsonStep = new JSONObject();
    jsonStep.put("index", getStepIndex(wizardStep));
    jsonStep.put("title", wizardStep.getTitle());
    jsonStep.put("subTitle", wizardStep.getSubTitle());
    jsonStep.put("tooltipText", wizardStep.getTooltipText());
    jsonStep.put("iconId", BinaryResourceUrlUtility.createIconUrl(wizardStep.getIconId()));
    jsonStep.put("enabled", wizardStep.isEnabled());
    jsonStep.put("actionEnabled", wizardStep.isActionEnabled());
    jsonStep.put("cssClass", wizardStep.getCssClass());
    jsonStep.put("finished", wizardStep.isFinished());
    BEANS.get(InspectorInfo.class).put(getUiSession().currentHttpRequest(), jsonStep, wizardStep, s -> INSPECTOR_ID_PROVIDER.get().getIdForWizardStep(s));
    return jsonStep;
  }

  protected Integer getStepIndex(IWizardStep<? extends IForm> wizardStep) {
    if (wizardStep != null) {
      IWizard wizard = getModel().getWizard();
      if (wizard != null) {
        return wizard.getStepIndex(wizardStep);
      }
    }
    return null;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_DO_STEP_ACTION.equals(event.getType())) {
      handleUiDoStepAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiDoStepAction(JsonEvent event) {
    int targetStepIndex = event.getData().optInt("stepIndex", -1);
    getModel().getUIFacade().stepActionFromUI(targetStepIndex);
  }
}
