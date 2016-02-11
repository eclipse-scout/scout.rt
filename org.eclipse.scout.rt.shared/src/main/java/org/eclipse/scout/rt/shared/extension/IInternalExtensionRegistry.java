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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * Internal extension registry holding all additional operations that must be available to the Scout runtime.
 */
public interface IInternalExtensionRegistry extends IExtensionRegistry {
  /**
   * Gets all contribution classes that exist for the given container.
   *
   * @param container
   *          The container class. Must no be null.
   * @return A {@link Set} holding all contribution classes that exist for the given container.
   * @throws IllegalArgumentException
   *           if the container is null.
   */
  Set<Class<?>> getContributionsFor(Class<?> container);

  /**
   * Creates all contribution instances that exist for the given container and fulfill the given class filter. The
   * filter uses <code>instanceof</code> to check if a contribution is valid.
   *
   * @param container
   *          The container for which the contributions should be returned.
   * @param filterType
   *          The filter class or null if all contributions should be created and returned.
   * @return The created contributions for the given container matching the given filter.
   * @throws IllegalArgumentException
   *           if the container is null.
   */
  <T> List<T> createContributionsFor(Object container, Class<T> filterType);

  /**
   * Creates all {@link IExtension}s that exist for the given owner. These are all {@link IExtension}s whose owner is
   * assignable from the given owner.<br>
   * E.g.: If an extension is registered for owner {@link ICodeType} this extension is returned for all owners that are
   * <code>instanceof</code> {@link ICodeType}.
   *
   * @param owner
   *          The owner of the extensions.
   * @return a {@link List} with all {@link IExtension}s for the given owner.
   * @throws IllegalArgumentException
   *           if the owner is null.
   */
  <T extends IExtension<?>> List<T> createExtensionsFor(Object owner);

  /**
   * Creates one {@link MoveDescriptor} for the given model object if at least one model move operations was registered
   * for the given object or for one of its super classes or interfaces (i.e. <code>instanceof</code>). More than one
   * model move operations are computed into one {@link MoveDescriptor}. The method returns <code>null</code> if no move
   * operations were registered for the given model object. The optional parent model object iterator is used for model
   * move operations that were registered using deep linking (i.e. registered with a multi-segment
   * {@link ClassIdentifier}).
   *
   * @param modelObject
   *          The model object a move descriptor should be created for.
   * @param parentModelObjectIterator
   *          Optional iterator that is going through the list of parent model objects. It is used for evaluating
   *          deep-linked move operations (i.e. those registered by
   *          {@link IExtensionRegistry#registerMove(org.eclipse.scout.rt.platform.classid.ClassIdentifier, Double, Class)}
   *          ). May be <code>null</code>.
   * @return <code>null</code> if no move operations were registered for the given model object or one
   *         {@link MoveDescriptor}.
   */
  <T> MoveDescriptor<T> createModelMoveDescriptorFor(T modelObject, Iterator<?> parentModelObjectIterator);

  /**
   * Pushes the given extensions on the top stack.
   *
   * @param extensions
   */
  void pushExtensions(List<? extends IExtension<?>> extensions);

  /**
   * Removes the last pushed extensions from the stack.
   *
   * @param extensions
   */
  void popExtensions(List<? extends IExtension<?>> extensions);

  /**
   * Pushes the given scope class to the stack.
   *
   * @param scopeClass
   */
  void pushScope(Class<?> scopeClass);

  /**
   * Removes the last pushed scope class from the stack.
   */
  void popScope();

  /**
   * Creates a backup of the current extension context (i.e. extension and scope stacks).
   */
  ExtensionContext backupExtensionContext();

  /**
   * Executes the given runnable in the given {@link ExtensionContext}.
   */
  void runInContext(ExtensionContext ctx, Runnable runnable);
}
