/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;

public abstract class AbstractSearchFormExtension<OWNER extends AbstractSearchForm> extends AbstractFormExtension<OWNER> implements ISearchFormExtension<OWNER> {

  public AbstractSearchFormExtension(OWNER owner) {
    super(owner);
  }
}
