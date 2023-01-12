/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield.PageFieldChains.PageFieldPageChangedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;

public interface IPageFieldExtension<PAGE extends IPage, OWNER extends AbstractPageField<PAGE>> extends IGroupBoxExtension<OWNER> {

  void execPageChanged(PageFieldPageChangedChain<PAGE> chain, PAGE oldPage, PAGE newPage);
}
