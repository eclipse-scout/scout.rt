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
package org.eclipse.scout.rt.ui.html.thirdparty;

import java.io.IOException;

import org.eclipse.scout.rt.ui.html.script.IScriptProcessorService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

public class ThirdPartyScriptProcessorService extends AbstractService implements IScriptProcessorService {
  private ThirdPartyScriptProcessorImpl m_impl;

  @Override
  public void initializeService(ServiceRegistration registration) {
    m_impl = new ThirdPartyScriptProcessorImpl();
  }

  @Override
  public String compileCss(String content) throws IOException {
    return m_impl.compileCss(content);
  }

  @Override
  public String compileJs(String content) throws IOException {
    return m_impl.compileJs(content);
  }

  @Override
  public String minifyCss(String content) throws IOException {
    return m_impl.minifyCss(content);
  }

  @Override
  public String minifyJs(String content) throws IOException {
    return m_impl.minifyJs(content);
  }

}
