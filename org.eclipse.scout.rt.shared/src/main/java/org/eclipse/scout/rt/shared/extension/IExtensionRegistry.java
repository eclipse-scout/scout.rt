/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * The Scout Extension Registry interface used to hold all extensions, contributions and moves within a scout
 * application.<br>
 * A registration may be an {@link IExtension}, a contribution to an owner implementing {@link IContributionOwner} or a
 * move of an existing class.
 *
 * @since 4.2
 */
public interface IExtensionRegistry extends IService {

  /**
   * Registers a content and/or behavior modification.<br>
   * This method supports two different types of registrations:
   * <ol>
   * <li><b>Extensions</b> (behavior modification)</li><br>
   * This is the case if the given extensionOrContributionClass is <code>instanceof</code> {@link IExtension}.<br>
   * The extension is applied to an owner which is detected according to the following rule:
   * <ol>
   * <li>The type parameter of the extension class is used as owner if available.</li>
   * <li>Otherwise the value of the {@link Extends} annotation on the extension class is used as owner if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * Extensions may also contain contributions. These are automatically registered as well. See next section for details
   * about contributions. <br>
   * <br>
   * <li><b>Contributions</b> (new content)</li> <br>
   * This is the case if the given extensionOrContributionClass is NOT <code>instanceof</code> {@link IExtension}.<br>
   * These are classes that are contributed to an owner implementing {@link IContributionOwner}.<br>
   * The owner is detected according to the following rule:
   * <ol>
   * <li>The value of the {@link Extends} annotation on the contribution class is used as owner if present.</li>
   * <li>Otherwise the owner of the surrounding {@link IExtension} class is reused if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * The detected owner must support the type of the given contribution. E.g.: A contribution of type IMenu is valid for
   * an IMenu owner because menus may contain other menus. But this contribution may not be valid for {@link ICode}
   * owners because these do not support menus.<br>
   * A contribution is considered to be valid for an owner if there is at least one
   * {@link IExtensionRegistrationValidatorService} registered that declares the given combination to be valid.<br>
   * </ol>
   *
   * @param extensionOrContributionClass
   *          The extension or contribution class that should be registered. Must not be null and must either be a
   *          primary class or a static class. See above for other restrictions on the given
   *          extensionOrContributionClass parameter.
   * @throws IllegalExtensionException
   *           if the extensionOrContributionClass does not fulfill the restrictions described above.
   */
  void register(Class<?> extensionOrContributionClass);

  /**
   * Registers a content and/or behavior modification for the given owner.<br>
   * This method supports two different types of registrations:
   * <ol>
   * <li><b>Extensions</b> (behavior modification)</li><br>
   * These are classes that implement {@link IExtension}. In that case the given ownerClass must be
   * <code>instanceof</code> the OWNER type parameter of the given extensionOrContributionClass.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The type parameter of the extension class is used as owner if available.</li>
   * <li>Otherwise the value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * Extensions may also contain contributions. These are automatically registered as well. See next section for details
   * about contributions. <br>
   * <br>
   * <li><b>Contributions</b> (new content)</li> <br>
   * These are classes that are contributed to the given owner implementing {@link IContributionOwner}. In that case the
   * given owner must be valid for the given contribution. This means the given owner must support the type of the given
   * contribution.<br>
   * E.g.: A contribution of type IMenu is valid for an IMenu owner because menus may contain other menus. But this
   * contribution may not be valid for {@link ICode} owners because these do not support menus.<br>
   * A contribution is considered to be valid if there is at least one {@link IExtensionRegistrationValidatorService}
   * registered that declares the given combination to be valid.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>Otherwise the owner of the surrounding {@link IExtension} class is reused if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * </ol>
   *
   * @param extensionOrContributionClass
   *          The extension or contribution class that should be applied to the given owner. Must not be null and must
   *          either be a primary class or a static class. See above for other restrictions on the given
   *          extensionOrContributionClass parameter.
   * @param ownerClass
   *          The owner class that should be modified. May be null. See above for restrictions on the owner parameter.
   * @throws IllegalExtensionException
   *           if one of the parameters does not fulfill the restrictions described above.
   */
  void register(Class<?> extensionOrContributionClass, Class<?> ownerClass);

