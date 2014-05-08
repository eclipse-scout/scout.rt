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
package org.eclipse.scout.rt.ui.json.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.form.fields.JsonValueField;

/**
 * @author awe
 */
public class JsonCheckBoxField extends JsonValueField<IBooleanField> {

	public JsonCheckBoxField(IBooleanField model, IJsonSession session) {
		super(model, session);
	}

	@Override
	public String getObjectType() {
		return "CheckBoxField";
	}

}
