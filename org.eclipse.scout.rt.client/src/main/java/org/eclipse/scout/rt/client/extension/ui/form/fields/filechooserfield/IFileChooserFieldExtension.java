package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public interface IFileChooserFieldExtension<OWNER extends AbstractFileChooserField> extends IValueFieldExtension<BinaryResource, OWNER> {
}
