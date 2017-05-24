package org.eclipse.scout.rt.platform.resource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
  private boolean m_cachingAllowed = false; // default
  private int m_cacheMaxAge = 0; // default

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
        .withLastModified(resource.getLastModified())
        .withCachingAllowed(resource.isCachingAllowed())
        .withCacheMaxAge(resource.getCacheMaxAge());
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
   * {@link StandardCharsets#UTF_8} encoding is used.
   *
   * @see #withContent(String, Charset).
   */
  public BinaryResources withContent(String content) {
    return withContent(content, StandardCharsets.UTF_8);
  }

  /**
   * @param content
   *          The resource's content as string, the provided encoding will be used. If <code>null</code>, content and
   *          charset will be <code>null</code>. The fingerprint for the given content is calculated automatically.
   * @param charset
   *          Charset to use for string content encoding and as {@link BinaryResource} charset. If <code>null</code> is
   *          provided, {@link StandardCharsets#UTF_8} is used.
   */
  public BinaryResources withContent(String content, Charset charset) {
    if (charset == null) {
      charset = StandardCharsets.UTF_8;
    }

    m_content = content == null ? null : content.getBytes(charset);
    m_charset = content == null ? null : charset.name();

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
   * Enables / disables caching of this resource, default is false
   */
  public BinaryResources withCachingAllowed(boolean enabled) {
    m_cachingAllowed = enabled;
    return this;
  }

  /**
   * Set cacheMaxAge. Ignored if {@link #withCachingAllowed(boolean)} is false.
   * <p>
   * see IHttpCacheControl constants
   */
  public BinaryResources withCacheMaxAge(int cacheMaxAge) {
    m_cacheMaxAge = cacheMaxAge;
    return this;
  }

  /**
   * @return Built binary resource
   */
  public BinaryResource build() {
    return new BinaryResource(m_filename, m_contentType, m_charset, m_content, m_lastModified, m_cachingAllowed, m_cacheMaxAge);
  }
}
