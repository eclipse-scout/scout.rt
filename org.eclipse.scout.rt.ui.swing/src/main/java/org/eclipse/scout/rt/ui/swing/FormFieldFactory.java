/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JComponent;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldExtension;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

/**
 * Generic {@link IFormFieldFactory} for creating {@link IFormField}s by using the extension point
 * org.eclipse.scout.rt.ui.swing.formfields. <br>
 * With this extension point it is possible to define how a UI object is created for a specific model class: <br>
 * Either map a specific UI class to the model class and use the default factory to create it or specify a specific
 * factory.
 */
public class FormFieldFactory implements IFormFieldFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormFieldFactory.class);

  private Map<Class<?>, IFormFieldFactory> m_fields;

  public FormFieldFactory() {
    TreeMap<CompositeObject, P_FormFieldExtension> sortedMap = new TreeMap<CompositeObject, P_FormFieldExtension>();
    for (IFormFieldExtension extension : OBJ.NEW(FormFieldExtensions.class).getFormFieldExtensions()) {
      if (extension.isActive()) {
        final Class<?> modelClazz = extension.getModelClass();
        final Class<? extends ISwingScoutFormField> uiClazz = extension.getUiClass();
        final Class<? extends IFormFieldFactory> factoryClazz = extension.getFactoryClass();
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
          LOG.debug("extension '" + extension.getName() + "' has neither an UiClass nor a factory defined! Skipping extension.");
          break;
        }

        int distance = -distanceToIFormField(modelClazz, 0);
        CompositeObject key = new CompositeObject(distance, modelClazz.getName());
        if (sortedMap.containsKey(key)) {
          P_FormFieldExtension existingExt = sortedMap.get(key);
          // check scope
          if (existingExt.getFormFieldExtension().getScope() == extension.getScope()) {
            LOG.warn("Duplicate form field extension for '" + extension.getModelClass().getName() + "' with the same scope.");
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
  public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField model, ISwingEnvironment environment) {
    IFormFieldFactory factory = null;
    for (Entry<Class<?>, IFormFieldFactory> link : m_fields.entrySet()) {
      if (link.getKey().isAssignableFrom(model.getClass())) {
        // create instance
        factory = link.getValue();
        try {
          return factory.createFormField(parent, model, environment);
        }
        catch (Throwable e) {
          LOG.error("could not create form field for: [model = '" + model.getClass().getName() + "'; ui = '" + factory.toString() + "'].", e);
        }
      }
    }
    if (factory != null) {
      try {
        return factory.createFormField(parent, model, environment);
      }
      catch (Throwable t) {
        t.printStackTrace();
        return null;
      }
    }
    return null;
  }

  private class P_DirectLinkFormFieldFactory implements IFormFieldFactory {
    private final Class<? extends ISwingScoutFormField> m_uiClazz;

    public P_DirectLinkFormFieldFactory(Class<? extends ISwingScoutFormField> uiClazz) {
      m_uiClazz = uiClazz;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField field, ISwingEnvironment environment) {
      try {
        ISwingScoutFormField newInstance = m_uiClazz.newInstance();
        newInstance.createField(field, environment);
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
