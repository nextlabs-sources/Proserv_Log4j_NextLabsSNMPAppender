/*
 *
 *
============================================================================
 *     This license is based on the Apache Software License, Version 1.1
 *
============================================================================
 *
 *    Copyright (C) 2001-2003 Mark Masterson. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names Mark Masterson, M2 Technologies, SNMPTrapAppender or log4j must
 *    not be used to endorse or promote products derived  from this  software
 *    without  prior written permission. For written permission, please contact
 *    m.masterson@computer.org.
 *
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

package org.apache.log4j.ext;

import java.net.InetAddress;

import org.apache.logging.log4j.status.StatusLogger;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPduEncodingException;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpTrapHandler;
import org.opennms.protocols.snmp.SnmpTrapSession;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * This class makes use of the JoeSNMP library to implement the underlying SNMP
 * protocol(s).  The JoeSNMP library is a part of the OpenNMS project, and is
 * available/distributed under the GNU Lesser General Public License (LGPL).<p>
 *
 * To quote the JoeSNMP FAQ:<br><pre>
 * "What license is JoeSNMP released under?<br>
 *  <br>
 *	JoeSNMP is released under the GNU Lesser General Public License (LGPL),<br>
 *	documented fully at http://www.fsf.org/copyleft/lesser.html.<br>
 *	Effectively, this means that JoeSNMP is free to distribute and modify as<br>
 *	long as you provide your modified code back to the community.  And if<br>
 *	you'd like to use JoeSNMP as a library within your commercial product,<br>
 *	you are welcome to do so as well, but again, any changes to the<br>
 *	library itself need to be contributed back."</pre><p>
 *
 * You can get more information about OpenNMS <a href="http://www.opennms.org
 * /">here</a>.  You can get a copy of JoeSNMP <a href="http://www.opennms.org
 * /files/releases/joeSNMP/">here</a>.<br>
 *
 * You will need a copy of the library ("joesnmp-0.2.6.jar" at a minimum) to use
 * and/or compile this class.<br>
 * WARNING: The JoeSNMP library <b>requires a minimum of JDK 1.2</b>.
 * <p>
 * @version 2.0.3<br>
 * 2002-10-03<br>
 * changes ---<br>
 *
 * 2001-09-31: mwm : cleaned up the coding style errors, modified the
 * addTrapMessageVariable(String value) to addTrapMessageVariable(String value,
 * String applicationTrapOIDValue) so that the Facade is more flexible.<br>
 *
 * 2001-10-03: mwm : swapped the SNMP library out, replacing the AdventNet
 * commercial library with the one from OpenNMS.  Thanks to <a href="mailto:JZhao@Qcorps.com">
 * Jin Zhao</a> for pointing me towards this!<br>
 *
 * 2001-11-04: mwm : fixed a minor bug with use of the SnmpTrapSession object.  General tidying up.<br>
 *
 * 2002-10-03: mwm : changed the name of the class to "JoeSNMPTrapSender", to reflect the fact that
 * this is now simply the concrete implementation of the new "SnmpTrapSenderFacade" interface that
 * uses JoeSNMP as it's underlying library.  Made changes to deal with the
 * new architecture.<br>
 *
 * 2002-10-15: mwm : fixed a bug that caused some NMS software to receive a the IP address of the
 * sending host incorrectly formatted.<br>
 *
 * 2002-10-15: mwm : changed the sysUpTime value to a long, to cope with the SysUpTimeResolver mechanism.<br>
 *
 * 2002-12-10: mwm : minor tweaks and prettying up of code.<br>
 *
 * 2003-03-21: mwm : fixed a big, nasty, RTFM bug in #sendTrap<br>
 *
 * 2003-05-24: mwm : minor changes to accomodate the changes in the SnmpTrapSenderFacade interface.<br>
 *
 * @author Mark Masterson (<a href="mailto:m.masterson@computer.org">m.masterson@computer.org</a>)<br>
 */
public class JoeSNMPTrapSender2 implements SnmpTrapHandler, SnmpTrapSenderFacade {

    private String managementHost = "127.0.0.1";
    private int managementHostTrapListenPort = 162;
    private String enterpriseOID = "1.3.6.1.2.1.1.2.0";
    private String localIPAddress = "127.0.0.1";
    private int localTrapSendPort = 161;
    private int genericTrapType;
    private int specificTrapType = 6;
    private String applicationTrapOID = "1.3.6.1.2.1.1.2.0.0.0.0";
    private String communityString = "public";
    private long sysUpTime;
    private SnmpPduTrap pdu;
    private SnmpTrapSession session;
    private boolean isInitialized;
    private int trapVersion = 1;

    /**
     * Default constructor.
     */
    public JoeSNMPTrapSender2() {
    }

    /**
     * Skeleton method, implemented only to satisfy the requirements of the JoeSNMP API.  Does nothing except spit out
     * an error message via StatusLogger.getLogger().
     */
    public void snmpReceivedTrap(final SnmpTrapSession parm1,
                                 final InetAddress parm2,
                                 final int parm3,
                                 final SnmpOctetString parm4,
                                 final SnmpPduPacket parm5) {
        StatusLogger.getLogger().error("This appender does not support receiving traps",
                     new java.lang.UnsupportedOperationException("Method snmpReceivedTrap() not implemented."));
    }

