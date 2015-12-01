/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.net.URL;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.ResourceBase;

/**
 * @since 5.2
 */
@Order(5500)
public class DefaultValuesConfigurationContributor implements IDefaultValuesConfigurationContributor {

  @Override
  public URL contributeDefaultValuesConfigurationUrl() {
    return ResourceBase.class.getResource("json/defaultValues.json");
  }
}
