/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.ObjectType;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;

/**
 * Handler to process and optionally convert the response from a {@link UiCallback} from the browser.
 * <p>
 * It provides hooks to process success and exception responses. Furthermore, it declares the ObjectType of the UI
 * (browser) side callback handler. This ObjectType can be specified by overwriting the corresponding method or by
 * adding an {@link ObjectType} annotation to the implementation.
 *
 * @param <TResponse>
 *     The response type as it is returned from the UI handler. Must be an {@link IDoEntity}.
 * @param <TResult>
 *     The converted result type as it will be returned by the callback.
 */
@Bean
public interface IUiCallbackHandler<TResponse extends IDoEntity, TResult> {

  /**
   * @return The ObjectType of the UI (browser) side callback handler class. By default the {@link ObjectType}
   *         annotation of this class is used. Then there is no need to overwrite this method.
   */
  default String uiCallbackHandlerObjectType() {
    // use @ObjectType annotation by default
    return BEANS.get(ApiExposeHelper.class).objectTypeOf(this);
  }

  /**
   * Executed in case the UI callback was successful.
   *
   * @param response
   *          The {@link IDoEntity} returned from the UI. Might be {@code null} in case the callback has no result.
   * @return The result of the callback. If the {@link Pair#getRight()} returns a {@link Throwable}, the callback is
   *         considered failed. If the {@link Throwable} is {@code null}, {@link Pair#getLeft()} is used as result.
   */
  Pair<TResult, ? extends Throwable> onCallbackDone(TResponse response);

  /**
   * Executed in case the UI callback was not successful.
   *
   * @param exception
   *          A prepared {@link ProcessingException} holding the message and code returned in the error from the UI
   *          (UiCallbackErrorDo).
   * @param message
   *          The message of the error as returned in the UiCallbackErrorDo returned from the UI.
   * @param code
   *          The optional code of the error as returned in the UiCallbackErrorDo returned from the UI.
   * @return The result of the callback. If the {@link Pair#getRight()} returns a {@link Throwable}, the callback is
   *         considered failed. If the {@link Throwable} is {@code null}, {@link Pair#getLeft()} is used as result.
   */
  default Pair<TResult, ? extends Throwable> onCallbackFailed(ProcessingException exception, String message, String code) {
    return ImmutablePair.of(null, exception);
  }
}
