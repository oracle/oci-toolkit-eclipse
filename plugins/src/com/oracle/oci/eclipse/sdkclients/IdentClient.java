/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.oracle.bmc.Region;
import com.oracle.bmc.identity.model.Policy;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.AuthToken;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.Compartment.LifecycleState;
import com.oracle.bmc.identity.model.CreateAuthTokenDetails;
import com.oracle.bmc.identity.model.CreatePolicyDetails;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.model.Tenancy;
import com.oracle.bmc.identity.model.User;
import com.oracle.bmc.identity.requests.CreateAuthTokenRequest;
import com.oracle.bmc.identity.requests.CreatePolicyRequest;
import com.oracle.bmc.identity.requests.GetTenancyRequest;
import com.oracle.bmc.identity.requests.GetUserRequest;
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.CreateAuthTokenResponse;
import com.oracle.bmc.identity.responses.CreatePolicyResponse;
import com.oracle.bmc.identity.responses.GetTenancyResponse;
import com.oracle.bmc.identity.responses.GetUserResponse;
import com.oracle.bmc.identity.responses.ListAvailabilityDomainsResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;
import com.oracle.bmc.logging.model.Log;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class IdentClient extends BaseClient {

    private static IdentClient single_instance = null;
    private static IdentityClient identityClient;
    private static Tenancy tenancyInfo = null;

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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        identityClient.setRegion(evt.getNewValue().toString());
    }

    @Override
    public void updateClient() {
        close();
        createIdentityClient();
    }

    @Override
    public void close() {
        try {
            if (identityClient != null) {
                identityClient.close();
            }
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

    public List<Compartment> getCompartmentList(Compartment compartment) {
        List<Compartment> compartmentList = new ArrayList<Compartment>();

        try {
            ListCompartmentsResponse response = identityClient
                    .listCompartments(ListCompartmentsRequest.builder().compartmentId(compartment.getId())
                            .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible).build());
            if (response != null) {
                compartmentList = response.getItems()
                        .stream()
                        .filter(predicate -> !predicate.getLifecycleState().equals(LifecycleState.Deleted))
                        .filter(predicate -> !predicate.getLifecycleState().equals(LifecycleState.Deleting))
                        .sorted(Comparator.comparing(Compartment::getName))
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return compartmentList;
    }

    public Compartment getRootCompartment() {
        String compartmentId = AuthProvider.getInstance().getProvider().getTenantId();
        Compartment rootComp = Compartment.builder()
                .compartmentId(compartmentId)
                .id(compartmentId)
                .name(AuthProvider.ROOT_COMPARTMENT_NAME)
                .lifecycleState(LifecycleState.Active)
                .build();
        return rootComp;
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

    public AuthToken createAuthToken() {
        // Get the user home region
        Region homeRegion = Region.fromRegionCode(getTenancy().getHomeRegionKey());

        // We need to set the client to use user home region to be able to create the Auth Token
        identityClient.setRegion(homeRegion);

        CreateAuthTokenRequest request = CreateAuthTokenRequest.builder()
                .userId(AuthProvider.getInstance().getProvider().getUserId())
                .createAuthTokenDetails(CreateAuthTokenDetails.builder().description("Eclipse OKE Token").build()).build();
        CreateAuthTokenResponse response = identityClient.createAuthToken(request);

        // Switch back region to the one the user selected
        identityClient.setRegion(AuthProvider.getInstance().getRegion());

        return response.getAuthToken();
    }

    public Tenancy getTenancy() {
        if(tenancyInfo == null) {
            GetTenancyRequest request = GetTenancyRequest.builder()
                    .tenancyId(AuthProvider.getInstance().getProvider().getTenantId()).build();
            GetTenancyResponse response = identityClient.getTenancy(request);
            tenancyInfo = response.getTenancy();
        }
        return tenancyInfo;
    }

    public User getUser() {
        GetUserRequest request = GetUserRequest.builder()
                .userId(AuthProvider.getInstance().getProvider().getUserId()).build();
        GetUserResponse response = identityClient.getUser(request);

        return response.getUser();
    }
    
    public Policy createIAMPolicy(String compartmentId, String desc, String name, ArrayList<String> statements) {
    	System.out.println("compartmentId: " + compartmentId);
    	System.out.println("desc: " + desc);
    	System.out.println("name: " + name);
    	System.out.println("statements: " + statements);
    	
    	CreatePolicyRequest request = 
    			CreatePolicyRequest.builder()
                        .createPolicyDetails(
                                CreatePolicyDetails.builder()
                                        .compartmentId(compartmentId)
                                        .description(desc)
                                        .name(name)
                                        .statements(statements)
                                        .build())
                        .build();
    	
    	CreatePolicyResponse response = identityClient.createPolicy(request);
        System.out.println("Successfully added policies. Statements: " + String.join("\n", statements));
        return response.getPolicy();
    }
}
