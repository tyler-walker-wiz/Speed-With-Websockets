package com.example.speed.Controller;

import com.example.speed.Model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class SpeedController {
    private static final Logger logger = LoggerFactory.getLogger(SpeedController.class);
    private static final SpeedInstance speedInstance = SpeedInstance.getInstance();

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    // TODO: Add game logic stuff

    @MessageMapping("/rest/api/game.playCard")
    public void playCard(@Payload Action cardMove, @Header("simpSessionId") String sessionID) {
        // TODO: Update game instance using Action

        sendGameState();
    }

    @MessageMapping("/rest/api/game.drawCard")
    public void drawCard(@Header("simpSessionId") String sessionID) {
        Map playerMap = speedInstance.getPlayerMap();
        if (playerMap.containsKey(sessionID)) {
            Player player = (Player) playerMap.get(sessionID);
            Hand hand = player.getHand();
            Deck deck = player.getDrawPile();
            if (hand.getSize() < 5 && deck.getSize() > 0) {
                hand.addCard(deck.dealCard());
            }
        }


        sendGameState();
    }

    public boolean addPlayer(String sessionID) {
        Map playerMap = speedInstance.getPlayerMap();
        if (playerMap.size() < 2) {
            if (!playerMap.containsKey(sessionID)) {
                playerMap.put(sessionID, new Player(sessionID));
                return true;
            }
        }
        return false;
    }

    public boolean removePlayer(String sessionID) {
        Map playerMap = speedInstance.getPlayerMap();
        if (playerMap.containsKey(sessionID)) {
            playerMap.remove(sessionID);
            return true;
        }
        return false;
    }

    private void sendGameState() {
        for (String playerID : speedInstance.getPlayerMap().keySet()) {
            SanitizedGameState gameState = new SanitizedGameState(playerID, speedInstance);
            simpMessagingTemplate.convertAndSendToUser(playerID, "/queuq/gamestate", gameState);
        }
    }
}
