/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.pages;

import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.ui.commons.Constants;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.markup.html.WebPage;

public class SAML2SPLogin extends WebPage {

    private static final long serialVersionUID = -245801838574917258L;

    private static final Logger LOG = LoggerFactory.getLogger(SAML2SPLogin.class);

    private static final String SAML_ACCESS_ERROR = "SAML 2.0 access error";

    public SAML2SPLogin(final PageParameters parameters) {
        super(parameters);

        String token = (String) ((ServletWebRequest) getRequest()).getContainerRequest().
                getSession().getAttribute(org.apache.syncope.ext.saml2lsp.agent.Constants.SAML2SPJWT);
        if (StringUtils.isBlank(token)) {
            LOG.error("No JWT found, redirecting to default greeter");

            PageParameters params = new PageParameters();
            params.add("errorMessage", SAML_ACCESS_ERROR);
            setResponsePage(Login.class, params);
        }

        IAuthenticationStrategy strategy = getApplication().getSecuritySettings().getAuthenticationStrategy();

        if (SyncopeConsoleSession.get().authenticate(token)) {
            if (parameters.get("sloSupported").toBoolean(false)) {
                SyncopeConsoleSession.get().setAttribute(Constants.BEFORE_LOGOUT_PAGE, SAML2SPBeforeLogout.class);
            }

            // If login has been called because the user was not yet logged in, than continue to the
            // original destination, otherwise to the Home page
            continueToOriginalDestination();
            setResponsePage(getApplication().getHomePage());
        } else {
            PageParameters params = new PageParameters();
            params.add("errorMessage", SAML_ACCESS_ERROR);
            setResponsePage(Login.class, params);
        }
        strategy.remove();
    }
}
