package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield;

import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField;

public interface IFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends IValueFieldExtension<BinaryResource, OWNER> {
}
