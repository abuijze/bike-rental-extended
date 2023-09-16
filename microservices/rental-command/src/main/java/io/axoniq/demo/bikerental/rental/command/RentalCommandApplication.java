package io.axoniq.demo.bikerental.rental.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentalCommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalCommandApplication.class, args);
	}

	@Autowired
	public void configureSerializers(ObjectMapper objectMapper) {
		objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
	}
}
