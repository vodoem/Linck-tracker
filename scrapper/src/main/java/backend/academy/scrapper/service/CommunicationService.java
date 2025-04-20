package backend.academy.scrapper.service;

import backend.academy.model.LinkUpdate;

public interface CommunicationService {
    void sendUpdate(LinkUpdate update);
}
