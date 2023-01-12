/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.js;

import org.eclipse.scout.rt.client.ui.form.IFormUIFacade;
import org.eclipse.scout.rt.dataobject.IDataObject;

public interface IJsFormUIFacade<OUT extends IDataObject> extends IFormUIFacade {

  void fireSaveFromUI(OUT outputData);
}
