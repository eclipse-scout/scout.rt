/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * Enumeration for a few well-known extensions and their mime types.
 * <p>
 * For file extension lookup based on mime type, the order of the mime types is relevant (first match returns).
 * <p>
 * This enum can be extended in {@link MimeTypes}
 */
public enum MimeType implements IMimeType {
  AA("audio/audible", "aa"),
  AAC("audio/aac", "aac"),
  AMV("video/x-amv", "amv"),
  APPCACHE("text/cache-manifest", "appcache"),
  APPLICATION_OCTET_STREAM("application/octet-stream", "bin"),
  AVI("video/avi", "avi"),
  BMP("image/bmp", "bmp", IMimeMagic.BMP),
  CSS("text/css", "css"),
  CSV("text/csv", "csv"),
  DOC("application/msword", "doc", IMimeMagic.DOC_XLS_PPT),
  DOCM("application/vnd.ms-word.document.macroEnabled.12", "docm", IMimeMagic.DOCX_XLSX_PPTX),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", IMimeMagic.DOCX_XLSX_PPTX),
  DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx", IMimeMagic.DOCX_XLSX_PPTX),
  EML("message/rfc822", "eml"),
  EXE("application/vnd.microsoft.portable-executable", "exe", IMimeMagic.EXE_DLL_SYS),
  DLL("application/vnd.microsoft.portable-executable", "dll", IMimeMagic.EXE_DLL_SYS),
  SYS("application/vnd.microsoft.portable-executable", "sys", IMimeMagic.EXE_DLL_SYS),
  FLV("video/x-flv", "flv"),
  GIF("image/gif", "gif", IMimeMagic.GIF),
  GZ("application/gzip", "gz", IMimeMagic.GZ),
  HTML("text/html", "html"),
  HTM("text/html", "htm"),
  ICO("image/x-icon", "ico", IMimeMagic.ICO),
  ICS("text/calendar", "ics"),
  IFB("text/calendar", "ifb"),
  JAR("application/java-archive", "jar", IMimeMagic.ZIP),
  JPG("image/jpeg", "jpg", IMimeMagic.JPEG_JPG),
  JPE("image/jpeg", "jpe"),
  JPEG("image/jpeg", "jpeg", IMimeMagic.JPEG_JPG),
  JFIF("image/jpeg", "jfif", IMimeMagic.JPEG_JPG),
  PJPEG("image/jpeg", "pjpeg", IMimeMagic.JPEG_JPG),
  PJP("image/jpeg", "pjp", IMimeMagic.JPEG_JPG),
  JS("application/javascript", "js"),
  JSON("application/json", "json"),
  JSONML("application/jsonml+json", "jsonml"),
  LOG("text/x-log", "log"),
  M2V("video/mpeg", "m2v"),
  MIME("message/rfc822", "mime"),
  MKV("video/x-matroska", "mkv"),
  MOV("video/quicktime", "mov"),
  MP3("audio/mpeg", "mp3", IMimeMagic.MP3),
  MP4("video/mp4", "mp4", IMimeMagic.MP4),
  MPG("video/mpeg", "mpg"),
  MSG("application/vnd.ms-outlook", "msg", IMimeMagic.MSG),
  M4P("audio/mp4a-latm", "m4p"),
  OGA("audio/ogg", "oga"),
  OGV("video/ogg", "ogv"),
  ONEPKG("application/onenote", "onepkg"),
  ONETMP("application/onenote", "onetmp"),
  ONETOC("application/onenote", "onetoc"),
  ONETOC2("application/onenote", "onetoc2"),
  PDF("application/pdf", "pdf", IMimeMagic.PDF),
  PNG("image/png", "png", IMimeMagic.PNG),
  PPT("application/vnd.ms-powerpoint", "ppt", IMimeMagic.DOC_XLS_PPT),
  PPSM("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", "ppsm", IMimeMagic.DOCX_XLSX_PPTX),
  PPTM("application/vnd.ms-powerpoint.presentation.macroEnabled.12", "pptm", IMimeMagic.DOCX_XLSX_PPTX),
  POTX("application/vnd.openxmlformats-officedocument.presentationml.template", "potx", IMimeMagic.DOCX_XLSX_PPTX),
  PPSX("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx", IMimeMagic.DOCX_XLSX_PPTX),
  PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", IMimeMagic.DOCX_XLSX_PPTX),
  RSS("application/rss+xml", "rss"),
  SLDX("application/vnd.openxmlformats-officedocument.presentationml.slide", "sldx"),
  SVG("image/svg+xml", "svg"),
  SVGZ("image/svg+xml", "svgz", IMimeMagic.GZ),
  THMX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "thmx", IMimeMagic.ZIP),
  TIF("image/tiff", "tif", IMimeMagic.TIF_TIFF),
  TIFF("image/tiff", "tiff", IMimeMagic.TIF_TIFF),
  TXT("text/plain", "txt"),
  VCARD("text/vcard", "vcard"),
  VCF("text/x-vcard", "vcf"),
  VCS("text/x-vcalendar", "vcs"),
  WEBM("video/webm", "webm"),
  WOFF("application/font-woff", "woff", IMimeMagic.WOFF),
  XLS("application/vnd.ms-excel", "xls", IMimeMagic.DOC_XLS_PPT),
  XLSB("application/vnd.ms-excel.sheet.binary.macroEnabled.12", "xlsb"),
  XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", IMimeMagic.DOCX_XLSX_PPTX),
  XLSM("application/vnd.ms-excel.sheet.macroEnabled.12", "xlsm", IMimeMagic.DOCX_XLSX_PPTX),
  XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx", IMimeMagic.DOCX_XLSX_PPTX),
  XML("text/xml", "xml"),
  URI("text/x-uri", "url"),
  ZIP("application/zip", "zip", IMimeMagic.ZIP);

  private final String m_type;
  private final String m_fileExtension;
  private IMimeMagic m_magic;

  MimeType(String type, String fileExtension) {
    m_type = type;
    m_fileExtension = fileExtension != null ? fileExtension.toLowerCase(Locale.ROOT) : null;
  }

  MimeType(String type, String fileExtension, IMimeMagic magic) {
    m_type = type;
    m_fileExtension = fileExtension != null ? fileExtension.toLowerCase(Locale.ROOT) : null;
    m_magic = magic;
  }

  /**
   * Use {@link MimeTypes#findByFileExtension(String)} in order to include extended enum values
   */
  public static MimeType findByFileExtension(String fileExtension) {
    for (MimeType mimeType : values()) {
      if (mimeType.getFileExtension().equalsIgnoreCase(fileExtension)) {
        return mimeType;
      }
    }
    return null;
  }

  /**
   * Use {@link MimeTypes#findByMimeTypeName(String)} in order to include extended enum values
   */
  public static MimeType convertToMimeType(String mimeTypeText) {
    for (MimeType mimeType : values()) {
      if (mimeType.getType().equals(mimeTypeText)) {
        return mimeType;
      }
    }
    return null;
  }

  @Override
  public String getType() {
    return m_type;
  }

  @Override
  public String getFileExtension() {
    return m_fileExtension;
  }

  @Override
  public IMimeMagic getMagic() {
    return m_magic;
  }

  @Override
  public void setMagic(IMimeMagic magic) {
    m_magic = magic;
  }

  /**
   * Common image mime types.
   */
  public static Collection<MimeType> getCommonImageTypes() {
    return Arrays.asList(getCommonImageTypesAsArray());
  }

  /**
   * Common image mime types.
   */
  public static MimeType[] getCommonImageTypesAsArray() {
    return new MimeType[]{BMP, GIF, JPG, JPE, JPEG, PNG, SVG, TIF, TIFF};
  }

  /**
   * Common video mime types.
   */
  public static Collection<MimeType> getCommonVideoTypes() {
    return Arrays.asList(getCommonVideoTypesAsArray());
  }

  /**
   * Common image mime types.
   */
  public static MimeType[] getCommonVideoTypesAsArray() {
    return new MimeType[]{AVI, M2V, MKV, MOV, MP4, MPG};
  }

  public static boolean isOneOf(Collection<MimeType> mimeTypes, String input) {
    for (MimeType mimeType : mimeTypes) {
      if (mimeType.getType().equals(input)) {
        return true;
      }
    }
    return false;
  }
}
