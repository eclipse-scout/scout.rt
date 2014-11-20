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
package org.eclipse.scout.rt.ui.html.json.form.fields.richtextfield;

import org.eclipse.scout.rt.client.ui.form.fields.richtextfield.IRichTextField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonRichTextField<T extends IRichTextField> extends JsonValueField<T> {

  public JsonRichTextField(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "RichTextField";
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText, boolean whileTyping) {
  }

}
