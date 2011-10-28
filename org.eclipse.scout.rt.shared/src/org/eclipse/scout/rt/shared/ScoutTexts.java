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
package org.eclipse.scout.rt.shared;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * This is the base class for all scout-based translation classes.<br>
 * Do not change any member nor field of this class anytime otherwise the nls
 * support is not anymore garanteed. This class is auto generated and is
 * maintained by the plugins translations.nls file in the root directory of the
 * plugin.
 * <p>
 * The method getInstance() queries {@link Job#getProperty(org.eclipse.core.runtime.QualifiedName)} with
 * {@value #JOB_PROPERTY_NAME} to find the scope specific texts implementation. If this one is null, then the instance
 * itself is used as global default. That way applications can override scout texts in their application session only,
 * without interfering with other scout applications in the same osgi/eclipse runtime.
 * <p>
 * see IClientSession#getNlsTexts()<br/>
 * see IServerSession#getNlsTexts()<br/>
 * see ClientJob<br/>
 * see ServerJob<br/>
 * 
 * @see translations.nls
 */
public class ScoutTexts {

  public static final QualifiedName JOB_PROPERTY_NAME = new QualifiedName("org.eclipse.scout.commons", "DynamicNls");
  private static final ScoutTexts preSessionInstance = new ScoutTexts();

  protected ITextProviderService[] m_textProviderCache = null;

  public ScoutTexts() {
    BundleContext c = Activator.getDefault().getBundle().getBundleContext();
    try {
      c.addServiceListener(new ServiceListener() {
        @Override
        public void serviceChanged(ServiceEvent event) {
          if (event.getType() == ServiceEvent.REGISTERED || event.getType() == ServiceEvent.UNREGISTERING) {
            invalidateTextProviderCache();
          }
        }
      }, "(objectclass=" + ITextProviderService.class.getName() + ")");
    }
    catch (InvalidSyntaxException e) {
      // cannot happen, filter has been tested.
    }
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return getInstance().getText(locale, key, messageArguments);
  }

  public static ScoutTexts getInstance() {
    ScoutTexts jobInstance = null;
    try {
      jobInstance = (ScoutTexts) Job.getJobManager().currentJob().getProperty(JOB_PROPERTY_NAME);
    }
    catch (Throwable t) {
      //performance optimization: null job is very rare
    }

    //If session has not been initialized yet the preSessionInstance is used.
    if (jobInstance == null) {
      jobInstance = preSessionInstance;
    }

    return jobInstance;
  }

  public void invalidateTextProviderCache() {
    m_textProviderCache = null;
  }

  public final String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  public final String getText(Locale locale, String key, String... messageArguments) {
    return getTextInternal(locale, key, messageArguments);
  }

  public Map<String, String> getTextMap(Locale locale) {
    HashMap<String, String> map = new HashMap<String, String>();
    ITextProviderService[] providers = getTextProviders();
    for (int i = providers.length - 1; i >= 0; i--) {
      map.putAll(providers[i].getTextMap(locale));
    }
    return map;
  }

  protected synchronized ITextProviderService[] getTextProviders() {
    if (m_textProviderCache == null) {
      m_textProviderCache = SERVICES.getServices(ITextProviderService.class);
    }
    return m_textProviderCache;
  }

  protected String getTextInternal(Locale locale, String key, String... messageArguments) {
    for (ITextProviderService provider : getTextProviders()) {
      String result = provider.getText(locale, key, messageArguments);
      if (result != null) {
        return result;
      }
    }
    return "{undefined text " + key + "}";
  }
}
