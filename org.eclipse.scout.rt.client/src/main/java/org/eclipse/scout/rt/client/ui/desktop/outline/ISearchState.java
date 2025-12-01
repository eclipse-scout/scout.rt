/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.IWidget;

public interface ISearchState extends IWidget {

  String PROP_RESULT_COUNT = "resultCount";
  String PROP_LIMITED = "limited";
  String PROP_PENDING = "pending";

  int getResultCount();

  void setResultCount(int resultCount);

  boolean isLimited();

  void setLimited(boolean limited);

  boolean isPending();

  void setPending(boolean pending);

  ISearchStateUiFacade getUIFacade();
}
