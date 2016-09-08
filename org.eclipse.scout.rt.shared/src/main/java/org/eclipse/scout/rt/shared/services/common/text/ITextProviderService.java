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
package org.eclipse.scout.rt.shared.services.common.text;

import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Interface for Scout Text Providers.
 */
public interface ITextProviderService extends IService {

  /**
   * Gets the text for the given key and language.<br>
   * if there are parameters in the translation, they are replaced with the given list.
   *
   * @param locale
   *          The locale of the text
   * @param key
   *          The nls text key
   * @param messageArguments
   *          The arguments (can be null) to replace in the returned text.<br>
   *          The translation of the text might contain variables like {0},{1},{2},...<br>
   *          Examples: <br>
   *          <ul>
   *          <li>getText("MissingFile1"); with MissingFile1="The File could not be found."</li>
   *          <li>getText("MissingFile2", fileName); with MissingFile2="The File {0} could not be found."</li>
   *          <li>getText("MissingFile3", fileName, dir); with MissingFile3="The File {0} in Folder {1} could not be
   *          found."</li>
   *          </ul>
   * @return
   */
  String getText(Locale locale, String key, String... messageArguments);

  /**
   * returns all key/text pairs defined for the given locale.
   *
   * @param locale
   *          The locale for which the map should be returned (not all locales might have all keys).
   * @return A <code>java.util.Map</code> containing the key/text pairs.
   */
  Map<String, String> getTextMap(Locale locale);
}
