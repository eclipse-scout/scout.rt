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

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;

/**
 * This class provides functionality to proxy an object to apply the given {@link ModelContext} to the calling
 * {@link ClientRunContext} of the thread invoking a proxied method.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ModelContextProxy {

  /**
   * Creates a Java Proxy for the given object with the given {@link ModelContext} applied when invoking the object's
   * methods.
   *
   * @param object
   *          The object to be proxied.
   * @param modelContext
   *          The {@link ModelContext} to be applied when invoking the object's methods.
   * @return proxied object.
   */
  @SuppressWarnings("unchecked")
  public <OBJECT> OBJECT newProxy(final OBJECT object, final ModelContext modelContext) {
    return (OBJECT) Proxy.newProxyInstance(object.getClass().getClassLoader(), ReflectionUtility.getInterfaces(object.getClass()), new InvocationHandler() {

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return ClientRunContexts.copyCurrent().withDesktop(modelContext.getDesktop()).withOutline(modelContext.getOutline()).withForm(modelContext.getForm()).call(new Callable<Object>() {

          @Override
          public Object call() throws Exception {
            return method.invoke(object, args);
          }
        }, BEANS.get(ThrowableTranslator.class));
      }
    });
  }

  public static class ModelContext {
    private IDesktop m_desktop;
    private IOutline m_outline;
    private IForm m_form;

    private ModelContext() {
    }

    /**
     * @return {@link ModelContext} initialized with the current {@link IDesktop}, {@link IOutline} and {@link IForm}.
     */
    public static ModelContext copyCurrent() {
      return new ModelContext().withDesktop(IDesktop.CURRENT.get()).withOutline(IOutline.CURRENT.get()).withForm(IForm.CURRENT.get());
    }

    public IDesktop getDesktop() {
      return m_desktop;
    }

    /**
     * Sets the {@link IDesktop} to be set onto the {@link ClientRunContext} when the proxy is invoked.
     */
    public ModelContext withDesktop(IDesktop desktop) {
      m_desktop = desktop;
      return this;
    }

    public IOutline getOutline() {
      return m_outline;
    }

    /**
     * Sets the {@link IOutline} to be set onto the {@link ClientRunContext} when the proxy is invoked.
     */
    public ModelContext withOutline(IOutline outline) {
      m_outline = outline;
      return this;
    }

    public IForm getForm() {
      return m_form;
    }

    /**
     * Sets the {@link IForm} to be set onto the {@link ClientRunContext} when the proxy is invoked.
     */
    public ModelContext withForm(IForm form) {
      m_form = form;
      return this;
    }
  }
}
