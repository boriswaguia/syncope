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
package org.apache.syncope.client.console.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.syncope.client.console.PreferenceManager;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.panels.AnyDirectoryPanel;
import org.apache.syncope.client.console.panels.DisplayAttributesModalPanel;
import org.apache.syncope.client.console.wicket.ajax.form.AjaxDownloadBehavior;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.client.console.wizards.CSVPullWizardBuilder;
import org.apache.syncope.client.console.wizards.CSVPushWizardBuilder;
import org.apache.syncope.client.console.wizards.any.ProvisioningReportsPanel;
import org.apache.syncope.client.console.wizards.any.ResultPage;
import org.apache.syncope.client.ui.commons.Constants;
import org.apache.syncope.client.ui.commons.wizards.AjaxWizard;
import org.apache.syncope.common.lib.to.ProvisioningReport;
import org.apache.syncope.common.lib.types.IdRepoEntitlement;
import org.apache.syncope.common.rest.api.beans.AnyQuery;
import org.apache.syncope.common.rest.api.beans.CSVPullSpec;
import org.apache.syncope.common.rest.api.beans.CSVPushSpec;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public class IdMAnyDirectoryPanelAdditionalActionsProvider implements AnyDirectoryPanelAdditionalActionsProvider {

    private static final long serialVersionUID = -6768727277642238924L;

    protected AjaxLink<Void> csvPushLink;

    protected AjaxLink<Void> csvPullLink;

    @Override
    public void add(
            final AnyDirectoryPanel<?, ?> panel,
            final BaseModal<?> modal,
            final WebMarkupContainer container,
            final String type,
            final String realm,
            final String fiql,
            final List<String> pSchemaNames,
            final List<String> dSchemaNames,
            final PageReference pageRef) {

        AjaxDownloadBehavior csvDownloadBehavior = new AjaxDownloadBehavior();
        WebMarkupContainer csvEventSink = new WebMarkupContainer(Constants.OUTER) {

            private static final long serialVersionUID = -957948639666058749L;

            @Override
            public void onEvent(final IEvent<?> event) {
                if (event.getPayload() instanceof AjaxWizard.NewItemCancelEvent) {
                    ((AjaxWizard.NewItemCancelEvent<?>) event.getPayload()).getTarget().
                            ifPresent(target -> modal.close(target));
                } else if (event.getPayload() instanceof AjaxWizard.NewItemFinishEvent) {
                    AjaxWizard.NewItemFinishEvent<?> payload = (AjaxWizard.NewItemFinishEvent) event.getPayload();
                    Optional<AjaxRequestTarget> target = payload.getTarget();

                    SyncopeConsoleSession.get().info(getString(Constants.OPERATION_SUCCEEDED));
                    if (target.isPresent()) {
                        ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target.get());
                        target.get().add(container);
                    }

                    if (payload.getResult() instanceof ArrayList) {
                        modal.setContent(new ResultPage<Serializable>(
                                null,
                                payload.getResult()) {

                            private static final long serialVersionUID = -2630573849050255233L;

                            @Override
                            protected void closeAction(final AjaxRequestTarget target) {
                                modal.close(target);
                            }

                            @Override
                            protected Panel customResultBody(
                                    final String id, final Serializable item, final Serializable result) {

                                @SuppressWarnings("unchecked")
                                ArrayList<ProvisioningReport> reports = (ArrayList<ProvisioningReport>) result;
                                return new ProvisioningReportsPanel(id, reports, pageRef);
                            }
                        });
                        target.ifPresent(t -> t.add(modal.getForm()));
                    } else if (Constants.OPERATION_SUCCEEDED.equals(payload.getResult())) {
                        target.ifPresent(t -> {
                            if (csvDownloadBehavior.hasResponse()) {
                                csvDownloadBehavior.initiate(target.get());
                            }
                            modal.close(t);
                        });
                    }
                }
            }
        };
        csvEventSink.add(csvDownloadBehavior);
        panel.addOuterObject(csvEventSink);
        csvPushLink = new AjaxLink<Void>("csvPush") {

            private static final long serialVersionUID = -817438685948164787L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                CSVPushSpec spec = csvPushSpec(type, panel, pSchemaNames, dSchemaNames);
                AnyQuery query = csvAnyQuery(realm, fiql);

                target.add(modal.setContent(new CSVPushWizardBuilder(spec, query, csvDownloadBehavior, pageRef).
                        setEventSink(csvEventSink).
                        build(BaseModal.CONTENT_ID, AjaxWizard.Mode.EDIT)));

                modal.header(new StringResourceModel("csvPush", panel, Model.of(spec)));
                modal.show(true);
            }
        };
        MetaDataRoleAuthorizationStrategy.authorize(csvPushLink, Component.RENDER,
                String.format("%s,%s", IdRepoEntitlement.IMPLEMENTATION_LIST, IdRepoEntitlement.TASK_EXECUTE));
        panel.addInnerObject(csvPushLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
        csvPullLink = new AjaxLink<Void>("csvPull") {

            private static final long serialVersionUID = -817438685948164787L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                CSVPullSpec spec = csvPullSpec(type, realm);

                target.add(modal.setContent(new CSVPullWizardBuilder(spec, pageRef).
                        setEventSink(csvEventSink).
                        build(BaseModal.CONTENT_ID, AjaxWizard.Mode.EDIT)));

                modal.header(new StringResourceModel("csvPull", panel, Model.of(spec)));
                modal.show(true);
            }
        };
        MetaDataRoleAuthorizationStrategy.authorize(csvPullLink, Component.RENDER,
                String.format("%s,%s", IdRepoEntitlement.IMPLEMENTATION_LIST, IdRepoEntitlement.TASK_EXECUTE));
        panel.addInnerObject(csvPullLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
    }

    protected CSVPushSpec csvPushSpec(
            final String type,
            final AnyDirectoryPanel<?, ?> panel,
            final List<String> pSchemaNames,
            final List<String> dSchemaNames) {

        CSVPushSpec spec = new CSVPushSpec.Builder(type).build();
        spec.setFields(PreferenceManager.getList(panel.getRequest(),
                DisplayAttributesModalPanel.getPrefDetailView(type)).
                stream().filter(name -> !Constants.KEY_FIELD_NAME.equalsIgnoreCase(name)).
                collect(Collectors.toList()));
        spec.setPlainAttrs(PreferenceManager.getList(
                panel.getRequest(), DisplayAttributesModalPanel.getPrefPlainAttributeView(type)).
                stream().filter(name -> pSchemaNames.contains(name)).collect(Collectors.toList()));
        spec.setDerAttrs(PreferenceManager.getList(panel.getRequest(),
                DisplayAttributesModalPanel.getPrefPlainAttributeView(type)).
                stream().filter(name -> dSchemaNames.contains(name)).collect(Collectors.toList()));
        return spec;
    }

    protected CSVPullSpec csvPullSpec(final String type, final String realm) {
        CSVPullSpec spec = new CSVPullSpec();
        spec.setAnyTypeKey(type);
        spec.setDestinationRealm(realm);
        return spec;
    }

    protected AnyQuery csvAnyQuery(final String realm, final String fiql) {
        return new AnyQuery.Builder().realm(realm).fiql(fiql).build();
    }

    @Override
    public void hide() {
        csvPushLink.setEnabled(false);
        csvPushLink.setVisible(false);
        csvPullLink.setEnabled(false);
        csvPullLink.setVisible(false);
    }
}
