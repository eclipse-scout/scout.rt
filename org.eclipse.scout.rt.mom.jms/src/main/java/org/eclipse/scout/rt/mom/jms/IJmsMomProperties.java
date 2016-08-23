package org.eclipse.scout.rt.mom.jms;

/**
 * Properties used in {@link JmsMom} implementation.
 */
public interface IJmsMomProperties {

  /**
   * The property id for the unique ID of a 'request-reply' communication.
   */
  String PROP_REPLY_ID = "x-scout.mom.requestreply.id";

  /**
   * The property id to check whether 'request-reply' communication failed, meaning that request processing failed.
   */
  String PROP_REQUEST_REPLY_SUCCESS = "x-scout.mom.requestreply.code";

  /**
   * The property id to query the keys of a marshaller context.
   */
  String PROP_MARSHALLER_CONTEXT = "x-scout.mom.marshaller.properties";

  /**
   * The property id to query the keys of an encryption context.
   */
  String PROP_ENCRYPTER_CONTEXT = "x-scout.mom.encryption.properties";

  /**
   * The property ID to indicate whether this is a <code>null</code> object.
   */
  String PROP_NULL_OBJECT = "x-scout.mom.transferobject.null";
}