  /**
   * Registers a content and/or behavior modification for the given owner.<br>
   * This method supports two different types of registrations:
   * <ol>
   * <li><b>Extensions</b> (behavior modification)</li><br>
   * These are classes that implement {@link IExtension}. In that case the given ownerClass must be
   * <code>instanceof</code> the OWNER type parameter of the given extensionOrContributionClass.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The type parameter of the extension class is used as owner if available.</li>
   * <li>Otherwise the value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * Extensions may also contain contributions. These are automatically registered as well. See next section for details
   * about contributions. <br>
   * <br>
   * <li><b>Contributions</b> (new content)</li> <br>
   * These are classes that are contributed to the given owner implementing {@link IContributionOwner}. In that case the
   * given owner must be valid for the given contribution. This means the given owner must support the type of the given
   * contribution.<br>
   * E.g.: A contribution of type IMenu is valid for an IMenu owner because menus may contain other menus. But this
   * contribution may not be valid for {@link ICode} owners because these do not support menus.<br>
   * A contribution is considered to be valid if there is at least one {@link IExtensionRegistrationValidatorService}
   * registered that declares the given combination to be valid.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>Otherwise the owner of the surrounding {@link IExtension} class is reused if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * It is also possible to provide an order when registering contributions (parameter order). This is supported for all
   * contributions of type {@link IOrdered}. If this order is provided it is automatically applied to the created
   * contribution instances and overwrites any {@link Order} annotation that may be present on the contribution.
   * </ol>
   *
   * @param extensionOrContributionClass
   *          The extension or contribution class that should be applied to the given owner. Must not be null and must
   *          either be a primary class or a static class. See above for other restrictions on the given
   *          extensionOrContributionClass parameter.
   * @param ownerClass
   *          The owner class that should be modified. May be null. See above for restrictions on the owner parameter.
   * @param order
   *          The order to be used for the given contribution. May be null. See above for restrictions on the order
   *          parameter.
   * @throws IllegalExtensionException
   *           if one of the parameters does not fulfill the restrictions described above.
   */
  void register(Class<?> extensionOrContributionClass, Class<?> ownerClass, Double order);

  /**
   * Registers a content and/or behavior modification for the given owner.<br>
   * This method supports two different types of registrations:
   * <ol>
   * <li><b>Extensions</b> (behavior modification)</li><br>
   * These are classes that implement {@link IExtension}. In that case the given ownerClass must be
   * <code>instanceof</code> the OWNER type parameter of the given extensionOrContributionClass.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The type parameter of the extension class is used as owner if available.</li>
   * <li>Otherwise the value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * Extensions may also contain contributions. These are automatically registered as well. See next section for details
   * about contributions. <br>
   * <br>
   * <li><b>Contributions</b> (new content)</li> <br>
   * These are classes that are contributed to the given owner implementing {@link IContributionOwner}. In that case the
   * given owner must be valid for the given contribution. This means the given owner must support the type of the given
   * contribution.<br>
   * E.g.: A contribution of type IMenu is valid for an IMenu owner because menus may contain other menus. But this
   * contribution may not be valid for {@link ICode} owners because these do not support menus.<br>
   * A contribution is considered to be valid if there is at least one {@link IExtensionRegistrationValidatorService}
   * registered that declares the given combination to be valid.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>Otherwise the owner of the surrounding {@link IExtension} class is reused if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * </ol>
   *
   * @param extensionOrContributionClass
   *          The extension or contribution class that should be applied to the given owner. Must not be null and must
   *          either be a primary class or a static class. See above for other restrictions on the given
   *          extensionOrContributionClass parameter.
   * @param ownerClassIdentifier
   *          The {@link ClassIdentifier} describing the owner that should be modified. May be null. See above for
   *          restrictions on the owner parameter.
   * @throws IllegalExtensionException
   *           if one of the parameters does not fulfill the restrictions described above.
   * @see ClassIdentifier
   */
  void register(Class<?> extensionOrContributionClass, ClassIdentifier ownerClassIdentifier);

