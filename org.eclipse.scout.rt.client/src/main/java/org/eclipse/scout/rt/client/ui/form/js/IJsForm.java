/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.js;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Wraps a form implemented in ScoutJS. This jsForm will be opened by the JsFormAdapter and its whole lifecycle will be
 * handled by the browser. To determine which jsForm is opened the property {@code jsFormObjectType} is used, an
 * additional model can be passed using {@code jsFormModel}. It is possible to pass some {@code inputData} into the
 * jsForm and retrieve an {@code outputData} after the jsForm is closed.
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
