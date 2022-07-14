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

import java.lang.reflect.ParameterizedType;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Assertions;

@ClassId("371cbcfe-b79b-4d20-acfb-2a1b5ca375bf")
public abstract class AbstractJsForm<IN extends IDataObject, OUT extends IDataObject> extends AbstractForm implements IJsForm<IN, OUT> {

  private IJsFormUIFacade<OUT> m_uiFacade;

  private final Class<IN> m_inputDataType;
  private final Class<OUT> m_outputDataType;

  // Transport from UI to model (different threads)
  private volatile OUT m_outputData;

  public AbstractJsForm() {
    this(true);
  }

  public AbstractJsForm(boolean callInitializer) {
    super(false);

    Class<?> clazz = getClass();
    while (clazz.getSuperclass() != AbstractJsForm.class) {
      clazz = clazz.getSuperclass();
    }

    ParameterizedType parameterizedType = Assertions.assertInstance(clazz.getGenericSuperclass(), ParameterizedType.class);

    //noinspection unchecked
    m_inputDataType = (Class<IN>) parameterizedType.getActualTypeArguments()[0];
    //noinspection unchecked
    m_outputDataType = (Class<OUT>) parameterizedType.getActualTypeArguments()[1];
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setJsFormObjectType(getConfiguredJsFormObjectType());
    setJsFormModel(getConfiguredJsFormModel());
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  /**
   * @return the objectType of the jsForm to be opened
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(210)
  protected String getConfiguredJsFormObjectType() {
    return null;
  }

  /**
   * @return additional model for the jsForm
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(220)
  protected IDoEntity getConfiguredJsFormModel() {
    return null;
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  public IJsFormUIFacade<OUT> getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public Class<IN> getInputDataType() {
    return m_inputDataType;
  }

  @Override
  public Class<OUT> getOutputDataType() {
    return m_outputDataType;
  }

  @Override
  public IN getInputData() {
    //noinspection unchecked
    return (IN) propertySupport.getProperty(PROP_INPUT_DATA);
  }

  @Override
  public void setInputData(IN inputData) {
    propertySupport.setProperty(PROP_INPUT_DATA, inputData);
  }

  @Override
  public String getJsFormObjectType() {
    return propertySupport.getPropertyString(PROP_JS_FORM_OBJECT_TYPE);
  }

  @Override
  public void setJsFormObjectType(String jsFormObjectType) {
    propertySupport.setPropertyString(PROP_JS_FORM_OBJECT_TYPE, jsFormObjectType);
  }

  @Override
  public IDoEntity getJsFormModel() {
    return (IDoEntity) propertySupport.getProperty(PROP_JS_FORM_MODEL);
  }

  @Override
  public void setJsFormModel(IDoEntity jsFormModel) {
    propertySupport.setProperty(PROP_JS_FORM_MODEL, jsFormModel);
  }

  @Override
  public OUT getOutputData() {
    return m_outputData;
  }

  protected void setOutputData(OUT outputData) {
    m_outputData = outputData;
    touch();
  }

  protected void save(OUT outputData) {
    setOutputData(outputData);
    doSave();
  }

  @Order(1000)
  @ClassId("4d09d51a-2a15-4863-8921-2eead40957fa")
  public class MainBox extends AbstractGroupBox {
  }

  protected class P_UIFacade extends AbstractForm.P_UIFacade implements IJsFormUIFacade<OUT> {

    @Override
    public void fireSaveFromUI(OUT outputData) {
      save(outputData);
    }
  }
}
