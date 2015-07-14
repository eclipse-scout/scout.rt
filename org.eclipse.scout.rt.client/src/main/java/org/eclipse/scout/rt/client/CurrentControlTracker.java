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
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;

/**
 * This class tracks the current {@link IForm} and {@link IOutline} of the current thread's calling context.
 *
 * @since 5.1
 */
@ApplicationScoped
public class CurrentControlTracker {

  /**
   * The current {@link IForm} of the current thread's calling context, and is typically set when entering the
   * 'UI facade' or creating model elements.
   */
  public static final ThreadLocal<IForm> CURRENT_FORM = new ThreadLocal<>();

  /**
   * The current {@link IOutline} of the current thread's calling context, and is typically set when entering the
   * 'UI facade' or creating model elements.
   */
  public static final ThreadLocal<IOutline> CURRENT_OUTLINE = new ThreadLocal<>();

  /**
   * The {@link IDesktop} associated with this thread's calling context, and is typically set when entering the 'UI
   * facade' or creating model elements.
   */
  public static final ThreadLocal<IDesktop> CURRENT_DESKTOP = new ThreadLocal<>();

  /**
   * Creates a Java Proxy for the given 'object' with the given context information applied when invoking the object's
   * methods.
   *
   * @param object
   *          The object to be proxied.
   * @param contextInfo
   *          The context information to be applied when invoking the object's methods.
   * @return proxied object.
   */
  @SuppressWarnings("unchecked")
  public <OBJECT> OBJECT newProxy(final OBJECT object, final ContextInfo contextInfo) {
    return (OBJECT) Proxy.newProxyInstance(object.getClass().getClassLoader(), ReflectionUtility.getInterfaces(object.getClass()), new InvocationHandler() {

      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return ClientRunContexts.copyCurrent().withDesktop(contextInfo.getDesktop()).withOutline(contextInfo.getOutline()).withForm(contextInfo.getForm()).call(new Callable<Object>() {

          @Override
          public Object call() throws Exception {
            return method.invoke(object, args);
          }
        }, BEANS.get(ThrowableTranslator.class));
      }
    });
  }

  /**
   * Information about the invoking context.
   */
  public static class ContextInfo {
    private IDesktop m_desktop;
    private IOutline m_outline;
    private IForm m_form;

    private ContextInfo() {
    }

    /**
     * @return {@link ContextInfo} initialized with the current {@link IDesktop}, {@link IOutline} and {@link IForm}.
     *         <p>
     *         If not set explicitly, those values are set onto the {@link ClientRunContext} when entering the 'UI
     *         facade'.
     */
    public static ContextInfo copyCurrent() {
      return new ContextInfo().withDesktop(CURRENT_DESKTOP.get()).withOutline(CURRENT_OUTLINE.get()).withForm(CURRENT_FORM.get());
    }

    public IDesktop getDesktop() {
      return m_desktop;
    }

    /**
     * Sets the {@link IDesktop} to be set onto the {@link ClientRunContext} when entering the 'UI facade'.
     */
    public ContextInfo withDesktop(IDesktop desktop) {
      m_desktop = desktop;
      return this;
    }

    public IOutline getOutline() {
      return m_outline;
    }

    /**
     * Sets the {@link IOutline} to be set onto the {@link ClientRunContext} when entering the 'UI facade'.
     */
    public ContextInfo withOutline(IOutline outline) {
      m_outline = outline;
      return this;
    }

    public IForm getForm() {
      return m_form;
    }

    /**
     * Sets the {@link IForm} to be set onto the {@link ClientRunContext} when entering the 'UI facade'.
     */
    public ContextInfo withForm(IForm form) {
      m_form = form;
      return this;
    }
  }
}
