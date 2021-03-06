/*
 * An XML document type.
 * Localname: UserAccessAuditDetail
 * Namespace: http://schemas.microsoft.com/crm/2011/Contracts
 * Java type: com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetailDocument
 *
 * Automatically generated - do not modify.
 */
package com.microsoft.schemas.crm._2011.contracts.impl;
/**
 * A document containing one UserAccessAuditDetail(@http://schemas.microsoft.com/crm/2011/Contracts) element.
 *
 * This is a complex type.
 */
public class UserAccessAuditDetailDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetailDocument
{
    private static final long serialVersionUID = 1L;
    
    public UserAccessAuditDetailDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName USERACCESSAUDITDETAIL$0 = 
        new javax.xml.namespace.QName("http://schemas.microsoft.com/crm/2011/Contracts", "UserAccessAuditDetail");
    
    
    /**
     * Gets the "UserAccessAuditDetail" element
     */
    public com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail getUserAccessAuditDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail target = null;
            target = (com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail)get_store().find_element_user(USERACCESSAUDITDETAIL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Tests for nil "UserAccessAuditDetail" element
     */
    public boolean isNilUserAccessAuditDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail target = null;
            target = (com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail)get_store().find_element_user(USERACCESSAUDITDETAIL$0, 0);
            if (target == null) return false;
            return target.isNil();
        }
    }
    
    /**
     * Sets the "UserAccessAuditDetail" element
     */
    public void setUserAccessAuditDetail(com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail userAccessAuditDetail)
    {
        generatedSetterHelperImpl(userAccessAuditDetail, USERACCESSAUDITDETAIL$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "UserAccessAuditDetail" element
     */
    public com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail addNewUserAccessAuditDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail target = null;
            target = (com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail)get_store().add_element_user(USERACCESSAUDITDETAIL$0);
            return target;
        }
    }
    
    /**
     * Nils the "UserAccessAuditDetail" element
     */
    public void setNilUserAccessAuditDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail target = null;
            target = (com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail)get_store().find_element_user(USERACCESSAUDITDETAIL$0, 0);
            if (target == null)
            {
                target = (com.microsoft.schemas.crm._2011.contracts.UserAccessAuditDetail)get_store().add_element_user(USERACCESSAUDITDETAIL$0);
            }
            target.setNil();
        }
    }
}
