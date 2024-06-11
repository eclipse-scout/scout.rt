/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;

public interface IBrowserCallback<TResponse extends IDoEntity, TResult> {

  default String browserCallbackHandlerObjectType() {
    // use @ObjectType annotation by default
    return BEANS.get(ApiExposeHelper.class).objectTypeOf(this);
  }

  Pair<TResult, ? extends Throwable> onCallbackDone(TResponse response);

  default Pair<TResult, ? extends Throwable> onCallbackFailed(Throwable exception, String message, String code) {
    return ImmutablePair.of(null, exception);
  }
}
