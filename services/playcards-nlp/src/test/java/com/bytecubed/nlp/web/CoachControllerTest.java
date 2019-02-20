package com.bytecubed.nlp.web;

import com.bytecubed.commons.Formation;
import com.bytecubed.commons.PlayCard;
import com.bytecubed.commons.models.Placement;
import com.bytecubed.commons.models.PlayerMarker;
import com.bytecubed.commons.models.movement.CustomMoveDescriptor;
import com.bytecubed.commons.models.movement.CustomRoute;
import com.bytecubed.nlp.models.PlayCardInstruction;
import com.bytecubed.nlp.models.RouteInstruction;
import com.bytecubed.nlp.parsing.InstructionParser;
import com.bytecubed.nlp.repository.FormationRepository;
import com.bytecubed.nlp.repository.PlayRepository;
import com.bytecubed.nlp.repository.RouteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CoachControllerTest {

    public static final String TAG = "X";
    public static final String PLAY_NAME = "ByteCubed";
    private PlayRepository playRepository;
    private InstructionParser instructionParser;
    private FormationRepository formationRepository;
    private RouteRepository routeRepository;
    private UUID TEST_FORMATION_ID = randomUUID();
    private UUID TEST_ROUTE_ID = randomUUID();
    private Formation formation;

    @Before
    public void setup() {
        playRepository = mock(PlayRepository.class);
        instructionParser = mock(InstructionParser.class);
        formationRepository = formationRepository();
        routeRepository = routeRepository();
    }

    @Test
    public void shouldReturnNewPlayCardBasedOnOnePlayerOneRoute() {
        CoachController controller = new CoachController(
                playRepository,
                instructionParser,
                formationRepository,
                routeRepository);


        PlayCardInstruction instruction = new PlayCardInstruction(TEST_FORMATION_ID,
                asList(new RouteInstruction(TAG, TEST_ROUTE_ID)));

        PlayCard card = controller.postScript(instruction).getBody();
        System.out.println(card.getId());

        Formation formation = card.getFormation();
        assertThat(formation.getPlayerMarkers()).hasSize(1);

        PlayerMarker playerMarker = formation.getPlayerMarkers().get(0);
        assertThat(playerMarker.getRoutes()).hasSize(1);

        CustomMoveDescriptor movements = (CustomMoveDescriptor) playerMarker.getRoutes().get(0).getMoveDescriptors().get(0);
        assertThat(movements.getStart()).isEqualTo(new Placement(10d, 10d));
        assertThat(movements.getEnd()).isEqualTo(new Placement(10d, 05d));
    }

    @Test
    @Ignore
    public void shouldGenerateWithIFormation() {
        UUID iFormationId = UUID.fromString("2a6009a0-b66d-440a-b141-7cb64054e217");
        UUID routeID = UUID.fromString("d2c38c13-ece6-40fe-acab-f72aaacf737b");

        PlayCardInstruction instruction = new PlayCardInstruction(iFormationId,
                asList(new RouteInstruction("X", routeID)));

        RestTemplate template = new RestTemplate();
        String response = template.postForEntity("http://localhost:8080/coach/playcards/script", instruction, String.class).getBody();

        System.out.println( "Response:  " + response );
    }

    @Test
    @Ignore
    public void shouldSaveRealRoute(){
        CustomRoute route = new CustomRoute(asList(new CustomMoveDescriptor(
                new Placement(0, 0),
                new Placement(0, -5))), "X");

        RestTemplate template = new RestTemplate();
        String response = template.postForEntity( "http://localhost:8080/coach/routes", route, String.class).getBody();
        System.out.println( response );
    }

    @Test
    public void shouldGenerateAndSaveRoute() throws Exception {
        CustomRoute route = new CustomRoute(asList(new CustomMoveDescriptor(
                new Placement(0, 0),
                new Placement(0, -5))), "X");

        MockMvc controller = MockMvcBuilders.standaloneSetup(
                new CoachController(playRepository, instructionParser,
                        formationRepository, routeRepository)).build();
        controller.perform(
                post("/coach/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(route)))
                .andExpect(status().isOk());

        ArgumentCaptor<CustomRoute> savedRouteCaptor = ArgumentCaptor.forClass(CustomRoute.class);
        verify(routeRepository).save(savedRouteCaptor.capture());
        CustomRoute savedRoute = savedRouteCaptor.getValue();

        CustomMoveDescriptor descriptor = (CustomMoveDescriptor) savedRoute.getMoveDescriptors().get(0);
        assertThat(descriptor.getStart()).isEqualTo(((CustomMoveDescriptor) route.getMoveDescriptors().get(0)).getStart());
        assertThat(descriptor.getEnd()).isEqualTo(((CustomMoveDescriptor) route.getMoveDescriptors().get(0)).getEnd());
    }

    private FormationRepository formationRepository() {
        FormationRepository formationRepository = mock(FormationRepository.class);
        formation = new Formation(TEST_FORMATION_ID, PLAY_NAME, asList(new PlayerMarker(
                new Placement(10, 10), "wr", TAG
        )));

        when(formationRepository.findById(TEST_FORMATION_ID))
                .thenReturn(Optional.of(formation));

        return formationRepository;
    }

    private RouteRepository routeRepository() {
        RouteRepository routeRepository = mock(RouteRepository.class);
        CustomRoute customRoute = new CustomRoute(asList(
                new CustomMoveDescriptor(new Placement(0d, 0d),
                        new Placement(0d, -5d))
        ), TAG);

        when(routeRepository.findById(TEST_ROUTE_ID)).thenReturn(Optional.of(customRoute));

        return routeRepository;
    }

}