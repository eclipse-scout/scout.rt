/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield.PageFieldChains.PageFieldPageChangedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;

public interface IPageFieldExtension<PAGE extends IPage, OWNER extends AbstractPageField<PAGE>> extends IGroupBoxExtension<OWNER> {

  void execPageChanged(PageFieldPageChangedChain<PAGE> chain, PAGE oldPage, PAGE newPage);
}
