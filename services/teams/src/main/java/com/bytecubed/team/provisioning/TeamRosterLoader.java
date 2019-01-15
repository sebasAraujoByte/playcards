package com.bytecubed.team.provisioning;

import com.bytecubed.commons.models.Player;
import com.bytecubed.team.repository.TeamRegistry;
import com.bytecubed.team.repository.TeamRepository;
import com.bytecubed.team.web.NFLResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamRosterLoader {

    private TeamRepository teamRepository;
    private Logger logger = LoggerFactory.getLogger(TeamRosterLoader.class);

    public TeamRosterLoader(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Player> load() {

        NFLResponse response = new RestTemplate().getForObject("http://api.fantasy.nfl.com/v1/players/stats?statType=seasonStats&season=2018&week=1&format=json", NFLResponse.class);
        logger.debug( "Player Response size:  " + response.getPlayers().size());

        List<Player> players = response.getPlayers().stream()
                .map(n->n.toPlayer(new TeamRegistry()))
                .collect(Collectors.toList());

        players.forEach(p->teamRepository.save(p.getTeam()));

        return players;
    }

}
