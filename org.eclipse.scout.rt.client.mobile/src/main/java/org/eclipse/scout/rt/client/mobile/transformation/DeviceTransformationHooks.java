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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.util.BeanUtility;

/**
 * <b>Warn: The hook concept is experimental and may be removed without notice.</b>
 * <p>
 * The hooks are ordered by following rules:<br>
 * <ul>
 * <li>If multiple hooks are registered for the same class every hook gets called</li>
 * <li>If a hook is registered for a concrete field (f.e. PersonForm.PersonField) and its template field (f.e.
 * AbstractPersonField then the concrete field (PersonField) has higher priority. If the concrete field gets transformed
 * the hooks for the template field are NOT called.</li>
 * </ul>
 * 
 * @since 3.10.0 M3
 */
public class DeviceTransformationHooks {
  private static List<TransformationHookRef> s_transformationHooks = new ArrayList<TransformationHookRef>();
  private static final Object REGISTRY_LOCK = new Object();

  public static void addFormTransformationHook(Class<? extends IForm> formClass, IDeviceTransformationHook formTransformationHook) {
    addTransformationHook(formClass, formTransformationHook);
  }

  public static void removeFormTransformationHook(Class<? extends IForm> formClass, IDeviceTransformationHook formTransformationHook) {
    removeTransformationHook(formClass, formTransformationHook);
  }

  /**
   * @return null if no hooks are registered
   */
  public static List<IDeviceTransformationHook> getFormTransformationHooks(Class<? extends IForm> formClass) {
    return getTransformationHooks(formClass);
  }

  public static void addFormFieldTransformationHook(Class<? extends IFormField> fieldClass, IDeviceTransformationHook transformationHook) {
    addTransformationHook(fieldClass, transformationHook);
  }

  public static void removeFormFieldTransformationHook(Class<? extends IFormField> fieldClass, IDeviceTransformationHook formFieldTransformationHook) {
    removeTransformationHook(fieldClass, formFieldTransformationHook);
  }

  /**
   * @return null if no hooks are registered
   */
  public static List<IDeviceTransformationHook> getFormFieldTransformationHooks(Class<? extends IFormField> fieldClass) {
    return getTransformationHooks(fieldClass);
  }

  private static void addTransformationHook(Class<?> refClass, IDeviceTransformationHook transformationHook) {
    synchronized (REGISTRY_LOCK) {
      int index = 0;
      int insertPos = 0;
      boolean added = false;
      for (TransformationHookRef ref : s_transformationHooks) {
        if (ref.getRefClass().equals(refClass)) {
          ref.getHooks().add(transformationHook);
          added = true;
          break;
        }
        if (refClass.isAssignableFrom(ref.getRefClass())) {
          int typeDistance = BeanUtility.computeTypeDistance(refClass, ref.getRefClass());
          if (typeDistance > 0) {
            insertPos = index + 1;
          }
        }
        index++;
      }
      if (!added) {
        s_transformationHooks.add(insertPos, new TransformationHookRef(refClass, transformationHook));
      }
    }
  }

  private static void removeTransformationHook(Class<?> refClass, IDeviceTransformationHook formFieldTransformationHook) {
    synchronized (REGISTRY_LOCK) {
      List<IDeviceTransformationHook> hooks = getTransformationHooks(refClass);
      if (hooks != null) {
        hooks.remove(formFieldTransformationHook);
      }
    }
  }

  /**
   * @return null if no hooks are registered
   */
  private static List<IDeviceTransformationHook> getTransformationHooks(Class<?> refClass) {
    synchronized (REGISTRY_LOCK) {
      for (TransformationHookRef ref : s_transformationHooks) {
        if (ref.getRefClass().isAssignableFrom(refClass)) {
          return ref.getHooks();
        }
      }
    }
    return null;
  }

  private static class TransformationHookRef {
    private Class<?> m_refClass;
    private List<IDeviceTransformationHook> m_hooks;

    public TransformationHookRef(Class<?> refClass, IDeviceTransformationHook hook) {
      m_refClass = refClass;
      m_hooks = new LinkedList<IDeviceTransformationHook>();
      m_hooks.add(hook);
    }

    public Class<?> getRefClass() {
      return m_refClass;
    }

    public List<IDeviceTransformationHook> getHooks() {
      return m_hooks;
    }
  }

}
