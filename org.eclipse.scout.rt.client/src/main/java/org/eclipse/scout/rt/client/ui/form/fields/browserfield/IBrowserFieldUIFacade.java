/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import org.eclipse.scout.rt.platform.resource.BinaryResource;

public interface IBrowserFieldUIFacade {

  void firePostExternalWindowStateFromUI(boolean windowState);

  void firePostMessageFromUI(Object data, String origin);

  BinaryResource requestBinaryResourceFromUI(String filename);

  void setLocationFromUI(String location);
}
