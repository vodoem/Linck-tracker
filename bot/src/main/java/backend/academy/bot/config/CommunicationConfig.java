package backend.academy.bot.config;

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
