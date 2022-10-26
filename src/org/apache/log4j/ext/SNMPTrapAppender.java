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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An appender to send formatted logging event strings to a specified managment
 * host (typically, a MLM of some sort, but could also be an SNMP management
 * console) in the form of an SNMP trap.
 * <p>
 * This appender does not attempt to provide full access to the SNMP API.  In
 * particular, use of this appender does not make an SNMP agent out of the
 * calling application.  You cannot use this appender as an interface to do SNMP
 * GET or SET calls  -- all it does is pass on your logging event as a TRAP.
 * <p>
 * This appender uses a helper class which serves as the interface to the SNMP
 * sub-system.  This class must implement the SnmpTrapSenderFacade interface.  The
 * concrete implementation class you want to use must be specified in your properties
 * file -- see the examples below.  The implementation class must provide a
 * parameterless constructor -- this is the constructor that Log4J will use to
 * instantiate the class using the class name in the properties file.<br>
 * <br>
 * There are three implementation classes provided with this appender; the JoeSNMPTrapSender,
 * the WengsoftSNMPTrapSender and the NetSnmpCommandLineTrapSender classes.
 * If you fail to specify an implementation in your properties file, or if there
 * is a problem finding, loading or instantiating the implementation that you do specify,
 * the appender will fall back to using the JoeSNMPTrapSender implementation as a default.<p>
 *
 * You can and should, as your needs dictate, write your own implementation of
 * SnmpTrapSenderFacade, using the underlying SNMP library of your choice.  In this
 * case, the implementations provided with the appender should serve as adequate
 * examples for how this might be done.<p>
 *
 * Here's a sample of what you would need in an XML configuration file to configure
 * this appender:<br>
 * <xmp>
 *    <appender name="TRAP_LOG" class="org.apache.log4j.ext.SNMPTrapAppender">
 *        <param name="ImplementationClassName" value="org.apache.log4j.ext.JoeSNMPTrapSender"/>
 *        <param name="ManagementHost" value="127.0.0.1"/>
 *        <param name="ManagementHostTrapListenPort" value="162"/>
 *        <param name="EnterpriseOID" value="1.3.6.1.4.1.24.0"/>
 *        <param name="LocalIPAddress" value="127.0.0.1"/>
 *        <param name="LocalTrapSendPort" value="161"/>
 *        <param name="GenericTrapType" value="6"/>
 *        <param name="SpecificTrapType" value="12345678"/>
 *        <param name="CommunityString" value="public"/>
 *        <param name="ForwardStackTraceWithTrap" value="true"/>
 *        <param name="Threshold" value="DEBUG"/>
 *        <param name="ApplicationTrapOID" value="1.3.6.1.4.1.24.12.10.22.64"/>
 *        <layout class="org.apache.log4j.PatternLayout">
 *                <param name="ConversionPattern" value="%d,%p,[%t],[%c],%m%n"/>
 *        </layout>
 *    </appender>
 * </xmp>
 * <br>
 * To configure an otherwise identical appender that uses a different implementation
 * class, you would simply change the value of the "ImplementationClassName", as
 * in the following example:<br>
 * <xmp>
 *    <appender name="TRAP_LOG" class="org.apache.log4j.ext.SNMPTrapAppender">
 *        <param name="ImplementationClassName" value="org.apache.log4j.ext.WengsoftSNMPTrapSender"/>
 *        <param name="ManagementHost" value="127.0.0.1"/>
 *        <param name="ManagementHostTrapListenPort" value="162"/>
 *        <param name="EnterpriseOID" value="1.3.6.1.4.1.24.0"/>
 *        <param name="LocalIPAddress" value="127.0.0.1"/>
 *        <param name="LocalTrapSendPort" value="161"/>
 *        <param name="GenericTrapType" value="6"/>
 *        <param name="SpecificTrapType" value="12345678"/>
 *        <param name="CommunityString" value="public"/>
 *        <param name="ForwardStackTraceWithTrap" value="true"/>
 *        <param name="Threshold" value="DEBUG"/>
 *        <param name="ApplicationTrapOID" value="1.3.6.1.4.1.24.12.10.22.64"/>
 *        <layout class="org.apache.log4j.PatternLayout">
 *                <param name="ConversionPattern" value="%d,%p,[%t],[%c],%m%n"/>
 *        </layout>
 *    </appender>
 * </xmp>
 * <br>
 * To configure the appender to use a delimited conversion pattern, to allow multiple VarBinds, you would
 * drop the parameter <xmp><param name="ApplicationTrapOID" value="1.3.6.1.4.1.24.12.10.22.64"/></xmp> from the
 * configuration, and change the Layout class to {@link org.apache.log4j.ext.SnmpDelimitedConversionPatternLayout}.
 * You would then need to set the appropriate parameters of the Layout class.  See
 * {@link SnmpDelimitedConversionPatternLayout} for an explanation of using multiple VarBinds.<br>
 * <xmp>
 *    <appender name="TRAP_LOG" class="org.apache.log4j.ext.SNMPTrapAppender">
 *        <param name="ImplementationClassName" value="org.apache.log4j.ext.WengsoftSNMPTrapSender"/>
 *        <param name="ManagementHost" value="127.0.0.1"/>
 *        <param name="ManagementHostTrapListenPort" value="162"/>
 *        <param name="EnterpriseOID" value="1.3.6.1.4.1.24.0"/>
 *        <param name="LocalIPAddress" value="127.0.0.1"/>
 *        <param name="LocalTrapSendPort" value="161"/>
 *        <param name="GenericTrapType" value="6"/>
 *        <param name="SpecificTrapType" value="12345678"/>
 *        <param name="CommunityString" value="public"/>
 *        <param name="ForwardStackTraceWithTrap" value="true"/>
 *        <param name="Threshold" value="DEBUG"/>
 *        <layout class="org.apache.log4j.ext.SnmpDelimitedConversionPatternLayout">
 *              <param name="ValuePairDelim" value="/"/>
 *              <param name="VarDelim" value=";"/>
 *              <param name="ConversionPattern" value="%p;1.3.6.1.4.1.24.100.1/%m;1.3.6.1.4.1.24.100.2/%C{1};1.3.6.1.4.1.24.100.3" />
 *        </layout>
 *    </appender>
 * </xmp>
 * <p>
 * Here's a sample of what you would need in a properties configuration file to
 * configure this appender:
 * <br><br>
 *           log4j.appender.TRAP_LOG=org.apache.log4j.ext.SNMPTrapAppender<br>
 *           log4j.appender.TRAP_LOG.ImplementationClassName=org.apache.log4j.ext.JoeSNMPTrapSender<br>
 *           log4j.appender.TRAP_LOG.ManagementHost=127.0.0.1<br>
 *           log4j.appender.TRAP_LOG.ManagementHostTrapListenPort=162<br>
 *           log4j.appender.TRAP_LOG.EnterpriseOID=1.3.6.1.4.1.24.0<br>
 *           log4j.appender.TRAP_LOG.LocalIPAddress=127.0.0.1<br>
 *           log4j.appender.TRAP_LOG.LocalTrapSendPort=161<br>
 *           log4j.appender.TRAP_LOG.GenericTrapType=6<br>
 *           log4j.appender.TRAP_LOG.SpecificTrapType=12345678<br>
 *           log4j.appender.TRAP_LOG.ApplicationTrapOID=1.3.6.1.4.1.24.12.10.22.64<br>
 *           log4j.appender.TRAP_LOG.CommunityString=public<br>
 *           log4j.appender.TRAP_LOG.ForwardStackTraceWithTrap=true<br>
 *           log4j.appender.TRAP_LOG.Threshold=DEBUG<br>
 *           log4j.appender.TRAP_LOG.layout=org.apache.log4j.PatternLayout<br>
 *           log4j.appender.TRAP_LOG.layout.ConversionPattern=%d,%p,[%t],[%c],%m%n<br>
 * <br>
 * Here's an example using the properties file format the uses the delimited conversion pattern
 * technique to allow multiple VarBinds:
 * <br><br>
 *           log4j.appender.TRAP_LOG=org.apache.log4j.ext.SNMPTrapAppender<br>
 *           log4j.appender.TRAP_LOG.ImplementationClassName=org.apache.log4j.ext.JoeSNMPTrapSender<br>
 *           log4j.appender.TRAP_LOG.ManagementHost=127.0.0.1<br>
 *           log4j.appender.TRAP_LOG.ManagementHostTrapListenPort=162<br>
 *           log4j.appender.TRAP_LOG.EnterpriseOID=1.3.6.1.4.1.24.0<br>
 *           log4j.appender.TRAP_LOG.LocalIPAddress=127.0.0.1<br>
 *           log4j.appender.TRAP_LOG.LocalTrapSendPort=161<br>
 *           log4j.appender.TRAP_LOG.GenericTrapType=6<br>
 *           log4j.appender.TRAP_LOG.SpecificTrapType=12345678<br>
 *           log4j.appender.TRAP_LOG.CommunityString=public<br>
 *           log4j.appender.TRAP_LOG.ForwardStackTraceWithTrap=true<br>
 *           log4j.appender.TRAP_LOG.Threshold=DEBUG<br>
 *           log4j.appender.TRAP_LOG.layout=org.apache.log4j.ext.SnmpDelimitedConversionPatternLayout<br>
 *           log4j.appender.TRAP_LOG.layout.ValuePairDelim=/<br>
 *           log4j.appender.TRAP_LOG.layout.VarDelim=;<br>
 *           log4j.appender.TRAP_LOG.layout.ConversionPattern=
 *                  %p;1.3.6.1.4.1.24.100.1/%m;1.3.6.1.4.1.24.100.2/%C{1};1.3.6.1.4.1.24.100.3<br>
 * <p>
 * <b>This software is based on the log4j software provided by the Apache Jakrta log4j project.  This
 * software is released under a version of the Apache license version 1.1 -- please see the LICENSE.TXT
 * file included with this distribution for details.</b>
 * <p>
 * Version 1.2.9.1<br>
 * 2001-09-29<br>
 * changes ---
 *
 * 2001-10-03: mwm : made changes needed to support v.1.1.1 of the SNMPTrapSenderFacade<br>
 *
 * 2002-09-02: mwm : changed to be compatible with Log4J v. 1.2.x<br>
 *
 * 2002-10-03: mwm : Made changes, mainly in #append, to deal with the
 * new SnmpTrapSenderFacade interface architecture.<br>
 *
 * 2002-10-15: mwm: Included the SysUpTimeResolver contributed by Thomas Muller.<br>
 *
 * 2002-10-15: mwm : changed the sysUpTime value to a long, to cope with the SysUpTimeResolver mechanism.<br>
 *
 * 2002-12-10: mwm : minor tweaks and prettying up of code.<br>
 *
 * 2003-03-21: mwm : incorporated the first cut of Matt Monks's code to use a delimited conversion pattern
 * string to allow mutliple VarBinds to be attached to the trap PDU.<br>
 *
 * 2003-03-22: mwm : after reading Ceki's fine new book on Log4J, made several changes (for example, to
 * the implementation of #close) to correct deficiencies in this class as an implementation of
 * AppenderSkeleton.  Also improved/added handling of the stack trace of the Throwable associated with
 * the LoggingEvent, again inspired by Ceki's example in the new book.<br>
 *
 * 2003-03-23: mwm : building on Matt Monks's ideas, added the SnmpDelimitedConversionPatternLayout, and used
 * it to refactor the handling of the delimited conversion pattern case.<br>
 *
 * 2003-05-24: mwm : minor changes to accomodate the change in the SnmpTrapSenderFacade interface, and added
 * two new properties.<br>
 *
 * 2003-07-05: mwm : some improvement in the exception handling of #loadImplementationClass<br>
 *
 * @author Mark Masterson (<a href="mailto:m.masterson@computer.org">m.masterson@computer.org</a>),
 * @author <br>Thomas Muller (<a href="mailto:ttm@online.no">ttm@online.no</a>)
 * @author <br>Matt Monks (<a href="mailto:Matthew.Monks@netdecisions.com">Matthew.Monks@netdecisions.com</a>)
 */