  /**
   * Registers a content and/or behavior modification for the given owner.<br>
   * This method supports two different types of registrations:
   * <ol>
   * <li><b>Extensions</b> (behavior modification)</li><br>
   * These are classes that implement {@link IExtension}. In that case the given ownerClass must be
   * <code>instanceof</code> the OWNER type parameter of the given extensionOrContributionClass.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The type parameter of the extension class is used as owner if available.</li>
   * <li>Otherwise the value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * Extensions may also contain contributions. These are automatically registered as well. See next section for details
   * about contributions. <br>
   * <br>
   * <li><b>Contributions</b> (new content)</li> <br>
   * These are classes that are contributed to the given owner implementing {@link IContributionOwner}. In that case the
   * given owner must be valid for the given contribution. This means the given owner must support the type of the given
   * contribution.<br>
   * E.g.: A contribution of type IMenu is valid for an IMenu owner because menus may contain other menus. But this
   * contribution may not be valid for {@link ICode} owners because these do not support menus.<br>
   * A contribution is considered to be valid if there is at least one {@link IExtensionRegistrationValidatorService}
   * registered that declares the given combination to be valid.<br>
   * If no owner is provided it is detected according to the following rule:
   * <ol>
   * <li>The value of the {@link Extends} annotation is used as owner if present.</li>
   * <li>Otherwise the owner of the surrounding {@link IExtension} class is reused if present.</li>
   * <li>If still no owner could be determined, an {@link IllegalExtensionException} is thrown.</li>
   * </ol>
   * It is also possible to provide an order when registering contributions (parameter order). This is supported for all
   * contributions of type {@link IOrdered}. If this order is provided it is automatically applied to the created
   * contribution instances and overwrites any {@link Order} annotation that may be present on the contribution.
   * </ol>
   *
   * @param extensionOrContributionClass
   *          The extension or contribution class that should be applied to the given owner. Must not be null and must
   *          either be a primary class or a static class. See above for other restrictions on the given
   *          extensionOrContributionClass parameter.
   * @param ownerClassIdentifier
   *          The {@link ClassIdentifier} describing the owner that should be modified. May be null. See above for
   *          restrictions on the owner parameter.
   * @param order
   *          The order to be used for the given contribution. May be null. See above for restrictions on the order
   *          parameter.
   * @throws IllegalExtensionException
   *           if one of the parameters does not fulfill the restrictions described above.
   * @see ClassIdentifier
   */
  void register(Class<?> extensionOrContributionClass, ClassIdentifier ownerClassIdentifier, Double order);

  /**
   * Registers a move operation for the given {@link IOrdered} model class without changing its current container.
   *
   * @param modelClass
   *          The model class that should be moved. Must not be null.
   * @param newOrder
   *          The new order.
   * @throws IllegalExtensionException
   *           If the modelClass is null.
   */
  void registerMove(Class<? extends IOrdered> modelClass, double newOrder);

  /**
   * Registers a move operation for the given {@link IOrdered} model object. The element is moved from its current
   * container into the context-specific root container. The root container of a form field is the hosting form's root
   * group box. The root of an {@link ICode} is its {@link ICodeType}'s root code list.
   * <p/>
   * This method has the same effect as invoking {@link #registerMove(Class, Double, Class)} using
   * {@link IMoveModelObjectToRootMarker} as target class.
   *
   * @param modelClass
   *          The model class that should be moved. Must not be null.
   * @param newOrder
   *          The new order or null if the order should not be changed.
   * @throws IllegalExtensionException
   *           If the modelClass is null or the given model class cannot be moved into another container.
   */
  void registerMoveToRoot(Class<? extends IOrdered> modelClass, Double newOrder);

  /**
   * Registers a move operation for the given {@link IOrdered} model object. The method allows to define a new order,
   * the new container class or both of them.<br>
   * At least a new order or a new container must be specified.
   *
   * @param modelClass
   *          The model class that should be moved. Must not be null.
   * @param newOrder
   *          The new order. If null, the existing order is not changed.
   * @param newContainerClass
   *          The new container class. The container is not changed if it is null. A matching model object is moved to
   *          top level if the new container class is {@link IMoveModelObjectToRootMarker}.
   * @throws IllegalExtensionException
   *           In the following cases:
   *           <ul>
   *           <li>If the modelClass is null</li>
   *           <li>If neither a new order nor a new container has been specified</li>
   *           <li>If the new container is the model itself</li>
   *           <li>If the given container is not supported for the given model</li>
   *           </ul>
   */
  void registerMove(Class<? extends IOrdered> modelClass, Double newOrder, Class<? extends IOrdered> newContainerClass);

