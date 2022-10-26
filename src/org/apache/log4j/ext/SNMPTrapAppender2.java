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

import java.io.Serializable;
import java.util.StringTokenizer;

import javax.print.attribute.SetOfIntegerSyntax;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.OptionConverter;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name = "SNMPTrapAppender2", category = Node.CATEGORY, elementType = "appender", printObject = true)
public class SNMPTrapAppender2 extends AbstractAppender {

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
	private boolean snmpConvert = true;
	private String varDelim = ";";
	private String valuePairDelim = "/";

	protected SNMPTrapAppender2(String name, Filter filter, Layout<? extends Serializable> layout,
			String managementHost, String managementHostTrapListenPort, String enterpriseOID, String localIPAddress,
			String localTrapSendPort, String communityString, String genericTrapType, String specificTrapType,
			String applicationTrapOID, String implementationClassName, String forwardStackTraceWithTrap,
			String trapVersion, boolean snmpConvert, String valuePairDelim, String varDelim) {
		super(name, filter, layout);
		try {
			setManagementHost(managementHost);
			setManagementHostTrapListenPort(Integer.parseInt(managementHostTrapListenPort));
			setEnterpriseOID(enterpriseOID);
			setLocalIPAddress(localIPAddress);
			setGenericTrapType(Integer.parseInt(genericTrapType));
			setSpecificTrapType(Integer.parseInt(specificTrapType));
			setApplicationTrapOID(applicationTrapOID);
			setImplementationClassName(implementationClassName);
			setForwardStackTraceWithTrap(forwardStackTraceWithTrap);
			setTrapVersion(Integer.parseInt(trapVersion));
			setLocalTrapSendPort(Integer.parseInt(localTrapSendPort));
			setCommunityString(communityString);
			setSnmpConvert(snmpConvert);
			setValuePairDelim(valuePairDelim);
			setVarDelim(varDelim);

		} catch (Exception e) {
			StatusLogger.getLogger().error("Invalid parameters for appender", e);
		}

	}

	@PluginFactory
	public static SNMPTrapAppender2 createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("ManagementHost") String managementHost,
			@PluginAttribute("ManagementHostTrapListenPort") String managementHostTrapListenPort,
			@PluginAttribute("EnterpriseOID") String enterpriseOID,
			@PluginAttribute("LocalIPAddress") String localIPAddress,
			@PluginAttribute("localTrapSendPort") String localTrapSendPort,
			@PluginAttribute("communityString") String communityString,
			@PluginAttribute("GenericTrapType") String genericTrapType,
			@PluginAttribute("SpecificTrapType") String specificTrapType,
			@PluginAttribute("ApplicationTrapOID") String applicationTrapOID,
			@PluginAttribute("ImplementationClassName") String implementationClassName,
			@PluginAttribute("ForwardStackTraceWithTrap") String forwardStackTraceWithTrap,
			@PluginAttribute("TrapVersion") String trapVersion, @PluginAttribute("SNMPConvert") String snmpConvert,
			@PluginAttribute("ValuePairDelim") String valuePairDelim, @PluginAttribute("VarDelim") String varDelim,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filters") final Filter filter) {
		if (trapVersion == null) {
			trapVersion = "1";
		}