public class SNMPTrapAppender extends AppenderSkeleton {

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final SysUpTimeResolver DEFAULT_SYSUP_TIME_RESOLVER = new SysUpTimeResolver() {
        private final long appenderLoadedTime = System.currentTimeMillis();

        public long getSysUpTime() {
            return System.currentTimeMillis() - appenderLoadedTime;
        }
    };

    private String managementHost = "127.0.0.1";
    private int managementHostTrapListenPort = 162;
    private String enterpriseOID = "1.3.6.1.2.1.2.0";
    private String localIPAddress = "127.0.0.1";
    private int localTrapSendPort = 161;
    private int genericTrapType = 6;
    private int specificTrapType = 1;
    private String applicationTrapOID = "1.3.6.1.2.1.2.0.0.0.0";
    private String communityString = "public";
    private long sysUpTime;
    private String implementationClassName;
    private SysUpTimeResolver sysUpTimeResolver = DEFAULT_SYSUP_TIME_RESOLVER;
    private String forwardStackTraceWithTrap = FALSE;
    private int trapVersion = 1;

    /**
     * Default constructor.
     */
    public SNMPTrapAppender() {
        //default c-tor
    }

    /**
     * Construct the appender with the specified Layout.
     * @param layoutValue a Layout
     */
    public SNMPTrapAppender(final Layout layoutValue) {
        super.layout = layoutValue;
    }