  /**
   * Registers a move operation for the given modelClassIdentifier. The last segment of the {@link ClassIdentifier} must
   * be instanceof {@link IOrdered}.<br>
   * The given model will be moved to the given new order without changing its current container.
   *
   * @param modelClassIdentifier
   *          The {@link ClassIdentifier} for the model for which the order should be changed.
   * @param newOrder
   *          The new order.
   * @throws IllegalExtensionException
   *           If the modelClass is null or the last segment of the {@link ClassIdentifier} is no {@link IOrdered}
   *           class.
   * @see ClassIdentifier
   */
  void registerMove(ClassIdentifier modelClassIdentifier, double newOrder);

  /**
   * Registers a move operation for the given modelClassIdentifier. The last segment of the {@link ClassIdentifier} must
   * be instanceof {@link IOrdered}.<br>
   * The method allows to define a new order, a new container class or both of them.<br>
   * At least a new order or a new container must be specified.
   *
   * @param modelClassIdentifier
   *          The {@link ClassIdentifier} for the model for which the order should be changed.
   * @param newOrder
   *          The new order. If null, the existing order is not changed.
   * @param newContainerClass
   *          The new container class. The container is not changed if it is null. A matching model object is moved to
   *          top level if the new container class is {@link IMoveModelObjectToRootMarker}.
   * @throws IllegalExtensionException
   *           In the following cases:
   *           <ul>
   *           <li>If the modelClassIdentifier is null</li>
   *           <li>If neither a new order nor a new container has been specified</li>
   *           <li>If the new container is the model itself</li>
   *           <li>If the given container is not supported for the given model</li>
   *           <li>If the last segment of the given modelClassIdentifier is no {@link IOrdered} class.</li>
   *           </ul>
   * @see ClassIdentifier
   */
  void registerMove(ClassIdentifier modelClassIdentifier, Double newOrder, Class<? extends IOrdered> newContainerClass);

  /**
   * Registers a move operation for the given modelClassIdentifier. The last segment of the modelClassIdentifier must be
   * instanceof {@link IOrdered}.<br>
   * The method allows to define a new order, a new container class or both of them.<br>
   * At least a new order or a new container must be specified.
   *
   * @param modelClassIdentifer
   *          The {@link ClassIdentifier} for the model for which the order should be changed.
   * @param newOrder
   *          The new order. If null, the existing order is not changed.
   * @param newContainerClassIdentifier
   *          The {@link ClassIdentifier} describing the container in which the given modelClassIdentifer should be
   *          moved. The container is not changed if it is null. A matching model object is moved to top level if the
   *          newContainerClassIdentifier only contains {@link IMoveModelObjectToRootMarker}.
   * @throws IllegalExtensionException
   *           In the following cases:
   *           <ul>
   *           <li>If the modelClassIdentifier is null</li>
   *           <li>If neither a new order nor a new container has been specified</li>
   *           <li>If the new container is the model itself</li>
   *           <li>If the given container is not supported for the given model</li>
   *           <li>If the last segment of the given modelClassIdentifier or newContainerClassIdentifier is no
   *           {@link IOrdered} class.</li>
   *           </ul>
   * @see ClassIdentifier
   */
  void registerMove(ClassIdentifier modelClassIdentifer, Double newOrder, ClassIdentifier newContainerClassIdentifier);

