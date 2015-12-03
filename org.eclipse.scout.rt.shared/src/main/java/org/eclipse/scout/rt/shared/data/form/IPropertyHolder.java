/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
