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
package org.eclipse.scout.rt.ui.html.script;

import java.io.IOException;

import org.eclipse.scout.service.IService;

/**
 * Service interface for YUI or LESS used to compile and minify javscript and css.
 * <p>
 * Default implementation is in org.eclipse.scout.rt.ui.html.thirdparty
 */
public interface IScriptProcessorService extends IService {

  String compileCss(String content) throws IOException;

  String compileJs(String content) throws IOException;

  String minifyCss(String content) throws IOException;

  String minifyJs(String content) throws IOException;

}
