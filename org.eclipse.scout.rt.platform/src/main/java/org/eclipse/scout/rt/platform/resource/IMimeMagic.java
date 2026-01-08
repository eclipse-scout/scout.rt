/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.MalwareScanner;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Typically used in combination with {@link MalwareScanner}
 * <p>
 * see {@link MimeTypes#verifyMagic(BinaryResource)} and <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">List of file signatures</a>
 *
 * @since 10.x
 */
public interface IMimeMagic {
  IMimeMagic AVIF = createMagic(4, "6674797061766966");
  IMimeMagic DOC_XLS_PPT = createMagic("d0cf11e0a1b11ae1");
  IMimeMagic DOCX_XLSX_PPTX = createMagic("d0cf11e0a1b11ae1", "504b0304", "504b0506", "504b0708"); // union of DOC_XLS_PPT (used for protected office files) and ZIP (used for non-protected office files)
  IMimeMagic BMP = createMagic("424d");
  IMimeMagic EXE_DLL_SYS = createMagic("4d5a", "5a4d");
  IMimeMagic GIF = createMagic("474946383761", "474946383961");
  IMimeMagic GZ = createMagic("1f8b");
  IMimeMagic XZ = createMagic("FD377A585A00");
  IMimeMagic ICO = createMagic("00000100");
  IMimeMagic JPEG_JPG = createMagic("ffd8ff");
  IMimeMagic MKV = createMagic("1a45dfa3");
  IMimeMagic MP3 = createMagic("494433", "fff2", "fff3", "fffb");
  IMimeMagic MP4 = createMagic(4, "6674797069736f6d", "667479706D703432");
  IMimeMagic MSG = createMagic("2320637265617465", "6e616d6573706163", "d0cf11e0a1b11ae1");
  IMimeMagic PDF = createMagic("25504446");
  IMimeMagic PNG = createMagic("89504e470d0a1a0a");
  IMimeMagic TIF_TIFF = createMagic("49492a00", "4d4d002a");
  IMimeMagic WAV = createMagic("52494646XXXXXXXX57415645"); // The first 4 bytes have to be 'RIFF', then 4 bytes are skipped, then the 4 wav magic bytes are expected.
  IMimeMagic WOFF = createMagic("774f4646", "774f4632");
  IMimeMagic ZIP = createMagic("504b0304", "504b0506", "504b0708");
  IMimeMagic HEIC = createMagic("6674797068656963", "667479706d");
  IMimeMagic RAR = createMagic("526172211A0700", "526172211A070100");
  IMimeMagic CLASS = createMagic("CAFEBABE");
  IMimeMagic OGG = createMagic("4F676753");
  IMimeMagic EML = createMagic("52656365697665643A");
  IMimeMagic FLV = createMagic("464C56");
  IMimeMagic MPG = createMagic("47", "000001BA", "000001B3");
  IMimeMagic FLAC = createMagic("664C6143");
  IMimeMagic TAR = createMagic(257, "7573746172003030", "7573746172202000");
  IMimeMagic SEVEN_ZIP = createMagic("377ABCAF271C");

  /**
   * @return The number of bytes of the longest magic number (including offset if present).
   */
  int length();

  /**
   * Checks if the given bytes match this MIME magic.
   *
   * @param content
   *     The bytes to check or {@code null}.
   * @return {@code true} if the given bytes contain this MIME magic bytes.
   */
  boolean matches(byte[] content);

  /**
   * Validate file content or {@link BinaryResource}. Check headers and content in order to find out if the file is
   * valid or corrupt or malware
   *
   * @return true if the content of this resource complies with the mime type
   */
  default boolean matches(BinaryResource res) {
    return res != null && matches(res.getContent());
  }

  /**
   * @see MagicBytePattern for desciption of the hexMagics parameter.
   */
  static IMimeMagic createMagic(String... hexMagics) {
    return createMagic(0, hexMagics);
  }

  /**
   * @see MagicBytePattern for desciption of the hexMagics parameter.
   */
  static IMimeMagic createMagic(int pos, String... hexMagics) {
    return createMagic(Arrays.stream(hexMagics)
        .map(hexMagic -> new MagicBytePattern(pos, hexMagic))
        .toArray(MagicBytePattern[]::new));
  }

  /**
   * @see MagicBytePattern for desciption of the hexMagics parameter.
   */
  static IMimeMagic createMagic(MagicBytePattern... patterns) {
    Assertions.assertTrue(patterns.length > 0, "At least one pattern must be provided");

    int length = Arrays.stream(patterns)
        .mapToInt(bytePattern -> bytePattern.getPos() + bytePattern.getLength())
        .max()
        .orElseThrow(() -> new ProcessingException("Unreachable, as provided patterns must not be empty")); // longest magic marker

    return new IMimeMagic() {
      @Override
      public int length() {
        return length;
      }

      @Override
      public boolean matches(byte[] content) {
        if (content == null || content.length == 0) {
          return false;
        }
        for (MagicBytePattern pattern : patterns) {
          if (content.length < pattern.getPos() + pattern.getLength()) {
            continue;
          }
          if (pattern.matches(content)) {
            return true;
          }
        }
        return false;
      }
    };
  }
}

