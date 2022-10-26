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

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.StringTokenizer;

/**
 * This layout allows you to embed trap specific OID's in the <b>conversion pattern</b> defined in the
 * configuration script, assigning each embedded OID to a specific element of the LoggingEvent.  Each element
 * obtained this way will be appended as a separate VarBind to the trap's PDU.  Thus, you can have the
 * Level (DEBUG, WARN, ERROR, etc.) as one VarBind, the logging message as another, the class name, or date,
 * or whatever, as yet more..  Each VarBind thus defined will typically be displayed by the SNMP managment
 * application that receives the trap as a discrete element -- this makes the trap much more readable and
 * can dramatically increase the usefulness of the appender.<br>
 * <br>
 * When using this Layout class, the appender assumes that the conversion pattern string in the
 * properties file will be in the following format: <br>
 * <br>
 * <b>(LoggingEvent element)(Value of the <code>varDelim</code> parameter of this class)(trap OID)(Value of the
 * <code>valuePairDelim</code> parameter of this class)</b>....(pattern repeats n+ times).<br>
 * <br>
 * ... where value pairs of conversion characters and applicationTrapOIDs are delimited
 * by the <code>valuePairDelim</code> variable (defined in log4j config, default is <b>/</b>).  Within
 * these value pairs the values are delimited by the <code>varDelim</code> variable (defined in
 * log4j config, default is <b>;</b>). <br>
 * <br>
 * Each conversion character is then formatted into the specific logging
 * information data it represents.  It is then bound to the SNMP PDU as a VarBind,
 * with the applicationTrapOID as the name, and the logging data string as the value...<br>
 * <br>
 * Thus, in the following example:<br>
 * <br>
 * <b>
 * "p;1.3.6.1.4.1.24.100.1/%m;1.3.6.1.4.1.24.100.2/%C;1.3.6.1.4.1.24.100.3"<br>
 * </b>
 *  <br>
 * ... the "p" element of the LoggingEvent (the Level) is mapped with the OID "1.3.6.1.4.1.24.100.1" to a
 * discrete VarBind, the "m" element of the LoggingEvent (the message) is mapped with the OID
 * "1.3.6.1.4.1.24.100.2" to another discrete VarBind, and so on.
 * <p>
 * Version 1.0.1<br>
 * 2001-09-29<br>
 * changes ---
 *
 * 2003.03.23: mwm : moved the #formatMultipleVarBinds method from the Appender, where it didn't seem
 * to belong, to this class.  Given the details of the task that this method resolves, and the means
 * that it uses to do so, it seems a better fit as a responsibility of this class.<br>
 *
 * @author Mark Masterson (<a href="mailto:m.masterson@computer.org">m.masterson@computer.org</a>),
 * @author <br>Matt Monks (<a href="mailto:Matthew.Monks@netdecisions.com">Matthew.Monks@netdecisions.com</a>)
 */
public class SnmpDelimitedConversionPatternLayout extends PatternLayout {

    private String valuePairDelim = "/";
    private String varDelim = ";";

    /**
     * Gets the value of the delimiter used in the conversion pattern string to delimit value pairs that should be added
     * as separate VarBind variables to the trap. Default is "/".
     *
     * @return the delimiter character used
     */
    public String getValuePairDelim() {
        return valuePairDelim;
    }

    /**
     * Sets the value of the delimiter used in the conversion pattern string to delimit value pairs that should be added
     * as separate VarBind variables to the trap. Default is "/".
     *
     * @param valuePairDelim delimiter value pairs
     */
    public void setValuePairDelim(final String valuePairDelim) {
        this.valuePairDelim = valuePairDelim;
    }

    /**
     * Gets the value of the delimiter used in the conversion pattern string to delimit the key and value in a value
     * pair embedded within the string. Default is ";".
     *
     * @return the delimiter character used
     */
    public String getVarDelim() {
        return varDelim;
    }

    /**
     * Sets the value of the delimiter used in the conversion pattern string to delimit the key and value in a value
     * pair embedded within the string. Default is ";".
     *
     * @param varDelim delimiter within value pairs
     */
    public void setVarDelim(final String varDelim) {
        this.varDelim = varDelim;
    }

    /**
     * Override this to insulate us from potential changes to the super class.
     *
     * @return true
     */
    public boolean ignoresThrowable() {
        return true;
    }

    /**
     * Breaks the conversion pattern string itself up and, using the tokens thus found, builds distinct VarBinds out of
     * the OID's <b>embedded</b> in the conversion pattern string and the escaped elements of the LoggingEvent.
     *
     * @param event to log
     * @param out   logging target
     */
    public void formatMultipleVarBinds(final LoggingEvent event, final SnmpTrapSenderFacade out) {
        final String pattern = this.getConversionPattern();
        final StringTokenizer splitter = new StringTokenizer(pattern, this.getValuePairDelim());
        while (splitter.hasMoreTokens()) {
            final String variable = splitter.nextToken();
            final StringTokenizer varSplitter = new StringTokenizer(variable, this.getVarDelim());
            final PatternLayout layout = new PatternLayout(varSplitter.nextToken());
            final String parsedResult = layout.format(event);
            out.addTrapMessageVariable(varSplitter.nextToken(), parsedResult);
        }
    }
}
