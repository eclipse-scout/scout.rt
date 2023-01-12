/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.mode;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("b424e0e3-e0a3-4056-a8e5-3f7a8262f668")
public abstract class AbstractMode<T> extends AbstractAction implements IMode<T> {

  public AbstractMode() {
    this(true);
  }

  public AbstractMode(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setRef(getConfiguredRef());
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(1000)
  protected T getConfiguredRef() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getRef() {
    return (T) propertySupport.getProperty(PROP_REF);
  }

  @Override
  public void setRef(T value) {
    propertySupport.setProperty(PROP_REF, value);
  }
}