  /**
   * Registers a move operation for the given modelClassIdentifer. The element is moved from its current container into
   * the context-specific root container. The root container of a form field is the hosting form's root group box. The
   * root of an {@link ICode} is its {@link ICodeType}'s root code list.
   * <p/>
   * This method has the same effect as invoking {@link #registerMove(ClassIdentifier, Double, Class)} using
   * {@link IMoveModelObjectToRootMarker} as target class.
   *
   * @param modelClassIdentifer
   *          The {@link ClassIdentifier} describing the object to be moved.
   * @param newOrder
   *          The new order or null if the order should not be changed.
   * @throws IllegalExtensionException
   *           If the modelClassIdentifer is null or the given model class cannot be moved into another container or the
   *           last segment of the given modelClassIdentifer is no {@link IOrdered} class.
   */
  void registerMoveToRoot(ClassIdentifier modelClassIdentifer, Double newOrder);

  /**
   * Deregisters the given extension or contribution from all owners it has been registered for.<br>
   * If it is an {@link IExtension} also the nested contributions are deregistered.
   *
   * @param extensionOrContributionClass
   *          The extension or contribution that should be removed from this {@link IExtensionRegistry}. May not be
   *          null.
   * @return <code>true</code> if the {@link IExtensionRegistry} has been changed. <code>false</code> if nothing has
   *         been deregistered.
   * @throws IllegalExtensionException
   *           If the extensionOrContributionClass is null.
   * @see #register(Class)
   */
  boolean deregister(Class<?> extensionOrContributionClass);

  /**
   * Deregisters the given move registration.
   *
   * @param modelClass
   *          The model for which the move should be deregistered. Must not be null.
   * @param newOrder
   *          Describes the new order of the move that should be deregistered.
   * @return <code>true</code> if the {@link IExtensionRegistry} has been changed. <code>false</code> if nothing has
   *         been deregistered.
   * @throws IllegalExtensionException
   *           if the modelClass is null.
   * @see #registerMove(Class, double)
   */
  boolean deregisterMove(Class<? extends IOrdered> modelClass, double newOrder);

  /**
   * Deregisters the given move registration.
   *
   * @param modelClass
   *          The model for which the move should be deregistered. Must not be null.
   * @param newOrder
   *          Describes the new order of the move that should be deregistered. May be null in case the move only changes
   *          the container.
   * @param newContainerClass
   *          The new container of the move that should be deregistered. May be null in case the move only changes the
   *          order.
   * @return <code>true</code> if the {@link IExtensionRegistry} has been changed. <code>false</code> if nothing has
   *         been deregistered.
   * @throws IllegalExtensionException
   *           if the modelClass is null or newOrder and newContainerClass are null.
   * @see #registerMove(Class, Double, Class)
   */
  boolean deregisterMove(Class<? extends IOrdered> modelClass, Double newOrder, Class<? extends IOrdered> newContainerClass);

  /**
   * Deregisters the given move registration.
   *
   * @param modelClassIdentifier
   *          The {@link ClassIdentifier} of the model for which the move should be deregistered.
   * @param newOrder
   *          Describes the new order of the move that should be deregistered. May be null in case the move only changes
   *          the container.
   * @param newContainerClass
   *          The new container of the move that should be deregistered. May be null in case the move only changes the
   *          order.
   * @return <code>true</code> if the {@link IExtensionRegistry} has been changed. <code>false</code> if nothing has
   *         been deregistered.
   * @throws IllegalExtensionException
   *           if the modelClass is null or newOrder and newContainerClass are null.
   */
  boolean deregisterMove(ClassIdentifier modelClassIdentifier, Double newOrder, Class<? extends IOrdered> newContainerClass);

  /**
   * Deregisters the given move registration.
   *
   * @param modelClassIdentifier
   *          The {@link ClassIdentifier} of the model for which the move should be deregistered.
   * @param newOrder
   *          Describes the new order of the move that should be deregistered. May be null in case the move only changes
   *          the container.
   * @param newContainerClassIdentifier
   *          The new container of the move that should be deregistered. May be null in case the move only changes the
   *          order.
   * @return <code>true</code> if the {@link IExtensionRegistry} has been changed. <code>false</code> if nothing has
   *         been deregistered.
   * @throws IllegalExtensionException
   *           if the modelClass is null or newOrder and newContainerClass are null.
   */
  boolean deregisterMove(ClassIdentifier modelClassIdentifier, Double newOrder, ClassIdentifier newContainerClassIdentifier);
}
