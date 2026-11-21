package com.example.katEye;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requiere MySQL; se habilitara con Testcontainers en la fase de pruebas")
@SpringBootTest
class KatEyeApplicationTests {

	@Test
	void contextLoads() {
	}

}
