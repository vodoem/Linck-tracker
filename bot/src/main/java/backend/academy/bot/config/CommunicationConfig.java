package backend.academy.bot.config;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.kafka.KafkaCommunicationService;
import backend.academy.bot.kafka.KafkaProducerService;
import backend.academy.bot.service.CommunicationService;
import backend.academy.bot.service.HttpCommunicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunicationConfig {

//    @Bean
//    @ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
//    public CommunicationService httpCommunicationService(ScrapperClient scrapperClient) {
//        return new HttpCommunicationService(scrapperClient);
//    }
//
//    @Bean
//    @ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
//    public CommunicationService kafkaCommunicationService(KafkaProducerService kafkaProducerService) {
//        return new KafkaCommunicationService(kafkaProducerService);
//    }
}