    /**
     * Implemented to return "true" .
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Uses an instance of {@link SnmpTrapSenderFacade} to send the String returned by Layout.format() as the message(s)
     * of an SNMP trap. If the various properties have not been intialized, the defaults will be used.
     */
    protected void append(final LoggingEvent event) {
        //check pre-conditions
        if (!isAsSevereAsThreshold(event.getLevel())) return;
        if (null == getLayout()) {
            errorHandler.error(new StringBuffer().append("No layout set for the Appender named [")
                    .append(getName())
                    .append(']').toString(),
                               null,
                               ErrorCode.MISSING_LAYOUT);
            return;
        }
        //Create and intialize the interface to SNMP -- will
        //use default values if none have been provided, which will,
        //in most cases, result in the trap being sent to dev(null)...
        final SnmpTrapSenderFacade out = loadImplementationClass();
        if (null != out) {
            if (0 == sysUpTime) sysUpTime = sysUpTimeResolver.getSysUpTime();
            out.initialize(this);
            parseLoggingEventAndAddToTrap(event, out);
            //fire it off
            out.sendTrap();
        }
    }

    /**
     * Load the concrete class specifed in the properties/config file that implements the SnmpTrapSenderFacade
     * interface.  Logs an error using the ErrorHandler if there are problems, and returns null.
     *
     * @return an instance of an implementation of SnmpTrapSenderFacade or null if there was an Exception
     */
    private SnmpTrapSenderFacade loadImplementationClass() {
        SnmpTrapSenderFacade result = null;
        try {
            result = (SnmpTrapSenderFacade)
                    OptionConverter.instantiateByClassName(
                            implementationClassName,
                            Class.forName(implementationClassName),
                            null);
        } catch (Exception ex) {
            errorHandler.error(new StringBuffer().append("Could not locate the implementation class - ")
                    .append(implementationClassName).toString(),
                               ex,
                               ErrorCode.GENERIC_FAILURE);
        }
        return result;
    }

