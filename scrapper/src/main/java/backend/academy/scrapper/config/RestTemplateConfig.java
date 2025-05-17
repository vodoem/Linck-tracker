package backend.academy.scrapper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${http.timeout.connect:5000}") // Значение по умолчанию: 5000
    private int connectTimeout;

    @Value("${http.timeout.read:10000}") // Значение по умолчанию: 10000
    private int readTimeout;

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); // Значение по умолчанию, будет переопределено из конфига
        factory.setReadTimeout(readTimeout); // Значение по умолчанию, будет переопределено из конфига
        return factory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }
}
