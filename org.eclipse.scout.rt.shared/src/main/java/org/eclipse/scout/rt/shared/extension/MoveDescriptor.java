/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;

public class MoveDescriptor<MODEL_TYPE> {

  private final MODEL_TYPE m_model;
  private final ClassIdentifier m_newContainerIdentifier;
  private final Double m_newOrder;

  public MoveDescriptor(MODEL_TYPE model, ClassIdentifier newContainerIdentifier, Double newOrder) {
    m_model = model;
    m_newContainerIdentifier = newContainerIdentifier;
    m_newOrder = newOrder;
  }

  public MODEL_TYPE getModel() {
    return m_model;
  }

  public ClassIdentifier getNewContainerIdentifer() {
    return m_newContainerIdentifier;
  }

  public Double getNewOrder() {
    return m_newOrder;
  }

  @Override
  public String toString() {
    return "MoveDescriptor [m_model=" + m_model + ", m_newContainerIdentifier=" + m_newContainerIdentifier + ", m_newOrder=" + m_newOrder + "]";
  }
}
