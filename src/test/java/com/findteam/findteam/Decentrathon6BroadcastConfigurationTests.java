package com.findteam.findteam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.findteam.findteam.service.Decentrathon6BroadcastRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;

class Decentrathon6BroadcastConfigurationTests {

	@Test
	void broadcastRunnerIsDisabledByDefault() {
		ConditionalOnProperty property = Decentrathon6BroadcastRunner.class
				.getAnnotation(ConditionalOnProperty.class);
		assertEquals("findteam.broadcast.decentrathon-6.enabled", property.name()[0]);
		assertEquals("true", property.havingValue());
		try {
			String applicationConfig = new String(new ClassPathResource("application.yml").getInputStream().readAllBytes());
			assertTrue(applicationConfig.contains("decentrathon-6:\n      enabled: false"));
		} catch (java.io.IOException ex) {
			throw new AssertionError("Could not read application.yml", ex);
		}
	}
}
