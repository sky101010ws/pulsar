/**
 * Copyright 2016 Yahoo Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahoo.pulsar.client.admin.internal;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.WebTarget;

import com.yahoo.pulsar.client.admin.PulsarAdminException;
import com.yahoo.pulsar.client.admin.Lookup;
import com.yahoo.pulsar.client.api.Authentication;
import com.yahoo.pulsar.common.lookup.data.LookupData;
import com.yahoo.pulsar.common.naming.DestinationName;

public class LookupImpl extends BaseResource implements Lookup {

    private final WebTarget v2lookup;
    private final boolean useTls;

    public LookupImpl(WebTarget web, Authentication auth, boolean useTls) {
        super(auth);
        this.useTls = useTls;
        v2lookup = web.path("/lookup/v2");
    }

    private PulsarAdminException getLookupApiException(Exception e) {
        if (e instanceof ClientErrorException) {
            return new PulsarAdminException((ClientErrorException) e, e.getMessage());
        } else {
            return getApiException(e);
        }
    }

    @Override
    public String lookupDestination(String destination) throws PulsarAdminException {
        try {
            DestinationName destName = DestinationName.get(destination);
            return doDestinationLookup(v2lookup.path("/destination"), destName);
        } catch (Exception e) {
            throw getLookupApiException(e);
        }
    }

    private String doDestinationLookup(WebTarget lookupResource, DestinationName destName) throws PulsarAdminException {
        LookupData lookupData = request(lookupResource.path(destName.getLookupName())).get(LookupData.class);
        if (useTls) {
            return lookupData.getBrokerUrlTls();
        } else {
            return lookupData.getBrokerUrl();
        }
    }

}
