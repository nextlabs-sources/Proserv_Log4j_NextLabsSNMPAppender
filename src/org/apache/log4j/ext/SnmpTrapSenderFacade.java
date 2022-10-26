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

/**
 * Title: SnmpTrapSenderFacade<br>
 * Description: A simplified interface to an underlying SNMP API.
 * <p>
 * The intent of this class is both to limit the available interface, and allow
 * the underlying SNMP library to be changed, as desired.<br>
 * 
 * @author Mark Masterson (<a href=
 *         "mailto:m.masterson@computer.org">m.masterson@computer.org</a>)<br>
 *         <a href=
 *         "http://www.m2technologies.net/">http://www.m2technologies.net/</a><br>
 * @version 1.0.2 changes ---<br>
 *
 *          2002-10-15: mwm : changed the sysUpTime value to a long, to cope
 *          with the SysUpTimeResolver mechanism.<br>
 *
 *          2003-05-24: mwm : changed the method signature of the
 *          SnmpTrapSenderFacade interface to use the Appender as a single
 *          parameter object -- this makes the method more flexible against
 *          changes to the set of parameters or interest in the future.<br>
 */

public interface SnmpTrapSenderFacade {

	/**
	 * This method is called to set the values of all of the class fields used
	 * as parameters to the underlying SNMP API. This method <b>must</b> be
	 * called prior to calling either of the other methods in this class.
	 *
	 * @param appender
	 *            - An instance of the SNMPTrapAppender class. All of the values
	 *            needed to configure the trap sender will be extracted from the
	 *            Appender, via its getter methods.
	 */
	void initialize(SNMPTrapAppender appender);

	void initialize2(SNMPTrapAppender2 appender);

	/**
	 * Sends the PDU defined by the variables of the fields of this class. This
	 * method should always be called last by an application, after having
	 * called the initialization() method once, and the addTrapMessageVariable()
	 * method one or more times.
	 */
	void sendTrap();

	/**
	 * Adds a new Varbind to the SNMP PDU. The Varbind is made of the value of
	 * the application trap OID (applicationTrapOID) parameter, as the key, and
	 * the value paramater to this method as the value. A PDU has a collection
	 * of Varbind variables -- repeated calls to this method will add to that
	 * collection successively.
	 *
	 * @param applicationTrapOIDValue
	 *            - formatted as an OID E.g. "1.3.6.1.2.1.1.2.0.0.0.0" -- this
	 *            OID would point to the standard sysObjectID of the
	 *            "systemName" node of the standard "system" MIB. <br>
	 *            This is the default value, if none is provided. <br>
	 *            If you want(need) to use custom OIDs (such as ones from the
	 *            "private.enterprises" node -- "1.3.6.1.4.1.x.x.x..."), you
	 *            always need to provide the <b>fully qualified</b> OID as the
	 *            parameter for this variable.
	 *            <p>
	 * @param value
	 *            - the text to append to the Varbind that will be added to the
	 *            SNMP PDU.
	 */
	void addTrapMessageVariable(String applicationTrapOIDValue, String value);

}