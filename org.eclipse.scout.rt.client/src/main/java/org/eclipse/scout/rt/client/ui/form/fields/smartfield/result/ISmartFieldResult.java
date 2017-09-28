/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ISmartFieldResult<LOOKUP_KEY> {

  IQueryParam<LOOKUP_KEY> getQueryParam();

  Throwable getException();

  List<ILookupRow<LOOKUP_KEY>> getLookupRows();

}
