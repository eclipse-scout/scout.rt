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

import java.util.Locale;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.nls.DynamicNls;

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
public class ScoutTexts extends DynamicNls {
  public static final String RESOURCE_BUNDLE_NAME = "resources.texts.ScoutTexts";//$NON-NLS-1$
  public static final QualifiedName JOB_PROPERTY_NAME = new QualifiedName("org.eclipse.scout.commons", "DynamicNls");

  private static ScoutTexts instance = new ScoutTexts();

  public static DynamicNls getInstance() {
    DynamicNls jobInstance = null;
    try {
      jobInstance = (DynamicNls) Job.getJobManager().currentJob().getProperty(JOB_PROPERTY_NAME);
    }
    catch (Throwable t) {
      //performance optimization: null job is very rare
    }
    if (jobInstance != null) {
      return jobInstance;
    }
    return instance;
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return getInstance().getText(locale, key, messageArguments);
  }

  protected ScoutTexts() {
    registerResourceBundle(RESOURCE_BUNDLE_NAME, ScoutTexts.class);
  }
}