    /**
     * Skeleton method, implemented only to satisfy the requirements of the JoeSNMP API.  Does nothing except spit out
     * an error message via StatusLogger.getLogger().
     */
    public void snmpReceivedTrap(final SnmpTrapSession parm1,
                                 final InetAddress parm2,
                                 final int parm3,
                                 final SnmpOctetString parm4,
                                 final SnmpPduTrap parm5) {
        StatusLogger.getLogger().error("This appender does not support receiving traps",
                     new java.lang.UnsupportedOperationException("Method snmpReceivedTrap() not implemented."));
    }

    /**
     * Skeleton method, implemented only to satisfy the requirements of the JoeSNMP API.  Does nothing except spit out
     * an error message via StatusLogger.getLogger().
     */
    public void snmpTrapSessionError(final SnmpTrapSession parm1,
                                     final int parm2,
                                     final Object parm3) {
        StatusLogger.getLogger().error("There was a fatal error at the SNMP session layer.");
    }

    public void initialize(final SNMPTrapAppender appender) {
        managementHost = appender.getManagementHost();
        managementHostTrapListenPort = appender.getManagementHostTrapListenPort();
        enterpriseOID = appender.getEnterpriseOID();
        localIPAddress = appender.getLocalIPAddress();
        localTrapSendPort = appender.getLocalTrapSendPort();
        communityString = appender.getCommunityString();
        sysUpTime = appender.getSysUpTime();
        genericTrapType = appender.getGenericTrapType();
        specificTrapType = appender.getSpecificTrapType();
        trapVersion = appender.getTrapVersion();
        pdu = new SnmpPduTrap();
        isInitialized = true;
    }
    
    @Override
	public void initialize2(SNMPTrapAppender2 appender) {
		managementHost = appender.getManagementHost();
        managementHostTrapListenPort = appender.getManagementHostTrapListenPort();
        enterpriseOID = appender.getEnterpriseOID();
        localIPAddress = appender.getLocalIPAddress();
        localTrapSendPort = appender.getLocalTrapSendPort();
        communityString = appender.getCommunityString();
        sysUpTime = appender.getSysUpTime();
        genericTrapType = appender.getGenericTrapType();
        specificTrapType = appender.getSpecificTrapType();
        trapVersion = appender.getTrapVersion();
        pdu = new SnmpPduTrap();
        isInitialized = true;
		
	}

    public void addTrapMessageVariable(final String applicationTrapOIDValue,
                                       final String value) {
        //check pre-condition
        if (!isInitialized) {
            StatusLogger.getLogger().error("The initialize() method must be called before calling addTrapMessageVariable()");
            return;
        }
        // add OID
        if (applicationTrapOIDValue != null) {
            applicationTrapOID = applicationTrapOIDValue;
        }
        final SnmpObjectId oid = new SnmpObjectId(applicationTrapOID);
        // set the type
        final SnmpOctetString msg = new SnmpOctetString();
        
        String formattedValue = value.replaceAll("\t", " ");
        formattedValue = formattedValue.replaceAll("\\<.*?>","");
        
        msg.setString(formattedValue);
        // create SnmpVar instance for the value and the type
        try {
            //create varbind
            final SnmpVarBind varbind = new SnmpVarBind(oid, msg);
            // add variable binding
            pdu.addVarBind(varbind);
        } catch (Exception e) {
            StatusLogger.getLogger().error(new StringBuffer().append("Unexpected error creating SNMP bind variable: ")
                    .append(oid)
                    .append(" with value: ")
                    .append(value).toString(), e);
        }
    }

    public void sendTrap() {
        //check pre-condition
        if (!isInitialized) {
            StatusLogger.getLogger().error("The initialize() method must be called before calling sendTrap()");
            return;
        }
        //open the session, set the PDU's values and send the packet
        try {
            session = new SnmpTrapSession(this, localTrapSendPort);
            final SnmpPeer peer = new SnmpPeer(InetAddress.getByName(managementHost));
            peer.setPort(managementHostTrapListenPort);
            final SnmpParameters snmpParms = new SnmpParameters();
            snmpParms.setReadCommunity(communityString);
            if (2 == trapVersion) {
                snmpParms.setVersion(SnmpSMI.SNMPV2);
            } else {
                snmpParms.setVersion(SnmpSMI.SNMPV1);
            }
            peer.setParameters(snmpParms);
            if (null != pdu) {
                pdu.setEnterprise(enterpriseOID);
                final SnmpOctetString addr = new SnmpOctetString();
                addr.setString(InetAddress.getByName(localIPAddress).getAddress());
                final SnmpIPAddress ipAddr = new SnmpIPAddress(addr);
                pdu.setAgentAddress(ipAddr);
                pdu.setGeneric(genericTrapType);
                pdu.setSpecific(specificTrapType);
                pdu.setTimeStamp(sysUpTime);
                if (0 < pdu.getLength()) {
                    session.send(peer, pdu);
                }
            }
        } catch (SnmpPduEncodingException ex) {
            StatusLogger.getLogger().error("There were problems with the SNMP parameters -- could not create and send trap", ex);
        } catch (Exception e) {
            StatusLogger.getLogger().error("There was an unexpected error", e);
        } finally {
            //this is running on a seperate thread, so make sure it gets
            //cleaned up...
            if (null != session && !session.isClosed()) session.close();
        }
    }

	
}