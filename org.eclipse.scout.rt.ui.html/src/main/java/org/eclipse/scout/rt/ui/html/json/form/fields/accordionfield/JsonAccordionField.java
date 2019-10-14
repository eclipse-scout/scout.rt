/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.accordionfield;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.IAccordionField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;

public class JsonAccordionField<T extends IAccordionField<? extends IAccordion>> extends JsonFormField<T> implements IBinaryResourceConsumer {

  public JsonAccordionField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "AccordionField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IAccordionField.PROP_ACCORDION, model, getUiSession()) {
      @Override
      protected IAccordion modelValue() {
        return getModel().getAccordion();
      }
    });
    putJsonProperty(new JsonProperty<T>(IAccordionField.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<T>(IAccordionField.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if ((getModel().getDropType() & IDNDSupport.TYPE_FILE_TRANSFER) == IDNDSupport.TYPE_FILE_TRANSFER) {
      ResourceListTransferObject transferObject = new ResourceListTransferObject(binaryResources);
      getModel().getUIFacade().fireDropActionFromUI(transferObject);
    }
  }

  @Override
  public long getMaximumUploadSize() {
    return getModel().getDropMaximumSize();
  }
}
