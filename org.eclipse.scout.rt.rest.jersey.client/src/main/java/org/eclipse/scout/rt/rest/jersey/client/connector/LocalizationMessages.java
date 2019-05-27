package org.eclipse.scout.rt.rest.jersey.client.connector;

import org.glassfish.jersey.internal.l10n.Localizable;
import org.glassfish.jersey.internal.l10n.LocalizableMessageFactory;
import org.glassfish.jersey.internal.l10n.Localizer;

/**
 * Defines string formatting method for each constant in the resource file
 */
public final class LocalizationMessages {

  private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("org.glassfish.jersey.apache.connector.localization");
  private final static Localizer localizer = new Localizer();

  public static Localizable localizableEXPECTED_CONNECTOR_PROVIDER_NOT_USED() {
    return messageFactory.getMessage("expected.connector.provider.not.used");
  }

  /**
   * The supplied component is not configured to use a ApacheConnectorProvider.
   */
  public static String EXPECTED_CONNECTOR_PROVIDER_NOT_USED() {
    return localizer.localize(localizableEXPECTED_CONNECTOR_PROVIDER_NOT_USED());
  }

  public static Localizable localizableERROR_BUFFERING_ENTITY() {
    return messageFactory.getMessage("error.buffering.entity");
  }

  /**
   * Error buffering the entity.
   */
  public static String ERROR_BUFFERING_ENTITY() {
    return localizer.localize(localizableERROR_BUFFERING_ENTITY());
  }

  public static Localizable localizableINVALID_CONFIGURABLE_COMPONENT_TYPE(Object arg0) {
    return messageFactory.getMessage("invalid.configurable.component.type", arg0);
  }

  /**
   * The supplied component "{0}" is not assignable from JerseyClient or JerseyWebTarget.
   */
  public static String INVALID_CONFIGURABLE_COMPONENT_TYPE(Object arg0) {
    return localizer.localize(localizableINVALID_CONFIGURABLE_COMPONENT_TYPE(arg0));
  }

  public static Localizable localizableIGNORING_VALUE_OF_PROPERTY(Object arg0, Object arg1, Object arg2) {
    return messageFactory.getMessage("ignoring.value.of.property", arg0, arg1, arg2);
  }

  /**
   * Ignoring value of property "{0}" ("{1}") - not instance of "{2}".
   */
  public static String IGNORING_VALUE_OF_PROPERTY(Object arg0, Object arg1, Object arg2) {
    return localizer.localize(localizableIGNORING_VALUE_OF_PROPERTY(arg0, arg1, arg2));
  }

  public static Localizable localizableWRONG_PROXY_URI_TYPE(Object arg0) {
    return messageFactory.getMessage("wrong.proxy.uri.type", arg0);
  }

  /**
   * The proxy URI ("{0}") property MUST be an instance of String or URI.
   */
  public static String WRONG_PROXY_URI_TYPE(Object arg0) {
    return localizer.localize(localizableWRONG_PROXY_URI_TYPE(arg0));
  }

  public static Localizable localizableFAILED_TO_STOP_CLIENT() {
    return messageFactory.getMessage("failed.to.stop.client");
  }

  /**
   * Failed to stop the client.
   */
  public static String FAILED_TO_STOP_CLIENT() {
    return localizer.localize(localizableFAILED_TO_STOP_CLIENT());
  }

}
