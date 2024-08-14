// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.rpc.v1;

import dk.ule.oapenwb.rpc.v1.test.TestGrpc;
import dk.ule.oapenwb.rpc.v1.test.TestRequest;
import dk.ule.oapenwb.rpc.v1.test.TestResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class TestService extends TestGrpc.TestImplBase
{
	@Override
	public void test(TestRequest request, StreamObserver<TestResponse> responseObserver) {
		log.info("test() was called");

		responseObserver.onNext(TestResponse.newBuilder().setSomeString("Moin").build());
		responseObserver.onCompleted();
	}
}