    /**
     * Get the formatted logging event, and bind it to the SNMP PDU as a Varbind, with the applicationTrapOID as the
     * name, and the logging event string as the value...
     *
     * @param event to log
     * @param out logging target
     */
    private void parseLoggingEventAndAddToTrap(final LoggingEvent event,
                                               final SnmpTrapSenderFacade out) {
        final PatternLayout pl = (PatternLayout) getLayout();
        if (pl instanceof SnmpDelimitedConversionPatternLayout)
            ((SnmpDelimitedConversionPatternLayout) pl).formatMultipleVarBinds(
                    event, out);
        else
            out.addTrapMessageVariable(applicationTrapOID,
                                       pl.format(event));
        handleThrowable(event, out);
    }

    /**
     * If the Layout associated with this appender does not parse Throwables, then this appender may do so.  If the
     * parameter "ForwardStackTraceWithTrap" is set to "true" in the configuration script, each element of the stack
     * trace of the Throwable will be added as a separate VarBind to the trap PDU.
     *
     * @param event to log
     * @param out logging target
     */
    private void handleThrowable(final LoggingEvent event,
                                 final SnmpTrapSenderFacade out) {
        if (getLayout().ignoresThrowable()
            && TRUE.equals(getForwardStackTraceWithTrap())) {
            final String[] stackTrace = event.getThrowableStrRep();
            if (null != stackTrace) {
                for (int i = 0; i < stackTrace.length; i++) {
                    out.addTrapMessageVariable(applicationTrapOID,
                                               stackTrace[i]);
                }
            }
        }
    }

