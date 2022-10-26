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

import org.apache.log4j.helpers.LogLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Title: NetSnmpCommandLineTrapSender<br>
 * <br>
 * Description: This class makes use of the NetSNMP <b>snmptrap</b> utility to
 * send traps that implement the underlying SNMP protocol(s).<br>
 * <br>
 * NOTE: this class is intended primarily as a "proof-of-concept", and to serve
 * as an example of a possible usage of a command line trap sending utility with
 * the appender.  This class is not intended for production-grade environments!<br>
 * <br>
 * You can get a copy of NetSNMP <a href="http://net-snmp.sourceforge.net/">here</a>.<br>
 * <br>
 * You will need a copy of the library (e.g. "snmptrap.exe" and "libsnmp.dll" on Win32
 * platforms, at a minimum) to use this class.<br>
 * <br>
 * This sender launches a separate process, calling the NetSnmp utility "snmptrap" to
 * send a trap.  Because this sender's job is to start an operating system process,
 * it needs to know things that are not readily available to it via the normal avenues
 * open to a Log4J Appender.  In particular, it needs to know the location of
 * the NetSnmp binaries (the file system path), and the path to the MIB files used
 * by NetSnmp.  You communicate this information to the sender using Java System
 * Properties.<br>
 * <br>
 * The path to the NetSnmp binaries is set via the property
 * "log4j.ext.snmpTrapAppender.netSnmp.binPath".   If not set, the sender defaults to a value
 * of "/usr/local/bin/".<br>
 * <br>
 * The path to the NetSnmp MIBs is set via the property
 * "log4j.ext.snmpTrapAppender.netSnmp.mibsPath".   If not set, the sender defaults to a value
 * of "/usr/local/share/mibs".<br>
 * <br>
 * You can also get the sender to emit some diagnostic/debugging information by setting the
 * value of the "log4j.ext.snmpTrapAppender.netSnmp.diagnostic" property to "true".<br>
 * <br>
 * Thus, as an example, the following options to the "java" command line set the binary path,
 * the MIB path and the diagnostic flag for this sender:<br>
 * <br>
 * <code>-Dlog4j.ext.snmpTrapAppender.netSnmp.binPath="D:\\apps\\ucd-snmp-4.2.3-win32\\usr\\bin\\"
 * -Dlog4j.ext.snmpTrapAppender.netSnmp.mibsPath="D:\\apps\\ucd-snmp-4.2.3-win32\\usr\\mibs"
 * -Dlog4j.ext.snmpTrapAppender.netSnmp.diagnostic="true"</code><br>
 * <p>
 * @author Mark Masterson (<a href="mailto:m.masterson@computer.org">m.masterson@computer.org</a>)<br>
 * @version 1.0.1<br>
 * 2002-11-01<br>
 * changes ---<br>
 *
 * 2002-12-10: mwm : minor tweaks and prettying up of code.<br>
 *
 * 2003-05-24: mwm : minor changes to accomodate the changes in the SnmpTrapSenderFacade interface.<br>
 */
public class NetSnmpCommandLineTrapSender implements SnmpTrapSenderFacade {

    private static final String NET_SNMP_CMD_LINE_TRAP_SENDER_DIAGNOSTIC_PROPERTY
            = "log4j.ext.snmpTrapAppender.netSnmp.diagnostic";
    private static final String NET_SNMP_BIN_PATH_PROPERTY = "log4j.ext.snmpTrapAppender.netSnmp.binPath";
    private static final String NET_SNMP_MIBS_PATH_PROPERTY = "log4j.ext.snmpTrapAppender.netSnmp.mibsPath";
    private static final String NET_SNMP_BIN_PATH_DEFAULT = "/usr/local/bin/";
    private static final String NET_SNMP_MIBS_PATH_DEFAULT = "/usr/local/share/mibs";
    private String managementHost = "127.0.0.1";
    private String enterpriseOID = "1.3.6.1.2.1.1.2.0";
    private String localIPAddress = "127.0.0.1";
    private int genericTrapType;
    private int specificTrapType = 6;
    private String communityString = "public";
    private long sysUpTime;
    private boolean isInitialized;
    private List bindVariables;

