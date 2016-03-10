package org.eclipse.scout.rt.platform.resource;

import java.nio.charset.Charset;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

/**
 * Builder for {@link BinaryResource}.
 *
 * @since 5.2
 */
public final class BinaryResources {

  private String m_filename;
  private String m_contentType;
  private String m_charset;
  private byte[] m_content;
  private long m_lastModified = -1; // default

  private BinaryResources() {
  }

  public static BinaryResources create() {
    return new BinaryResources();
  }

  /**
   * Creates a builder for the {@link BinaryResource} fill with the values of the provided resource.
   */
  public static BinaryResources create(BinaryResource resource) {
    return create()
        .withFilename(resource.getFilename())
        .withContentType(resource.getContentType())
        .withCharset(resource.getCharset())
        .withContent(resource.getContent())
        .withLastModified(resource.getLastModified());
  }

  /**
   * @param filename
   *          A valid file name (with or without path information), preferably with file extension to allow automatic
   *          detection of MIME type. Examples: <code>"image.jpg"</code>, <code>"icons/eye.png"</code>
   */
  public BinaryResources withFilename(String filename) {
    m_filename = filename;
    return this;
  }

  /**
   * @param contentType
   *          MIME type of the resource. Example: <code>"image/jpeg"</code>. If this value is omitted, it is recommended
   *          to ensure that the argument <i>filename</i> has a valid file extension, which can then be used to
   *          determine the MIME type.
   *          <p>
   *          null contentType is replaced by {@link FileUtility#getContentTypeForExtension(String)}
   */
  public BinaryResources withContentType(String contentType) {
    m_contentType = contentType;
    return this;
  }

  /**
   * Convenience method that supports {@link java.nio.charset.Charset}.
   *
   * @param charset
   *          Charset for encoding
   */
  public BinaryResources withCharset(Charset charset) {
    m_charset = charset == null ? null : charset.name();
    return this;
  }

  /**
   * @param charset
   *          Charset for encoding
   */
  public BinaryResources withCharset(String charset) {
    m_charset = charset;
    return this;
  }

  /**
   * @param content
   *          The resource's content as byte array. The fingerprint for the given content is calculated automatically.
   */
  public BinaryResources withContent(byte[] content) {
    m_content = content;
    return this;
  }

  /**
   * @param lastModified
   *          "Last modified" timestamp of the resource (in milliseconds a.k.a. UNIX time). <code>-1</code> if unknown.
   */
  public BinaryResources withLastModified(long lastModified) {
    m_lastModified = lastModified;
    return this;
  }

  /**
   * Sets the last modified to now (according to {@link IDateProvider#currentMillis()}).
   */
  public BinaryResources withLastModifiedNow() {
    return withLastModified(BEANS.get(IDateProvider.class).currentMillis().getTime());
  }

  /**
   * @return Built binary resource
   */
  @SuppressWarnings("deprecation")
  public BinaryResource build() {
    // constructor will be changed to package private in 6.0, thus suppress deprecation warning in 5.2
    return new BinaryResource(m_filename, m_contentType, m_charset, m_content, m_lastModified);
  }
}
