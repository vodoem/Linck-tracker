package backend.academy.scrapper.fallback;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransportHealthMonitor {

    private final TransportManager transportManager;

    public TransportHealthMonitor(TransportManager transportManager) {
        this.transportManager = transportManager;
    }

    @Scheduled(fixedRate = 5000) // Проверка каждые 5 секунд
    public void monitorTransportHealth() {
        transportManager.checkAndSwitchTransport();
    }
}
