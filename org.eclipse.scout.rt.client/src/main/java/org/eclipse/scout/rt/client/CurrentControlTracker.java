/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;

/**
 * This class tracks the current model element, {@link IForm} and {@link IOutline} of the current thread's calling
 * context.
 * <p>
 * This class is intended to be used by the model thread only.
 *
 * @since 5.1
 */
@ApplicationScoped
public class CurrentControlTracker {

  /**
   * The current model element of the current thread's calling context, and is typically set when entering the 'UI
   * facade'.
   */
  public static final ThreadLocal<Object> CURRENT_MODEL_ELEMENT = new ThreadLocal<>();

  /**
   * The current {@link IForm} of the current thread's calling context, and is typically set when entering the
   * 'UI facade'.
   */
  public static final ThreadLocal<IForm> CURRENT_FORM = new ThreadLocal<>();

  /**
   * The current {@link IOutline} of the current thread's calling context, and is typically set when entering the
   * 'UI facade'.
   */
  public static final ThreadLocal<IOutline> CURRENT_OUTLINE = new ThreadLocal<>();

  /**
   * Creates a Java Proxy for the given 'UI facade' to install the current model element, {@link IForm} and
   * {@link IOutline} in the calling context of an invoking thread.<br/>
   * When this method is invoked, the current {@link IForm} and {@link IOutline} is determined by the current thread's
   * calling context.
   *
   * @param facade
   *          The 'UI facade' to be proxied.
   * @param modelElement
   *          The model element the facade belongs to.
   * @return proxied facade.
   */
  public <FACADE> FACADE install(final FACADE facade, final Object modelElement) {
    return install(facade, modelElement, CURRENT_FORM.get(), CURRENT_OUTLINE.get());
  }

  /**
   * Creates a Java Proxy for the given 'UI facade' to install the current model element, {@link IForm} and
   * {@link IOutline} in the calling context of an invoking thread.
   *
   * @param facade
   *          The 'UI facade' to be proxied.
   * @param modelElement
   *          The model element the facade belongs to.
   * @param form
   *          The {@link IForm} to be set onto the calling context of an invoking thread.
   * @param outline
   *          The {@link IOutline} to be set onto the calling context of an invoking thread.
   * @return proxied facade.
   */
  @SuppressWarnings("unchecked")
  public <FACADE> FACADE install(final FACADE facade, final Object modelElement, final IForm form, final IOutline outline) {
    return (FACADE) Proxy.newProxyInstance(facade.getClass().getClassLoader(), ReflectionUtility.getInterfaces(facade.getClass()), new InvocationHandler() {

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return ClientRunContexts.copyCurrent().outline(outline).form(form).modelElement(modelElement).call(new Callable<Object>() {

          @Override
          public Object call() throws Exception {
            return method.invoke(facade, args);
          }
        }, BEANS.get(ThrowableTranslator.class));
      }
    });
  }
}
