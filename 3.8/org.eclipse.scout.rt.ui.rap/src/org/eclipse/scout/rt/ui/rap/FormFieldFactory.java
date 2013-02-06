/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.extension.FormFieldsExtensionPoint;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldExtension;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

public class FormFieldFactory implements IFormFieldFactory {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(FormFieldFactory.class);

  private LinkedHashMap<Class<?>, IFormFieldFactory> m_fields;

  /**
   * @param bundle
   *          the ui.swt bundle of the requesting application (e.g.
   *          com.bsiag.crm.ui.swt)
   */
  @SuppressWarnings("unchecked")
  public FormFieldFactory(Bundle requesterBundle) {
    TreeMap<CompositeObject, P_FormFieldExtension> sortedMap = new TreeMap<CompositeObject, P_FormFieldExtension>();
    for (IFormFieldExtension extension : FormFieldsExtensionPoint.getFormFieldExtensions()) {
      if (extension.isActive()) {
        Bundle loaderBundle;
        if (extension.getScope() == IFormFieldExtension.SCOPE_LOCAL) {
          loaderBundle = requesterBundle;
        }
        else {
          loaderBundle = Platform.getBundle(extension.getContibuterBundleId());
        }
        if (loaderBundle != null) {
          Class<?> modelClazz;
          Class<? extends IRwtScoutFormField> uiClazz = null;
          Class<? extends IFormFieldFactory> factoryClazz = null;
          try {
            modelClazz = loaderBundle.loadClass(extension.getModelClassName());
            if (!StringUtility.isNullOrEmpty(extension.getUiClassName())) {
              uiClazz = (Class<? extends IRwtScoutFormField>) loaderBundle.loadClass(extension.getUiClassName());
              if (!IRwtScoutFormField.class.isAssignableFrom(uiClazz)) {
                LOG.warn("extension '" + extension.getName() + "' contributed by '" + extension.getContibuterBundleId() + "' has an ui class not instanceof " + IRwtScoutFormField.class.getName() + ".");
                uiClazz = null;
              }
            }
            else if (!StringUtility.isNullOrEmpty(extension.getFactoryClassName())) {
              factoryClazz = (Class<? extends IFormFieldFactory>) loaderBundle.loadClass(extension.getFactoryClassName());
              if (!IFormFieldFactory.class.isAssignableFrom(factoryClazz)) {
                LOG.warn("extension '" + extension.getName() + "' contributed by '" + extension.getContibuterBundleId() + "' has a facotry class not instanceof " + IFormFieldFactory.class.getName() + ".");
                factoryClazz = null;
              }
            }
            IFormFieldFactory factory = null;
            if (uiClazz != null) {
              factory = new P_DirectLinkFormFieldFactory(uiClazz);
            }
            else if (factoryClazz != null) {
              try {
                factory = factoryClazz.newInstance();
              }
              catch (Exception e) {
                LOG.warn("could not create a factory instance of '" + factoryClazz.getName() + "' ", e);
              }
            }
            else {
              LOG.debug("extension '" + extension.getName() + "' contributed by '" + extension.getContibuterBundleId() + "' has neither an UiClass nor a factory defined! Skipping extension.");
              continue;
            }
            int distance = -distanceToIFormField(modelClazz, 0);
            CompositeObject key = new CompositeObject(distance, modelClazz.getName());
            if (sortedMap.containsKey(key)) {
              P_FormFieldExtension existingExt = sortedMap.get(key);
              // check scope
              if (existingExt.getFormFieldExtension().getScope() == extension.getScope()) {
                LOG.warn("The bundles '" + extension.getContibuterBundleId() + "' and '" + existingExt.getFormFieldExtension().getContibuterBundleId() + "' are both providing " + "an form field extension to '" + extension.getModelClassName() + "' with the same scope.");
              }
              else if (existingExt.getFormFieldExtension().getScope() < extension.getScope()) {
                // replace
                sortedMap.put(key, new P_FormFieldExtension(modelClazz, factory, extension));
              }
            }
            else {
              sortedMap.put(key, new P_FormFieldExtension(modelClazz, factory, extension));
            }
          }
          catch (ClassNotFoundException e) {
            LOG.debug("local extension '" + extension.getName() + "' contributed by '" + extension.getContibuterBundleId() + "' is not visible from bundle: '" + loaderBundle.getSymbolicName() + "'.");
          }
        }

      }
    }

    m_fields = new LinkedHashMap<Class<?>, IFormFieldFactory>();
    for (P_FormFieldExtension ext : sortedMap.values()) {
      m_fields.put(ext.getModelClazz(), ext.getFactory());
    }
  }

  private static int distanceToIFormField(Class<?> visitee, int dist) {
    if (visitee == IFormField.class) {
      return dist;
    }
    else {
      int locDist = 100000;
      Class<?> superclass = visitee.getSuperclass();
      if (superclass != null) {
        locDist = distanceToIFormField(superclass, (dist + 1));
      }
      Class[] interfaces = visitee.getInterfaces();
      if (interfaces != null) {
        for (Class<?> i : interfaces) {
          locDist = Math.min(locDist, distanceToIFormField(i, (dist + 1)));
        }
      }
      dist = locDist;
      return dist;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public IRwtScoutFormField<IFormField> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    IRwtScoutFormField<IFormField> uiField = (IRwtScoutFormField<IFormField>) createUiFormFieldByExtension(parent, model, uiEnvironment);
    return uiField;
  }

  private IRwtScoutFormField<?> createUiFormFieldByExtension(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    IFormFieldFactory factory = null;
    for (Entry<Class<?>, IFormFieldFactory> link : m_fields.entrySet()) {
      if (link.getKey().isAssignableFrom(model.getClass())) {
        // create instance
        factory = link.getValue();
        try {
          return factory.createUiFormField(parent, model, uiEnvironment);
        }
        catch (Throwable e) {
          LOG.error("could not create form field for: [model = '" + model.getClass().getName() + "'; ui = '" + factory.toString() + "'].", e);
        }
      }
    }
    if (factory != null) {
      try {
        return factory.createUiFormField(parent, model, uiEnvironment);
      }
      catch (Throwable t) {
        t.printStackTrace();
        return null;
      }
    }
    return null;
  }

  private class P_DirectLinkFormFieldFactory implements IFormFieldFactory {
    private final Class<? extends IRwtScoutFormField> m_uiClazz;

    public P_DirectLinkFormFieldFactory(Class<? extends IRwtScoutFormField> uiClazz) {
      m_uiClazz = uiClazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
      try {
        IRwtScoutFormField newInstance = m_uiClazz.newInstance();
        newInstance.createUiField(parent, model, uiEnvironment);
        return newInstance;
      }
      catch (Exception e) {
        LOG.warn(null, e);
        return null;
      }
    }

    @Override
    public String toString() {
      return "DirectLinkFactory to: " + m_uiClazz.getName();
    }
  }// end class P_DirectLinkFormFieldFactory

  private class P_FormFieldExtension {
    private final Class<?> m_modelClazz;
    private final IFormFieldFactory m_factory;
    private final IFormFieldExtension m_formFieldExtension;

    public P_FormFieldExtension(Class<?> modelClazz, IFormFieldFactory factory, IFormFieldExtension formFieldExtension) {
      m_modelClazz = modelClazz;
      m_factory = factory;
      m_formFieldExtension = formFieldExtension;
    }

    public Class<?> getModelClazz() {
      return m_modelClazz;
    }

    public IFormFieldFactory getFactory() {
      return m_factory;
    }

    public IFormFieldExtension getFormFieldExtension() {
      return m_formFieldExtension;
    }
  } // end class P_FormFieldExtension

}
