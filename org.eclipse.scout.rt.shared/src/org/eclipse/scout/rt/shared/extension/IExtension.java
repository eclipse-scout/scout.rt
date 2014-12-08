package org.eclipse.scout.rt.shared.extension;

/**
 * Super interface of all Scout model extensions.<br>
 * Extensions can be applied to all {@link IExtensibleObject}s.<br>
 * Use the {@link IExtensionRegistry} service to register your extensions.
 *
 * @since 4.2
 */
public interface IExtension<OWNER extends IExtensibleObject> {

  /**
   * @return the owner of the extension (the object that is extended).
   */
  OWNER getOwner();
}
