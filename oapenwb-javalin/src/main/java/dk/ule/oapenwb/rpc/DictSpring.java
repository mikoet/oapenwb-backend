// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb.rpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DictSpring
{
	private final ManagedChannel channel;
	private final HealthBlockingStub healthStub;

	@Inject
	public DictSpring(AppConfig appConfig) {
		this.channel = ManagedChannelBuilder.forAddress(
			"localhost", appConfig.getRpcHostPort()).usePlaintext().build();
		this.healthStub = HealthGrpc.newBlockingStub(this.channel);
	}

	public void shutdown() {
		this.channel.shutdown();
	}

	public boolean isHealthy() {
		try {
			HealthCheckResponse response = this.healthStub.check(HealthCheckRequest.newBuilder().build());
			return response.getStatus() == HealthCheckResponse.ServingStatus.SERVING;
		} catch (StatusRuntimeException e) {
			log.debug("Error checking health status");
		}
		return false;
	}
}
