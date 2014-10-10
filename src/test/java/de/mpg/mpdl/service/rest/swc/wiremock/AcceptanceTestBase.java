/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mpg.mpdl.service.rest.swc.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class AcceptanceTestBase {

    public static final int PORT_NUMBER = 8089;
    protected static WireMockServer wireMockServer;
	protected static WireMockTestClient testClient;

	@BeforeClass
	public static void setupServer() {
		setupServer(wireMockConfig().port(PORT_NUMBER));
	}

	@AfterClass
	public static void serverShutdown() {
		wireMockServer.stop();
	}

    public static void setupServer(Options options) {
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        testClient = new WireMockTestClient(PORT_NUMBER);
        WireMock.configure();
    }


	@Before
	public void init() throws InterruptedException {
        WireMock.configureFor("localhost", PORT_NUMBER);
	}


}
