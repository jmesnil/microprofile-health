/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICES file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.eclipse.microprofile.health;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author Heiko Braun
 */
@ApplicationScoped
@Path("/health")
public class HttpBinding {

    @Inject
    @Any
    Instance<org.eclipse.microprofile.health.HealthCheckProcedure> procedures;

    @GET
    @Produces(value = "application/json")
    public Response checkHealth() {

        if (procedures == null) {
            return Response.ok().status(204).build();
        }

        List<org.eclipse.microprofile.health.HealthStatus> responses = new ArrayList<>();

        for (org.eclipse.microprofile.health.HealthCheckProcedure procedure : procedures) {
            org.eclipse.microprofile.health.HealthStatus status = procedure.execute();
            responses.add(status);
        }

        StringBuffer sb = new StringBuffer("{");
        sb.append("\"checks\": [\n");

        int i = 0;
        boolean failed = false;

        for (org.eclipse.microprofile.health.HealthStatus resp : responses) {

            sb.append(resp.toJson());

            if (!failed) {
                failed = resp.getState() != Status.State.UP;
            }

            if (i < responses.size() - 1) {
                sb.append(",\n");
            }
            i++;
        }
        sb.append("],\n");

        String outcome = failed ? "DOWN" : "UP";
        sb.append("\"outcome\": \"" + outcome + "\"\n");
        sb.append("}\n");

        return Response.ok(sb.toString()).build();
    }

}