		if (forwardStackTraceWithTrap == null) {
			forwardStackTraceWithTrap = "true";
		}

		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}

		if (snmpConvert == null) {
			snmpConvert = "true";
		}

		if (snmpConvert.equals("true")) {

			if (valuePairDelim == null) {
				valuePairDelim = "/";
			}

			if (varDelim == null) {
				varDelim = ";";
			}
		}

		return new SNMPTrapAppender2(name, filter, layout, managementHost, managementHostTrapListenPort, enterpriseOID,
				localIPAddress, localTrapSendPort, communityString, genericTrapType, specificTrapType,
				applicationTrapOID, implementationClassName, forwardStackTraceWithTrap, trapVersion,
				(snmpConvert.equals("true")) ? true : false, valuePairDelim, varDelim);
	}

	@Override
	public void append(LogEvent event) {
		// check pre-conditions
		if (null == getLayout()) {
			error(new StringBuffer().append("No layout set for the Appender named [").append(getName()).append(']')
					.toString(), event, null);
			return;
		}
		// Create and intialize the interface to SNMP -- will
		// use default values if none have been provided, which will,
		// in most cases, result in the trap being sent to dev(null)...
		final SnmpTrapSenderFacade out = loadImplementationClass();
		if (null != out) {
			if (0 == sysUpTime)
				sysUpTime = sysUpTimeResolver.getSysUpTime();
			out.initialize2(this);
			parseLoggingEventAndAddToTrap(event, out);
			// fire it off
			out.sendTrap();
		}
	}

	/**
	 * Load the concrete class specifed in the properties/config file that
	 * implements the SnmpTrapSenderFacade interface. Logs an error using the
	 * ErrorHandler if there are problems, and returns null.
	 *
	 * @return an instance of an implementation of SnmpTrapSenderFacade or null
	 *         if there was an Exception
	 */
	private SnmpTrapSenderFacade loadImplementationClass() {
		SnmpTrapSenderFacade result = null;
		try {
			result = (SnmpTrapSenderFacade) OptionConverter.instantiateByClassName(implementationClassName,
					Class.forName(implementationClassName), null);
		} catch (Exception ex) {
			error(new StringBuffer().append("Could not locate the implementation class - ")
					.append(implementationClassName).toString(), null, ex);
		}
		return result;
	}

	/**
	 * Get the formatted logging event, and bind it to the SNMP PDU as a
	 * Varbind, with the applicationTrapOID as the name, and the logging event
	 * string as the value...
	 *
	 * @param event
	 *            to log
	 * @param out
	 *            logging target
	 */
	private void parseLoggingEventAndAddToTrap(final LogEvent event, final SnmpTrapSenderFacade out) {
		PatternLayout pl = (PatternLayout) getLayout();
		if (snmpConvert) {
			final String pattern = pl.getConversionPattern();
			final StringTokenizer splitter = new StringTokenizer(pattern, this.getValuePairDelim());
			while (splitter.hasMoreTokens()) {
				final String variable = splitter.nextToken();
				final StringTokenizer varSplitter = new StringTokenizer(variable, this.getVarDelim());
				final PatternLayout layout = PatternLayout.newBuilder().withPattern(varSplitter.nextToken())
						.withAlwaysWriteExceptions(!ignoreExceptions()).build();
				final String parsedResult = layout.toSerializable(event);
				out.addTrapMessageVariable(varSplitter.nextToken(), parsedResult);
			}
		} else {
			out.addTrapMessageVariable(applicationTrapOID, pl.toSerializable(event));
		}
		handleThrowable(event, out);
	}

	/**
	 * If the Layout associated with this appender does not parse Throwables,
	 * then this appender may do so. If the parameter
	 * "ForwardStackTraceWithTrap" is set to "true" in the configuration script,
	 * each element of the stack trace of the Throwable will be added as a
	 * separate VarBind to the trap PDU.
	 *
	 * @param event
	 *            to log
	 * @param out
	 *            logging target
	 */
	private void handleThrowable(final LogEvent event, final SnmpTrapSenderFacade out) {
		if (ignoreExceptions() && TRUE.equals(getForwardStackTraceWithTrap())) {
			final Throwable thrownObj = event.getThrown();

			if (thrownObj != null) {
				final StackTraceElement[] stackTrace = thrownObj.getStackTrace();
				if (null != stackTrace) {
					for (int i = 0; i < stackTrace.length; i++) {
						out.addTrapMessageVariable(applicationTrapOID, stackTrace[i].toString());
					}
				}
			}
		}
	}

	/**
	 * Tests if the String parameter is #equalsIgnoreCase to one of the
	 * constants TRUE or FALSE defined for this class.
	 *
	 * @param value
	 *            to be tested for equivalency
	 *
	 * @return true if the parameter matches either of the two constants,
	 *         otherwise false.
	 */
	private static boolean testStringForBooleanEquivalency(final String value) {
		return TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value);
	}

	/**
	 * Sets the state of the Appender to "closed".
	 */
	/*
	 * public void close() { if (!closed) closed = true; }
	 */

	/**
	 * Get the numeric, dotted-decimal IP address of the remote host that traps
	 * will be sent to, as a String.
	 * 
	 * @return numeric IP address of the trap target
	 */
	public String getManagementHost() {
		return managementHost;
	}

	/**
	 * Set the IP address of the remote host that traps should be sent to.
	 *
	 * @param managementHostValue
	 *            -- the IP address of the remote host, in numeric,
	 *            dotted-decimal format, as a String. E.g. "10.255.255.1"
	 */
	public void setManagementHost(final String managementHostValue) {
		managementHost = managementHostValue;
	}

	/**
	 * Get the port used on the remote host to listen for SNMP traps. The
	 * standard is 162.
	 * 
	 * @return target trap port
	 */
	public int getManagementHostTrapListenPort() {
		return managementHostTrapListenPort;
	}

	/**
	 * Set the port used on the remote host to listen for SNMP traps. The
	 * standard is 162.
	 *
	 * @param managementHostTrapListenPortValue
	 *            -- any valid TCP/IP port
	 */
	public void setManagementHostTrapListenPort(final int managementHostTrapListenPortValue) {
		managementHostTrapListenPort = managementHostTrapListenPortValue;
	}

	/**
	 * Get the enterprise OID that will be sent in the SNMP PDU.
	 *
	 * @return A String, formatted as an OID E.g. "1.3.6.1.2.1.1.2.0" -- this
	 *         OID would point to the standard sysObjectID of the "systemName"
	 *         node of the standard "system" MIB.
	 */
	public String getEnterpriseOID() {
		return enterpriseOID;
	}

	/**
	 * Set the enterprise OID that will be sent in the SNMP PDU.
	 *
	 * @param enterpriseOIDValue
	 *            -- formatted as an OID E.g. "1.3.6.1.2.1.1.2.0" -- this OID
	 *            would point to the standard sysObjectID of the "systemName"
	 *            node of the standard "system" MIB.
	 *            <p/>
	 *            This is the default value, if none is provided.
	 *            <p/>
	 *            If you want(need) to use custom OIDs (such as ones from the
	 *            "private.enterprises" node -- "1.3.6.1.4.1.x.x.x..."), you
	 *            always need to provide the <b>fully qualified</b> OID as the
	 *            parameter to this method.
	 */
	public void setEnterpriseOID(final String enterpriseOIDValue) {
		enterpriseOID = enterpriseOIDValue;
	}

	/**
	 * Get the IP address of the host that is using this appender to send SNMP
	 * traps.
	 * 
	 * @return IP address of the local host
	 */
	public String getLocalIPAddress() {
		return localIPAddress;
	}

	/**
	 * Set the IP address of the host that is using this appender to send SNMP
	 * traps. This address will be encoded in the SNMP PDU, and used to provide
	 * things like the "agent"'s IP address.
	 *
	 * @param localIPAddressValue
	 *            -- an IP address, as a String, in numeric, dotted decimal
	 *            format. E.g. "10.255.255.2".
	 */
	public void setLocalIPAddress(final String localIPAddressValue) {
		localIPAddress = localIPAddressValue;
	}

	/**
	 * Get the generic trap type set for this SNMP PDU.
	 * 
	 * @return the trap type currently set
	 */
	public int getGenericTrapType() {
		return genericTrapType;
	}

	/**
	 * Set the generic trap type for this SNMP PDU. The allowed values for this
	 * attribute are a part of the SNMP standard. To avoid confusing the Log4J
	 * framework code that calls this setter as part of the configuration
	 * process, I don't enforce the pre-condition (which is "0 >=
	 * genericTrapTypeValue <= 6") here. You can pass in any value you like.
	 * However, a value that is outside of the allowed range will result in a
	 * deformed SNMP PDU -- such a PDU, in turn, will be silently ignored by
	 * most SNMP trap receivers -- IOW, the trap will go to dev>null.
	 *
	 * @param genericTrapTypeValue
	 *            -- One of the following values:
	 *            <p>
	 *            0 -- cold start<br>
	 *            1 -- warm start<br>
	 *            2 -- link down<br>
	 *            3 -- link up<br>
	 *            4 -- authentification failure<br>
	 *            5 -- EGP neighbor loss<br>
	 *            6 -- enterprise specific<br>
	 */
	public void setGenericTrapType(final int genericTrapTypeValue) {
		genericTrapType = genericTrapTypeValue;
	}

	/**
	 * Get the specific trap type set for this SNMP PDU.
	 * 
	 * @return specific trap type currently set
	 */
	public int getSpecificTrapType() {
		return specificTrapType;
	}

	/**
	 * Set the specific trap type for this SNMP PDU. Can be used for application
	 * and/or enterprise specific values.
	 *
	 * @param specificTrapTypeValue
	 *            -- any value within the range defined for an INTEGER in the
	 *            ASN.1/BER notation; i.e. -128 to 127
	 */
	public void setSpecificTrapType(final int specificTrapTypeValue) {
		specificTrapType = specificTrapTypeValue;
	}

	/**
	 * Get the trap OID that will be sent in the SNMP PDU for this app.
	 * 
	 * @return application OID currently set
	 */
	public String getApplicationTrapOID() {
		return applicationTrapOID;
	}

	/**
	 * Set the trap OID that will be sent in the SNMP PDU for this app.
	 *
	 * @param applicationTrapOIDValue
	 *            -- formatted as an OID E.g. "1.3.6.1.2.1.2.0.0.0.0" -- this
	 *            OID would point to the standard sysObjectID of the
	 *            "systemName" node of the standard "system" MIB.
	 *            <p/>
	 *            This is the default value, if none is provided.
	 *            <p/>
	 *            If you want(need) to use custom OIDs (such as ones from the
	 *            "private.enterprises" node -- "1.3.6.1.4.1.x.x.x..."), you
	 *            always need to provide the <b>fully qualified</b> OID as the
	 *            parameter to this method.
	 */
	public void setApplicationTrapOID(final String applicationTrapOIDValue) {
		applicationTrapOID = applicationTrapOIDValue;
	}

	/**
	 * Get the community string set for the SNMP session this appender will use.
	 * 
	 * @return the current community string
	 */
	public String getCommunityString() {
		return communityString;
	}

	/**
	 * Set the community string set for the SNMP session this appender will use.
	 * The community string is used by SNMP (prior to v.3) as a sort of
	 * plain-text password.
	 *
	 * @param communityStringValue
	 *            -- E.g. "public". This is the default, if none is provided.
	 */
	public void setCommunityString(final String communityStringValue) {
		communityString = communityStringValue;
	}

	/**
	 * Get the value of the system up time that will be used for the SNMP PDU.
	 * 
	 * @return current system up time
	 */
	public long getSysUpTime() {
		return sysUpTime;
	}

	/**
	 * Set the value of the system up time that will be used for the SNMP PDU.
	 *
	 * @param sysUpTimeValue
	 *            -- this is meant to be the amount of time, in seconds, elapsed
	 *            since the last re-start or re-initialization of the calling
	 *            application. Of course, to set this, your application needs to
	 *            keep track of the value. The default is 0, if none is
	 *            provided.
	 *
	 * @deprecated Now using the excellent SysUpTimeResolver idea from Thomas
	 *             Muller, but if you set this value in the properties file, the
	 *             appender will use that value, to maintain backwards
	 *             compatibility.
	 */
	public void setSysUpTime(final long sysUpTimeValue) {
		sysUpTime = sysUpTimeValue;
	}

	/**
	 * Get the value of the port that will be used to send traps out from the
	 * local host.
	 * 
	 * @return local trap send port
	 */
	public int getLocalTrapSendPort() {
		return localTrapSendPort;
	}

	/**
	 * Set the value of the port that will be used to send traps out from the
	 * local host.
	 *
	 * @param localTrapSendPortValue
	 *            -- any valid IP port number. The default is 161, if none is
	 *            provided.
	 */
	public void setLocalTrapSendPort(final int localTrapSendPortValue) {
		localTrapSendPort = localTrapSendPortValue;
	}

	/**
	 * Get the value of the concrete class that implements the
	 * SnmpTrapSenderFacade interface.
	 * 
	 * @return the FQN of the class currently configured as the delegate for
	 *         sending traps
	 */
	public String getImplementationClassName() {
		return implementationClassName;
	}

	/**
	 * Set the value of the concrete class that implements the
	 * SnmpTrapSenderFacade interface.
	 *
	 * @param implementationClassNameValue
	 *            -- a String containing the fully qualified class name of the
	 *            concrete implementation class, e.g.
	 *            "org.apache.log4j.ext.JoeSNMPTrapSender".
	 */
	public void setImplementationClassName(final String implementationClassNameValue) {
		implementationClassName = implementationClassNameValue;
	}

	/**
	 * Gets the concrete instance of an implementation of the SysUpTimeResolver
	 * interface that is being used by the appender.
	 *
	 * @return a concrete instance of an implementation of the SysUpTimeResolver
	 *         interface
	 */
	public SysUpTimeResolver getSysUpTimeResolver() {
		return sysUpTimeResolver;
	}

	/**
	 * See {@link SysUpTimeResolver}. This method sets the resolver by passing
	 * the FQN of the class that implements the SysUpTimeResolver interface, as
	 * a String.
	 *
	 * @param value
	 *            -- a String containing the fully qualified class name of the
	 *            concrete implementation class, e.g.
	 *            "org.apache.log4j.ext.MySysUpTimeResolver".
	 */
	public void setSysUpTimeResolver(final String value) {
		sysUpTimeResolver = (SysUpTimeResolver) OptionConverter.instantiateByClassName(value, SysUpTimeResolver.class,
				DEFAULT_SYSUP_TIME_RESOLVER);
	}

	/**
	 * Gets the flag that determines if the contents of the stack trace of any
	 * Throwable in the LoggingEvent should be added as VarBinds to the trap
	 * PDU.<br>
	 * Default is FALSE.
	 *
	 * @return the current value of this flag.
	 */
	public String getForwardStackTraceWithTrap() {
		return forwardStackTraceWithTrap;
	}

	/**
	 * Sets the flag that determines if the contents of the stack trace of any
	 * Throwable in the LoggingEvent should be added as VarBinds to the trap
	 * PDU.<br>
	 * Default is FALSE. Allowed values are TRUE and FALSE.
	 *
	 * @param forwardStackTraceWithTrap
	 *            true or false
	 */
	public void setForwardStackTraceWithTrap(final String forwardStackTraceWithTrap) {
		if (testStringForBooleanEquivalency(forwardStackTraceWithTrap))
			this.forwardStackTraceWithTrap = forwardStackTraceWithTrap;
		else
			throw new IllegalArgumentException(
					new StringBuffer().append("Value of forwardStackTraceWithTrap must be set to")
							.append("TRUE or FALSE! Illegal value was:").append(forwardStackTraceWithTrap).toString());
	}

	public int getTrapVersion() {
		return trapVersion;
	}

	public void setTrapVersion(final int trapVersion) {
		this.trapVersion = trapVersion;
	}

	public boolean isSnmpConvert() {
		return snmpConvert;
	}

	public void setSnmpConvert(boolean snmpConvert) {
		this.snmpConvert = snmpConvert;
	}

	public String getVarDelim() {
		return varDelim;
	}

	public void setVarDelim(String varDelim) {
		this.varDelim = varDelim;
	}

	public String getValuePairDelim() {
		return valuePairDelim;
	}

	public void setValuePairDelim(String valuePairDelim) {
		this.valuePairDelim = valuePairDelim;
	}

}