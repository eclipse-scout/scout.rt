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
package org.eclipse.scout.rt.svg.ui.html;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.svg.client.svgfield.ISvgField;
import org.eclipse.scout.rt.svg.ui.html.svgfield.JsonSvgField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.IJsonObjectFactory;

@Bean
@Order(1000)
public class SvgJsonObjectFactory implements IJsonObjectFactory {

  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    if (model instanceof ISvgField) {
      return new JsonSvgField((ISvgField) model, session, id, parent);
    }
    return null;
  }

  @Override
  public IJsonObject createJsonObject(Object object) {
    return null;
  }

}
