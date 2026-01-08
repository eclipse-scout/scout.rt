/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import org.eclipse.scout.rt.platform.security.MalwareScanner;
import org.eclipse.scout.rt.platform.util.HexUtility;

/**
 * Typically used in combination with {@link MalwareScanner}
 * <p>
 * see {@link MimeTypes#verifyMagic(BinaryResource)} and <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">List of file signatures</a>
 *
 * @since 10.x
 */
public interface IMimeMagic {
  IMimeMagic AVIF = createMagic(4, "6674797061766966");
  IMimeMagic DOC_XLS_PPT = createMagic(0, "d0cf11e0a1b11ae1");
  IMimeMagic DOCX_XLSX_PPTX = createMagic(0, "d0cf11e0a1b11ae1", "504b0304", "504b0506", "504b0708"); // union of DOC_XLS_PPT (used for protected office files) and ZIP (used for non-protected office files)
  IMimeMagic BMP = createMagic(0, "424d");
  IMimeMagic EXE_DLL_SYS = createMagic(0, "4d5a", "5a4d");
  IMimeMagic GIF = createMagic(0, "474946383761", "474946383961");
  IMimeMagic GZ = createMagic(0, "1f8b");
  IMimeMagic ICO = createMagic(0, "00000100");
  IMimeMagic JPEG_JPG = createMagic(0, "ffd8ff");
  IMimeMagic MKV = createMagic(0, "1a45dfa3");
  IMimeMagic MP3 = createMagic(0, "494433", "fff2", "fff3", "fffb");
  IMimeMagic MP4 = createMagic(4, "6674797069736f6d", "667479706D703432");
  IMimeMagic MSG = createMagic(0, "2320637265617465", "6e616d6573706163", "d0cf11e0a1b11ae1");
  IMimeMagic PDF = createMagic(0, "25504446");
  IMimeMagic PNG = createMagic(0, "89504e470d0a1a0a");
  IMimeMagic TIF_TIFF = createMagic(0, "49492a00", "4d4d002a");
  IMimeMagic WAV = createMagic(8, "57415645"); // The first 4 bytes have to be 'RIFF'. But `createMagic` doesn't support that yet.
  IMimeMagic WOFF = createMagic(0, "774f4646", "774f4632");
  IMimeMagic ZIP = createMagic(0, "504b0304", "504b0506", "504b0708");

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

  static IMimeMagic createMagic(int pos, String... hexMagics) {
    byte[][] magics = new byte[hexMagics.length][];
    int maxLen = 0; // longest magic marker
    for (int i = 0; i < hexMagics.length; i++) {
      magics[i] = HexUtility.decode(hexMagics[i]);
      maxLen = Math.max(maxLen, magics[i].length);
    }
    int magicsMaxLen = maxLen;

    return new IMimeMagic() {
      @Override
      public int length() {
        return pos + magicsMaxLen;
      }

      @Override
      public boolean matches(byte[] content) {
        if (content == null || content.length == 0) {
          return false;
        }
        for (byte[] magic : magics) {
          if (content.length < pos + magic.length) {
            continue;
          }
          if (matchesMagic(content, magic)) {
            return true;
          }
        }
        return false;
      }

      private boolean matchesMagic(byte[] content, byte[] magic) {
        for (int i = 0; i < magic.length; i++) {
          if (content[pos + i] != magic[i]) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
