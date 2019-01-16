package com.bytecubed.nlp.parsing;

import com.bytecubed.commons.Formation;
import com.bytecubed.commons.models.Move;
import com.bytecubed.commons.models.PlayerMarker;
import com.bytecubed.commons.models.Route;
import com.google.api.client.util.ArrayMap;
import org.bots4j.wit.WitClient;
import org.bots4j.wit.beans.GetIntentViaTextResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InstructionParser {

    private Logger logger = LoggerFactory.getLogger(InstructionParser.class);
    private WitClient client;

    @Autowired
    public InstructionParser(WitClient client){
        this.client = client;
    }

    public Route parse(String routeDescription) {
        GetIntentViaTextResponse response = client.getIntentViaText(routeDescription,null,null,null,null);
        ArrayMap distanceMap = (ArrayMap) ((List)response.getOutcomes().get(0).getEntities().get("distance")).get(0);
        double yards = Double.valueOf( distanceMap.get("value").toString());

        String playerTag = response.getOutcomes().get(0).getEntities().firstEntityValue("player_position");
        String route_type = response.getOutcomes().get(0).getEntities().firstEntityValue("route_type");

        return new Route(yards, Move.valueOf(route_type), playerTag);
    }

}
