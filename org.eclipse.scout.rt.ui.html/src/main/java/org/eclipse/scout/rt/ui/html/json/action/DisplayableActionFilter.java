/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.action;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.IAction;

public class DisplayableActionFilter<T extends IAction> implements Predicate<T> {

  @Override
  public boolean test(T element) {
    return element.isVisibleGranted();
  }

}
