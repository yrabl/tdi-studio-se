
package org.talend.mdm.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WSRoutingRulePKArray complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WSRoutingRulePKArray"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="wsRoutingRulePKs" type="{http://www.talend.com/mdm}WSRoutingRulePK" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSRoutingRulePKArray", propOrder = {
    "wsRoutingRulePKs"
})
public class WSRoutingRulePKArray {

    @XmlElement(nillable = true)
    protected List<WSRoutingRulePK> wsRoutingRulePKs;

    /**
     * Default no-arg constructor
     * 
     */
    public WSRoutingRulePKArray() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public WSRoutingRulePKArray(final List<WSRoutingRulePK> wsRoutingRulePKs) {
        this.wsRoutingRulePKs = wsRoutingRulePKs;
    }

    /**
     * Gets the value of the wsRoutingRulePKs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wsRoutingRulePKs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWsRoutingRulePKs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WSRoutingRulePK }
     * 
     * 
     */
    public List<WSRoutingRulePK> getWsRoutingRulePKs() {
        if (wsRoutingRulePKs == null) {
            wsRoutingRulePKs = new ArrayList<WSRoutingRulePK>();
        }
        return this.wsRoutingRulePKs;
    }

}
