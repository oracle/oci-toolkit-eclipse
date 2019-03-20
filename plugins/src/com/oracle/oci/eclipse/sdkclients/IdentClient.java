/**
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.ListAvailabilityDomainsResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class IdentClient {

    private static IdentClient single_instance = null;
    private static IdentityClient identityClient;
    private List<Compartment> compartmentList = new ArrayList<Compartment>();
    private final String ROOT_COMPARTMENT_NAME = "[Root Compartment]";

    private IdentClient() {
        if (identityClient == null) {
            identityClient = createIdentityClient();
        }
    }

    public static IdentClient getInstance() {
        if (single_instance == null) {
            single_instance = new IdentClient();
        }
        return single_instance;
    }

    public void updateClient() {
        try {
            if (identityClient != null) {
                identityClient.close();
            }
            createIdentityClient();
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    private IdentityClient createIdentityClient(){
        identityClient = new IdentityClient(AuthProvider.getInstance().getProvider());
        identityClient.setRegion(AuthProvider.getInstance().getRegion());
        return identityClient;
    }


    @Override
    public void finalize() throws Throwable{
        identityClient.close();
        single_instance = null;
    }

    public List<Compartment> getCompartmentList() {
        String nextPageToken = null;
        String compartmentId = AuthProvider.getInstance().getProvider().getTenantId();
        compartmentList.clear();

        Compartment rootComp = Compartment.builder()
                .compartmentId(compartmentId)
                .id(compartmentId)
                .name(ROOT_COMPARTMENT_NAME)
                .lifecycleState(LifecycleState.Active)
                .build();
        compartmentList.add(rootComp);

        do {
            try {
                ListCompartmentsResponse response =
                        identityClient.listCompartments(
                                ListCompartmentsRequest.builder()
                                .limit(10)
                                .compartmentId(compartmentId)
                                .page(nextPageToken)
                                .build());
                if (response != null) {
                    compartmentList.addAll(response.getItems());
                    nextPageToken = response.getOpcNextPage();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (nextPageToken != null);

        return compartmentList;
    }

    public String getCurrentCompartmentName() {
        for(Compartment compartment : compartmentList) {
            if (compartment.getId().equals(AuthProvider.getInstance().getCompartmentId())) {
                return compartment.getName();
            }
        }
        return ROOT_COMPARTMENT_NAME;
    }

    public List<RegionSubscription> getRegionsList() {
        ListRegionSubscriptionsResponse response =
                identityClient.listRegionSubscriptions(
                        ListRegionSubscriptionsRequest.builder().tenancyId(
                                AuthProvider.getInstance().getProvider().getTenantId()).build());
        return response.getItems();
    }

    public List<AvailabilityDomain> getAvailabilityDomains(
            AuthenticationDetailsProvider provider, String compartmentId, com.oracle.bmc.Region region) throws Exception {

        List<AvailabilityDomain> domains = new ArrayList<AvailabilityDomain>();

        ListAvailabilityDomainsResponse listAvailabilityDomainsResponse =
                identityClient.listAvailabilityDomains(
                        ListAvailabilityDomainsRequest.builder()
                        .compartmentId(compartmentId)
                        .build());
        if(listAvailabilityDomainsResponse != null)
            domains = listAvailabilityDomainsResponse.getItems();

        return domains;
    }
}