    /**
     * Tests if the String parameter is #equalsIgnoreCase to one of the constants TRUE or FALSE defined for this class.
     *
     * @param value to be tested for equivalency
     *
     * @return true if the parameter matches either of the two constants, otherwise false.
     */
    private static boolean testStringForBooleanEquivalency(final String value) {
        return TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value);
    }

    /**
     * Sets the state of the Appender to "closed".
     */
    public void close() {
        if (!closed) closed = true;
    }

    /**
     * Get the numeric, dotted-decimal IP address of the remote host that traps will be sent to, as a String.
     * @return numeric IP address of the trap target
     */
    public String getManagementHost() {
        return managementHost;
    }

    /**
     * Set the IP address of the remote host that traps should be sent to.
     *
     * @param managementHostValue -- the IP address of the remote host, in numeric, dotted-decimal format, as a String.
     *                            E.g. "10.255.255.1"
     */
    public void setManagementHost(final String managementHostValue) {
        managementHost = managementHostValue;
    }

    /**
     * Get the port used on the remote host to listen for SNMP traps.  The standard is 162.
     * @return target trap port
     */
    public int getManagementHostTrapListenPort() {
        return managementHostTrapListenPort;
    }

    /**
     * Set the port used on the remote host to listen for SNMP traps.  The standard is 162.
     *
     * @param managementHostTrapListenPortValue
     *         -- any valid TCP/IP port
     */
    public void setManagementHostTrapListenPort(
            final int managementHostTrapListenPortValue) {
        managementHostTrapListenPort = managementHostTrapListenPortValue;
    }

    /**
     * Get the enterprise OID that will be sent in the SNMP PDU.
     *
     * @return A String, formatted as an OID E.g. "1.3.6.1.2.1.1.2.0" -- this OID would point to the standard
     *         sysObjectID of the "systemName" node of the standard "system" MIB.
     */
    public String getEnterpriseOID() {
        return enterpriseOID;
    }

    /**
     * Set the enterprise OID that will be sent in the SNMP PDU.
     *
     * @param enterpriseOIDValue -- formatted as an OID E.g. "1.3.6.1.2.1.1.2.0" -- this OID would point to the standard
     *                           sysObjectID of the "systemName" node of the standard "system" MIB.
     *                           <p/>
     *                           This is the default value, if none is provided.
     *                           <p/>
     *                           If you want(need) to use custom OIDs (such as ones from the "private.enterprises" node
     *                           -- "1.3.6.1.4.1.x.x.x..."), you always need to provide the <b>fully qualified</b> OID
     *                           as the parameter to this method.
     */
    public void setEnterpriseOID(final String enterpriseOIDValue) {
        enterpriseOID = enterpriseOIDValue;
    }

    /**
     * Get the IP address of the host that is using this appender to send SNMP traps.
     * @return IP address of the local host
     */
    public String getLocalIPAddress() {
        return localIPAddress;
    }

    /**
     * Set the IP address of the host that is using this appender to send SNMP traps.  This address will be encoded in
     * the SNMP PDU, and used to provide things like the "agent"'s IP address.
     *
     * @param localIPAddressValue -- an IP address, as a String, in numeric, dotted decimal format.  E.g.
     *                            "10.255.255.2".
     */
    public void setLocalIPAddress(final String localIPAddressValue) {
        localIPAddress = localIPAddressValue;
    }

    /**
     * Get the generic trap type set for this SNMP PDU.
     * @return the trap type currently set
     */
    public int getGenericTrapType() {
        return genericTrapType;
    }

    /**
     * Set the generic trap type for this SNMP PDU.  The allowed values for this attribute are a part of the SNMP
     * standard. To avoid confusing the Log4J framework code that calls this setter as part of the configuration
     * process, I don't enforce the pre-condition (which is "0 >= genericTrapTypeValue <= 6") here.  You can pass in
     * any value you like.  However, a value that is outside of the allowed range will result in a deformed SNMP PDU --
     * such a PDU, in turn, will be silently ignored by most SNMP trap receivers -- IOW, the trap will go to dev>null.
     *
     * @param genericTrapTypeValue -- One of the following values:<p> 0 -- cold start<br> 1 -- warm start<br> 2 -- link
     *                             down<br> 3 -- link up<br> 4 -- authentification failure<br> 5 -- EGP neighbor
     *                             loss<br> 6 -- enterprise specific<br>
     */
    public void setGenericTrapType(final int genericTrapTypeValue) {
        genericTrapType = genericTrapTypeValue;
    }

    /**
     * Get the specific trap type set for this SNMP PDU.
     * @return specific trap type currently set
     */
    public int getSpecificTrapType() {
        return specificTrapType;
    }

    /**
     * Set the specific trap type for this SNMP PDU.  Can be used for application and/or enterprise specific values.
     *
     * @param specificTrapTypeValue -- any value within the range defined for an INTEGER in the ASN.1/BER notation; i.e.
     *                              -128 to 127
     */
    public void setSpecificTrapType(final int specificTrapTypeValue) {
        specificTrapType = specificTrapTypeValue;
    }

    /**
     * Get the trap OID that will be sent in the SNMP PDU for this app.
     * @return application OID currently set
     */
    public String getApplicationTrapOID() {
        return applicationTrapOID;
    }

    /**
     * Set the trap OID that will be sent in the SNMP PDU for this app.
     *
     * @param applicationTrapOIDValue -- formatted as an OID E.g. "1.3.6.1.2.1.2.0.0.0.0" -- this OID would point to the
     *                                standard sysObjectID of the "systemName" node of the standard "system" MIB.
     *                                <p/>
     *                                This is the default value, if none is provided.
     *                                <p/>
     *                                If you want(need) to use custom OIDs (such as ones from the "private.enterprises"
     *                                node -- "1.3.6.1.4.1.x.x.x..."), you always need to provide the <b>fully
     *                                qualified</b> OID as the parameter to this method.
     */
    public void setApplicationTrapOID(final String applicationTrapOIDValue) {
        applicationTrapOID = applicationTrapOIDValue;
    }

    /**
     * Get the community string set for the SNMP session this appender will use.
     * @return the current community string
     */
    public String getCommunityString() {
        return communityString;
    }

    /**
     * Set the community string set for the SNMP session this appender will use.  The community string is used by SNMP
     * (prior to v.3) as a sort of plain-text password.
     *
     * @param communityStringValue -- E.g. "public".  This is the default, if none is provided.
     */
    public void setCommunityString(final String communityStringValue) {
        communityString = communityStringValue;
    }

    /**
     * Get the value of the system up time that will be used for the SNMP PDU.
     * @return current system up time
     */
    public long getSysUpTime() {
        return sysUpTime;
    }

    /**
     * Set the value of the system up time that will be used for the SNMP PDU.
     *
     * @param sysUpTimeValue -- this is meant to be the amount of time, in seconds, elapsed since the last re-start or
     *                       re-initialization of the calling application.  Of course, to set this, your application
     *                       needs to keep track of the value.  The default is 0, if none is provided.
     *
     * @deprecated Now using the excellent SysUpTimeResolver idea from Thomas Muller, but if you set this value in the
     *             properties file, the appender will use that value, to maintain backwards compatibility.
     */
    public void setSysUpTime(final long sysUpTimeValue) {
        sysUpTime = sysUpTimeValue;
    }

    /**
     * Get the value of the port that will be used to send traps out from the local host.
     * @return local trap send port
     */
    public int getLocalTrapSendPort() {
        return localTrapSendPort;
    }

    /**
     * Set the value of the port that will be used to send traps out from the local host.
     *
     * @param localTrapSendPortValue -- any valid IP port number.  The default is 161, if none is provided.
     */
    public void setLocalTrapSendPort(final int localTrapSendPortValue) {
        localTrapSendPort = localTrapSendPortValue;
    }

    /**
     * Get the value of the concrete class that implements the SnmpTrapSenderFacade interface.
     * @return the FQN of the class currently configured as the delegate for sending traps
     */
    public String getImplementationClassName() {
        return implementationClassName;
    }

    /**
     * Set the value of the concrete class that implements the SnmpTrapSenderFacade interface.
     *
     * @param implementationClassNameValue -- a String containing the fully qualified class name of the concrete
     *                                     implementation class, e.g. "org.apache.log4j.ext.JoeSNMPTrapSender".
     */
    public void setImplementationClassName(
            final String implementationClassNameValue) {
        implementationClassName = implementationClassNameValue;
    }

    /**
     * Gets the concrete instance of an implementation of the SysUpTimeResolver interface that is being used by the
     * appender.
     *
     * @return a concrete instance of an implementation of the SysUpTimeResolver interface
     */
    public SysUpTimeResolver getSysUpTimeResolver() {
        return sysUpTimeResolver;
    }

    /**
     * See {@link SysUpTimeResolver}.  This method sets the resolver by passing the FQN of the class that implements the
     * SysUpTimeResolver interface, as a String.
     *
     * @param value -- a String containing the fully qualified class name of the concrete implementation class, e.g.
     *              "org.apache.log4j.ext.MySysUpTimeResolver".
     */
    public void setSysUpTimeResolver(final String value) {
        sysUpTimeResolver = (SysUpTimeResolver) OptionConverter.instantiateByClassName(
                value,
                SysUpTimeResolver.class,
                DEFAULT_SYSUP_TIME_RESOLVER);
    }

    /**
     * Gets the flag that determines if the contents of the stack trace of any Throwable in the LoggingEvent should be
     * added as VarBinds to the trap PDU.<br> Default is FALSE.
     *
     * @return the current value of this flag.
     */
    public String getForwardStackTraceWithTrap() {
        return forwardStackTraceWithTrap;
    }

    /**
     * Sets the flag that determines if the contents of the stack trace of any Throwable in the LoggingEvent should be
     * added as VarBinds to the trap PDU.<br> Default is FALSE.  Allowed values are TRUE and FALSE.
     *
     * @param forwardStackTraceWithTrap true or false
     */
    public void setForwardStackTraceWithTrap(final String forwardStackTraceWithTrap) {
        if (testStringForBooleanEquivalency(forwardStackTraceWithTrap))
            this.forwardStackTraceWithTrap = forwardStackTraceWithTrap;
        else
            throw new IllegalArgumentException(
                    new StringBuffer().append("Value of forwardStackTraceWithTrap must be set to")
                            .append("TRUE or FALSE! Illegal value was:")
                            .append(forwardStackTraceWithTrap).toString());
    }

    public int getTrapVersion() {
        return trapVersion;
    }

    public void setTrapVersion(final int trapVersion) {
        this.trapVersion = trapVersion;
    }
}