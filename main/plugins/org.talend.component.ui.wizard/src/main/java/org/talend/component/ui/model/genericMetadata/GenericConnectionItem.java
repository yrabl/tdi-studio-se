/**
 */
package org.talend.component.ui.model.genericMetadata;

import org.talend.core.model.properties.ConnectionItem;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Generic Connection Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.component.ui.model.genericMetadata.GenericConnectionItem#getTypeName <em>Type Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.component.ui.model.genericMetadata.GenericMetadataPackage#getGenericConnectionItem()
 * @model
 * @generated
 */
public interface GenericConnectionItem extends ConnectionItem {

    /**
     * Returns the value of the '<em><b>Type Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type Name</em>' attribute.
     * @see #setTypeName(String)
     * @see org.talend.component.ui.model.genericMetadata.GenericMetadataPackage#getGenericConnectionItem_TypeName()
     * @model required="true"
     * @generated
     */
    String getTypeName();

    /**
     * Sets the value of the '{@link org.talend.component.ui.model.genericMetadata.GenericConnectionItem#getTypeName <em>Type Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type Name</em>' attribute.
     * @see #getTypeName()
     * @generated
     */
    void setTypeName(String value);
} // GenericConnectionItem
