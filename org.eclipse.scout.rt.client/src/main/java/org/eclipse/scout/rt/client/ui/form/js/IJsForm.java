/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.js;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Wraps a form implemented in Scout JS.
 * <p>
 * The jsForm will be opened by the JsFormAdapter and its whole lifecycle will be handled by the browser. To determine
 * which jsForm will be opened, the property {@code jsFormObjectType} is used.
 * <p>
 * Except for {@link IForm#getDisplayHint()} and {@link IForm#getDisplayParent()}, no form properties will be sent to
 * the browser, they need to be set using JavaScript. However, it is possible to set some properties from Java code by
 * passing an additional model using {@link IJsForm#setJsFormModel(IDoEntity)}.
 * <p>
 * To set the data property of the jsForm, use {@link IJsForm#setInputData(IDataObject)}. Once the jsForm is closed, the
 * updated data will be sent back to the server and can be retrieved using {@link IJsForm#getOutputData()}.
 */
public interface IJsForm<IN extends IDataObject, OUT extends IDataObject> extends IForm {

  String PROP_INPUT_DATA = "inputData";
  String PROP_JS_FORM_OBJECT_TYPE = "jsFormObjectType";
  String PROP_JS_FORM_MODEL = "jsFormModel";

  @Override
  IJsFormUIFacade<OUT> getUIFacade();

  Class<IN> getInputDataType();

  Class<OUT> getOutputDataType();

  void setInputData(IN inputData);

  IN getInputData();

  OUT getOutputData();

  String getJsFormObjectType();

  void setJsFormObjectType(String jsFormObjectType);

  IDoEntity getJsFormModel();

  void setJsFormModel(IDoEntity jsFormModel);
}
