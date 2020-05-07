package io.microsamples.messaging.mocklistener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MockListenerApplication implements RabbitListenerConfigurer {
	public static void main(String[] args) {
		SpringApplication.run(MockListenerApplication.class, args);
	}

	@Bean
	public MappingJackson2MessageConverter jackson2Converter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		return converter;
	}

	@Bean
	public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(jackson2Converter());
		return factory;
	}

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
		rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(myHandlerMethodFactory());
	}
}

@RestController
@RequestMapping("responses")
@Slf4j
class MockListener {

	private Map<String, Object> response = new HashMap<>();

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void responseToReturn(@RequestBody Map<String, Object> rtn){
		this.response = rtn;
	}


	@RabbitListener(queues = "${spring.cloud.stream.bindings.input.destination}")
	public Map<String, Object> handleMessage(Message<Map<String, Object>> message) {
		log.info("Received: {}", message.getPayload());
		log.info("Sent: {}", response);
		return response;
	}
}
