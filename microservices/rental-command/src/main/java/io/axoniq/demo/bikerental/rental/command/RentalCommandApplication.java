package io.axoniq.demo.bikerental.rental.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentalCommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalCommandApplication.class, args);
	}

	@Autowired
	public void configureSerializers(XStream xStream, ObjectMapper objectMapper) {
		xStream.allowTypesByWildcard(new String[]{"io.axoniq.demo.bikerental.coreapi.**"});
		objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
	}

}
