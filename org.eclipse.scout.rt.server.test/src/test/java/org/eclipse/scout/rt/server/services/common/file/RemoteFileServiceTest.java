/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.file;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.junit.Test;

public class RemoteFileServiceTest {

  RemoteFileService service = new RemoteFileService("C://dev/");

  @Test
  public void testTryAddLocaleToFileName() {
    // locale with extension and hashtag
    Locale localeWithHashtagAndHyphen = new Locale.Builder().setLanguage("th").setRegion("TH").setExtension('x', "u-nu-thai").build(); //th_TH_#x-u-nu-thai
    checkAddLocaleToFileName("Example.txt", "Example_th_TH_#x-u-nu-thai.txt", localeWithHashtagAndHyphen, 0);
    checkAddLocaleToFileName("Example.txt", "Example_th_TH.txt", localeWithHashtagAndHyphen, 1);
    checkAddLocaleToFileName("Example.txt", "Example_th.txt", localeWithHashtagAndHyphen, 2);
    checkAddLocaleToFileName("Example.txt", "Example.txt", localeWithHashtagAndHyphen, 3);

    // more common language-country locale
    checkAddLocaleToFileName("Example.txt", "Example_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("Finde.txt", "Finde_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("Finde.txt", "Finde_de.txt", Locale.GERMANY, 1);
    checkAddLocaleToFileName("Finde.txt", "Finde.txt", Locale.GERMANY, 2);
    checkAddLocaleToFileName("FilenameWithReeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeealyLoooooooooooooooooooooooooooooongName.txt",
        "FilenameWithReeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeealyLoooooooooooooooooooooooooooooongName_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("FileNameWith_Special#Characters.andNumbers1234567890.txt", "FileNameWith_Special#Characters.andNumbers1234567890_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("FileNameWith_Special#Characters.andNumbers1234567890.txt", "FileNameWith_Special#Characters.andNumbers1234567890_de.txt", Locale.GERMANY, 1);
    checkAddLocaleToFileName("FileNameWith_Special#Characters.andNumbers1234567890.txt", "FileNameWith_Special#Characters.andNumbers1234567890.txt", Locale.GERMANY, 2);
    checkAddLocaleToFileName("FileNameWithLocaleItalian.txt", "FileNameWithLocaleItalian_it_IT.txt", Locale.ITALY, 0);

    // language only locale
    checkAddLocaleToFileName("Example.txt", "Example_de.txt", Locale.GERMAN, 0);
    checkAddLocaleToFileName("Example.txt", "Example.txt", Locale.GERMAN, 1);
  }

  @Test
  public void testTryAddLocaleToFileNameWithExistingLocalesInName() {
    // locale with extension and hashtag
    Locale localeWithHashtagAndHyphen = new Locale.Builder().setLanguage("th").setRegion("TH").setExtension('x', "u-nu-thai").build(); //th_TH_#x-u-nu-thai
    checkAddLocaleToFileName("Example_th_TH_#x-u-nu-thai.txt", "Example_th_TH_#x-u-nu-thai.txt", localeWithHashtagAndHyphen, 0);
    checkAddLocaleToFileName("Example_th_TH_#x-u-nu-thai.txt", "Example_th_TH.txt", localeWithHashtagAndHyphen, 1);
    checkAddLocaleToFileName("Example_th_TH_#x-u-nu-thai.txt", "Example_th.txt", localeWithHashtagAndHyphen, 2);
    checkAddLocaleToFileName("Example_th_TH_#x-u-nu-thai.txt", "Example_th_TH_#x-u-nu-thai.txt", localeWithHashtagAndHyphen, 3);

    checkAddLocaleToFileName("Example_de_DE.txt", "Example_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("Example_de_DE.txt", "Example_de.txt", Locale.GERMANY, 1);
    checkAddLocaleToFileName("Example_de_DE.txt", "Example_de_DE.txt", Locale.GERMANY, 2);
    checkAddLocaleToFileName("Examplede_de_DE.txt", "Examplede_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("Finde_de_DE.txt", "Finde_de_DE.txt", Locale.GERMANY, 0);
    checkAddLocaleToFileName("FileNameWithLocale_it_IT.txt", "FileNameWithLocale_it_IT.txt", Locale.ITALY, 0);

    // language only locale
    checkAddLocaleToFileName("Example_de.txt", "Example_de.txt", Locale.GERMAN, 0);
    checkAddLocaleToFileName("Example_de.txt", "Example_de.txt", Locale.GERMAN, 1);
  }

  @Test
  public void testMaxLocaleNameLength() {
    checkAddLocaleToFileName("test.txt", "test.txt", new Locale("locale-with-name-longer-than-64-characters_123456789abcdefghijklmnopqrstuvwxyz"), 0);
  }

  @Test
  public void testMissingLocale() {
    checkAddLocaleToFileName("Example_de.txt", "Example_de.txt", null, 0);
  }

  @Test
  public void testMissingFileSuffix() {
    checkAddLocaleToFileName("Example_de", "Example_de", null, 0);
  }

  protected void checkAddLocaleToFileName(String initialName, String expectedFileNameAfterTest, Locale locale, Integer fileExistsAtLevel) {
    AtomicInteger fileLocaleLevel = new AtomicInteger(0);
    String fileNameAfterExtend = service.tryAddLocaleToFileName(initialName, locale, "test/", file -> fileLocaleLevel.getAndIncrement() == fileExistsAtLevel);
    Assertions.assertEquals(fileNameAfterExtend, expectedFileNameAfterTest);
  }

  @Test(expected = SecurityException.class)
  public void testInvalidCharacterSlash() {
    service.tryAddLocaleToFileName("test.txt", new Locale("de-DE/"), "", file -> true);
  }

  @Test(expected = SecurityException.class)
  public void testInvalidCharacterColon() {
    service.tryAddLocaleToFileName("test.txt", new Locale("de-DE:"), "", file -> true);
  }

  @Test(expected = SecurityException.class)
  public void testInvalidCharactersPath() {
    service.tryAddLocaleToFileName("test.txt", new Locale("ls //C:temp/"), "", file -> true);
  }

  @Test(expected = SecurityException.class)
  public void testInvalidCharacterUTF8() {
    service.tryAddLocaleToFileName("test.txt", new Locale("de-DE퀀팀"), "", file -> true);
  }

  @Test(expected = SecurityException.class)
  public void testInvalidCharacterDot() {
    service.tryAddLocaleToFileName("test.txt", new Locale(".de-DE"), "", file -> true);
  }
}
