/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.TabBoxChains.TabBoxTabSelectedChain;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;

public interface ITabBoxExtension<OWNER extends AbstractTabBox> extends ICompositeFieldExtension<OWNER> {

  void execTabSelected(TabBoxTabSelectedChain chain, IGroupBox selectedBox);
}