    public void initialize(final SNMPTrapAppender appender) {
        managementHost = appender.getManagementHost();
        enterpriseOID = appender.getEnterpriseOID();
        localIPAddress = appender.getLocalIPAddress();
        communityString = appender.getCommunityString();
        sysUpTime = appender.getSysUpTime();
        genericTrapType = appender.getGenericTrapType();
        specificTrapType = appender.getSpecificTrapType();
        bindVariables = new ArrayList();
        isInitialized = true;
    }

    public void addTrapMessageVariable(final String applicationTrapOIDValue,
                                       final String value) {
        //check pre-condition
        if (!isInitialized) {
            LogLog.error("The initialize() method must be called before calling addTrapMessageVariable()");
            return;
        }
        bindVariables.add(new BindVariable(applicationTrapOIDValue, value));
    }

    public void sendTrap() {
        final String binPath = System.getProperty(NET_SNMP_BIN_PATH_PROPERTY, NET_SNMP_BIN_PATH_DEFAULT);
        final String mibsPath = System.getProperty(NET_SNMP_MIBS_PATH_PROPERTY, NET_SNMP_MIBS_PATH_DEFAULT);
        final boolean diagnosticFlag = Boolean.getBoolean(NET_SNMP_CMD_LINE_TRAP_SENDER_DIAGNOSTIC_PROPERTY);
        if (diagnosticFlag) {
            LogLog.error(binPath);
            LogLog.error(mibsPath);
        }
        final int index = 0;
        final String[] paramsX = sizeParameterArray();
        try {
            paramsX[index] = new StringBuffer().append(binPath).append("snmptrap").toString();
            fillParamsArray(paramsX, index, mibsPath);
            if (diagnosticFlag) LogLog.error(new StringBuffer().append("Command line array contains: ")
                    .append(Arrays.asList(paramsX)).toString());
            final Process einProcess = Runtime.getRuntime().exec(paramsX, null, new File(binPath));
            handleProcessStream(new BufferedReader(new InputStreamReader(einProcess.getInputStream())), diagnosticFlag);
            handleProcessStream(new BufferedReader(new InputStreamReader(einProcess.getErrorStream())), diagnosticFlag);
            final int rc = einProcess.waitFor();
            if (0 != rc) {
                LogLog.error("Error executing snmptrap!");
            }
        } catch (Exception e) {
            LogLog.error("Error executing snmptrap!", e);
        }
    }

    private String[] sizeParameterArray() {
        final int mandatoryParameterCount = 9;
        final int variableParameterCount = 3 * bindVariables.size();
        return new String[mandatoryParameterCount + variableParameterCount];
    }

    private static void handleProcessStream(final BufferedReader bufferedInputStreamReader,
                                            final boolean diagnosticFlag)
            throws IOException, InterruptedException {
        for (int i = 0; 99 > i; i++) {
            if (bufferedInputStreamReader.ready()) break;
            else Thread.sleep(100);
        }
        while (bufferedInputStreamReader.ready()) {
            if (diagnosticFlag) {
                LogLog.error(bufferedInputStreamReader.readLine());
            } else {
                bufferedInputStreamReader.readLine();
            }
        }
        bufferedInputStreamReader.close();
    }

    private void fillParamsArray(final String[] paramsX, int index, final String mibsPath) {
        paramsX[++index] = new StringBuffer().append("-M ").append(mibsPath).toString();
        paramsX[++index] = managementHost;
        paramsX[++index] = communityString;
        paramsX[++index] = new StringBuffer().append('.').append(enterpriseOID).toString();
        paramsX[++index] = localIPAddress;
        paramsX[++index] = Integer.toString(genericTrapType);
        paramsX[++index] = Integer.toString(specificTrapType);
        paramsX[++index] = Long.toString(sysUpTime);
        for (Iterator varsIt = bindVariables.iterator(); varsIt.hasNext();) {
            final BindVariable tmpVar = (BindVariable) varsIt.next();
            paramsX[++index] = new StringBuffer().append('.').append(tmpVar.getOid()).toString();
            paramsX[++index] = "s";
            paramsX[++index] = new StringBuffer().append('\"').append(tmpVar.getValue()).append('\"').toString();
        }
    }

    private static class BindVariable {

        private final String oid;
        private final String value;

        BindVariable(final String oidValue, final String variableValue) {
            oid = oidValue;
            value = variableValue;
        }

        public String getOid() {
            return oid;
        }

        public String getValue() {
            return value;
        }
    }

	@Override
	public void initialize2(SNMPTrapAppender2 appender) {
		// TODO Auto-generated method stub
		
	}
}
