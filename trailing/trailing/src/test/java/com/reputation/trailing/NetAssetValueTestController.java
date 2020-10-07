package com.reputation.trailing;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reputation.trailing.models.Trailing;

public class NetAssetValueTestController extends AbstractTestTemplate {

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void getNAVDetails() throws Exception {
		String uri = "/api/v1/getTrailingReturns";
		Trailing trail = new Trailing();
		trail.setSchemeNumber("102884");
		trail.setInvestmentPeriod(1);
		trail.setHorizon(1);
		
		String inputJson = super.mapToJson(trail);
		MvcResult mvcResult =
		 mvc.perform(post("/m/v1/feed/webinar/create").contentType(MediaType.APPLICATION_JSON)
		            .content(inputJson)).andExpect(status().isOk()).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		String content = mvcResult.getResponse().getContentAsString();
		assertEquals(content, "NetAssetValue returns are generated successfully");
	}

}
