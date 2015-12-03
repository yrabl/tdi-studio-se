/**
 */
package org.talend.component.ui.model.genericMetadata;

import org.talend.core.model.metadata.builder.connection.Connection;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Generic Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.component.ui.model.genericMetadata.GenericConnection#getCompProperties <em>Comp Properties</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.component.ui.model.genericMetadata.GenericMetadataPackage#getGenericConnection()
 * @model
 * @generated
 */
public interface GenericConnection extends Connection {
    /**
     * Returns the value of the '<em><b>Comp Properties</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comp Properties</em>' map isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Comp Properties</em>' attribute.
     * @see #setCompProperties(String)
     * @see org.talend.component.ui.model.genericMetadata.GenericMetadataPackage#getGenericConnection_CompProperties()
     * @model
     * @generated
     */
    String getCompProperties();

    /**
     * Sets the value of the '{@link org.talend.component.ui.model.genericMetadata.GenericConnection#getCompProperties <em>Comp Properties</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Comp Properties</em>' attribute.
     * @see #getCompProperties()
     * @generated
     */
    void setCompProperties(String value);

} // GenericConnection
