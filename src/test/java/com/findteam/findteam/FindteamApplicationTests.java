package com.findteam.findteam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "telegram.long-polling.enabled=false")
class FindteamApplicationTests {

	@Test
	void contextLoads() {
	}

}
