/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form;

import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;

public interface IPropertyHolder extends IContributionOwner {

  /**
   * @param id
   * @return
   */
  AbstractPropertyData getPropertyById(String id);

  /**
   * @param c
   * @return
   */
  <T extends AbstractPropertyData> T getPropertyByClass(Class<T> c);

  /**
   * @return
   */
  AbstractPropertyData[] getAllProperties();

  /**
   * @param c
   * @param v
   */
  <T extends AbstractPropertyData> void setPropertyByClass(Class<T> c, T v);
}
