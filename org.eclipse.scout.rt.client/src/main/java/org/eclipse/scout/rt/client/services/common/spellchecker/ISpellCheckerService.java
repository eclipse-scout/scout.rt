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
package org.eclipse.scout.rt.client.services.common.spellchecker;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * SpellChecker Service looks expects a configuration.xml see
 * {@link ISpellCheckerService#initialize(String, IResourceLocator)} and {@link IResourceLocator} Sample
 * configuration.xml is: <xmp> <?xml
 * version="1.0" encoding="UTF-8"?> <spellchecker> <language id="any">
 * <dictionary>sc_dict_any_01.tlx</dictionary>
 * <dictionary>sc_dict_any_02.tlx</dictionary> <option
 * id="IGNORE_NON_ALPHA_WORD_OPT" >true</option> <option
 * id="REPORT_DOUBLED_WORD_OPT" >true</option> <option
 * id="SPLIT_HYPHENATED_WORDS_OPT">true</option> <option
 * id="REPORT_MIXED_CASE_OPT" >false</option> <!-- Setting to true can cause
 * performance problems --> </language> <language id="de_CH">
 * <dictionary>sc_dict_ge-ch_01.tlx</dictionary>
 * <dictionary>sc_dict_ge-ch_02.tlx</dictionary>
 * <dictionary>sc_dict_ge-de_03.clx</dictionary> <option
 * id="ALLOW_ACCENTED_CAPS_OPT">true</option> <option
 * id="SPLIT_CONTRACTED_WORDS_OPT">false</option> <option
 * id="STRIP_POSSESSIVES_OPT">true</option> <option
 * id="SPLIT_WORDS_OPT">true</option> </language> <language id="de_AT">
 * <dictionary>sc_dict_ge-at_01.tlx</dictionary>
 * <dictionary>sc_dict_ge-at_02.tlx</dictionary>
 * <dictionary>sc_dict_ge-de_03.clx</dictionary> <option
 * id="ALLOW_ACCENTED_CAPS_OPT">true</option> <option
 * id="SPLIT_CONTRACTED_WORDS_OPT" >false</option> <option
 * id="STRIP_POSSESSIVES_OPT" >true</option> <option id="SPLIT_WORDS_OPT"
 * >true</option> </language> </spellchecker> </xmp>
 */
public interface ISpellCheckerService extends IService {

  /**
   * Initializes the spell checker and resets all settings.
   * 
   * @param pathToUserDictionary
   *          can be null to use the default path or non null (for example MS
   *          Words user dictionary)
   */
  void initialize(String pathToUserDictionary) throws ProcessingException;

  /**
   * Initializes the spell checker and resets all settings.
   * 
   * @param pathToUserDictionary
   *          can be null to use the default path or non null (for example MS
   *          Words user dictionary)
   * @param resourceLocator
   *          used to get spellchecker files. Resources normally are part of a
   *          plugin or are placed on the server via remote file service.
   *          Passing null defaults to remote files with paths /spellchecker/*
   *          see {@link RemoteFileResourceLocator} and {@link BundleResourceLocator}
   */
  void initialize(String pathToUserDictionary, IResourceLocator resourceLocator) throws ProcessingException;

  /**
   * Re initializes the spell checkers language profile without losing
   * previously set parameters
   */
  void reinitialize() throws ProcessingException;

  void switchLanguage(String locale) throws ProcessingException;

  void saveSettings();

  /**
   * Adds the given word to the temporary dictionary. Check-as-you-type fields
   * are automatically updated. Returns whether the word was successfully added.
   */
  boolean addWordToTempDictionary(String word);

  /**
   * Adds the given word to the user dictionary. Check-as-you-type fields are
   * automatically updated. Returns whether the word was successfully added.
   */
  boolean addWordToUserDictionary(String word);

  /**
   * Returns a list of language strings. see {@link #getLanguage()}
   */
  String[] getAvailableLanguages();

  /**
   * see {@link #getLanguage()}
   */
  boolean containsLanguage(String lang);

  /**
   * Returns the default for monitoring text areas. This is used if
   * check-as-you-type for multi-line text fields is set to application default
   * and if the programmer did not explicitly set the option for the field in
   * the Configurator.
   */
  boolean getDefaultForMonitoringTextAreas();

  void setDefaultForMonitoringTextAreas(boolean b);

  /**
   * Returns the default for monitoring text fields. This is used if
   * check-as-you-type for single-line text fields is set to application default
   * and if the programmer did not explicitly set the option for the field in
   * the Configurator.
   */
  boolean getDefaultForMonitoringTextFields();

  void setDefaultForMonitoringTextFields(boolean b);

  /**
   * @return locale of checking language as ;@link Locale#toString()} Examples:
   *         de, de_CH, en, en_US
   */
  String getLanguage();

  /**
   * Returns a list of strings with correcting suggestions for the given word.
   */
  List<String> getSuggestions(String word, int maxNoSuggestions);

  void ignoreAll(String word);

  /**
   * Returns whether the spellchecker is initialized and enabled
   */
  boolean isInstalled();

  boolean isCorrectText(String text);

  boolean isCorrectWord(String word);

  /**
   * Returns whether the spell checker is enabled. Takes effect immediately, no
   * re-initialization needed.
   */
  boolean isEnabled();

  /**
   * Returns whether case is ignored.
   */
  boolean isIgnoreCase();

  /**
   * Returns whether domain names are ignored.
   */
  boolean isIgnoreDomainNames();

  /**
   * Returns whether words with numbers are ignored.
   */
  boolean isIgnoreWordsWithNumbers();

  /**
   * Returns whether the spell checker is initialized
   */
  boolean isInitialized();

  boolean isInUserDictionary(String word, boolean caseSensitive);

  /**
   * Returns whether multi-line text fields are monitored ("check as you type").
   * null = Application default true = Always check as you type, ignore
   * field-specific settings false = Never check as you type, ignore
   * field-specific settings
   */
  Boolean isMonitoringTextAreas();

  /**
   * Returns whether single-line text fields are monitored
   * ("check as you type"). null = Application default true = Always check as
   * you type, ignore field-specific settings false = Never check as you type,
   * ignore field-specific settings
   */
  Boolean isMonitoringTextFields();

  /**
   * Returns whether hyphenated words (e.g. bright-blue) are split and checked
   * separately.
   */
  boolean isSplittingHyphenatedWords();

  /**
   * Returns whether words are split and checked separately (e.g.
   * 'Zusammenführen' -> 'Zusammen' and 'führen').
   */
  boolean isSplittingWords();

  /**
   * Returns whether possessives of the form 's and s' are removed from words
   * before checking their spelling.
   */
  boolean isStrippingPossessives();

  /**
   * Resets all settings. Needs re-initialization to take effect.
   */
  void dispose();

  /**
   * Removes the given word from the user dictionary. Returns true if the word
   * was successfully removed.
   */
  boolean removeWordFromUserDictionary(String word);

  /**
   * Sets whether the spell checker is enabled. Takes effect immediately, no
   * re-initialization needed.
   */
  void setEnabled(boolean enabled);

  /**
   * Sets whether to ignore case. Takes effect immediately, no re-initialization
   * needed.
   */
  void setIgnoreCase(boolean ignoreCase);

  /**
   * Sets whether to ignore domain names. Takes effect immediately, no
   * re-initialization needed.
   */
  void setIgnoreDomainNames(boolean ignoreDomainNames);

  /**
   * Sets whether to ignore words with numbers. Takes effect immediately, no
   * re-initialization needed.
   */
  void setIgnoreWordsWithNumbers(boolean ignoreWordsWithNumbers);

  /**
   * Sets the language. Needs re-initialization to take effect.
   */
  void setLanguage(String language);

  void setMonitorTextAreas(Boolean monitorTextAreas);

  void setMonitorTextFields(Boolean monitorTextFields);

  /**
   * Sets the precision of the spelling session. The parameter can range from 1
   * (shallow but fast) to 100 (deep but slow). Takes effect immediately, no
   * re-initialization needed.
   */
  void setPrecision(int precision);

  /**
   * Sets the user dictionary. Needs re-initialization to take effect.
   */
  void setUserDictionary(String userDictionary);

  /**
   * Must be called in scout model thread.
   */
  void showOptionsDialog();

  /**
   * Must be called in scout model thread.
   */
  void showCompleteDialog();

  /**
   * Must be called in scout model thread.
   */
  boolean showCompleteForSelectionDialog();

  ISpellingMonitor[] getAllSpellingMonitors();

  IUserDictionary getUserDictionary();

  /**
   * validate the whole text of all monitored text components.
   */
  void validateAllSpellingMonitors();

  /**
   * Rechecks the given word in all monitored text components.
   */
  void validateAllSpellingMonitors(String word);

  /**
   * Normally this method is not invoked by clients This method is thread-safe.
   */
  void addSpellingMonitor(ISpellingMonitor m);

  /**
   * Normally this method is not invoked by clients This method is thread-safe.
   */
  void removeSpellingMonitor(ISpellingMonitor m);
}
