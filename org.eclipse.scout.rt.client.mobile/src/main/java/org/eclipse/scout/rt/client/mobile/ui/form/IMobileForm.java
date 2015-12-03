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
package org.eclipse.scout.rt.client.mobile.ui.form;

import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public interface IMobileForm extends IForm {
  String PROP_HEADER_VISIBLE = "headerVisible";
  String PROP_FOOTER_VISIBLE = "footerVisible";
  String PROP_HEADER_ACTION_FETCHER = "headerActionFetcher";
  String PROP_FOOTER_ACTION_FETCHER = "footerActionFetcher";

  boolean isHeaderVisible();

  void setHeaderVisible(boolean visible);

  boolean isFooterVisible();

  void setFooterVisible(boolean visible);

  IActionFetcher getHeaderActionFetcher();

  void setHeaderActionFetcher(IActionFetcher headerActionFetcher);

  IActionFetcher getFooterActionFetcher();

  void setFooterActionFetcher(IActionFetcher footerActionFetcher);
}
