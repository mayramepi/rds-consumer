package ar.gob.recibosdesueldos.consumer.config;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import javax.jms.Queue;

@Configuration
@EnableJms
public class JmsConfig /*implements JmsListenerConfigurer */{
//    @Bean
//    public DefaultMessageHandlerMethodFactory handlerMethodFactory() {
//        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
//        factory.setMessageConverter(messageConverter());
//        return factory;
//    }
//
//    @Bean
//    public MessageConverter messageConverter() {
//        return new MappingJackson2MessageConverter();
//    }
//
//    @Override
//    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
//        registrar.setMessageHandlerMethodFactory(handlerMethodFactory());
//    }
    @Value("${app.jms_queue:lotes.queue}")
    private String jmsQueue;
    @Bean
    public Queue queue() {
        return new ActiveMQQueue(jmsQueue);
    }


}
