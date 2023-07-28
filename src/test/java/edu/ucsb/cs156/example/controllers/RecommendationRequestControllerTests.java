package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository recommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdates() throws Exception {

                // arrange
                LocalDateTime dateRequested1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime dateNeeded1 = LocalDateTime.parse("2022-01-05T11:59:59");

                RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("amateur@ucsb.edu")
                                .professorEmail("guru@ucsb.edu")
                                .explanation("teach me your ways, master")
                                .dateRequested(dateRequested1)
                                .dateNeeded(dateNeeded1)
                                .done(false)
                                .build();

                LocalDateTime dateRequested2 = LocalDateTime.parse("2022-02-04T01:01:01");
                LocalDateTime dateNeeded2 = LocalDateTime.parse("2022-02-05T11:59:59");

                RecommendationRequest recommendationRequest2 = RecommendationRequest.builder()
                                .requesterEmail("elden_ring_fan@gmail.com")
                                .professorEmail("miyazaki@fromsoft.somewhere")
                                .explanation("when DLC???")
                                .dateRequested(dateRequested2)
                                .dateNeeded(dateNeeded2)
                                .done(true)
                                .build();

                ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

                when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
                // arrange

                LocalDateTime dateRequested1 = LocalDateTime.parse("2022-02-04T01:01:01");
                LocalDateTime dateNeeded1 = LocalDateTime.parse("2022-03-05T11:59:59");



                RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("me@gmail.com")
                                .professorEmail("jacoco@compile.please")
                                .explanation("no_spaces_no_caps_you_like_it_huh")
                                .dateRequested(dateRequested1)
                                .dateNeeded(dateNeeded1)
                                .done(true)
                                .build();
                
                
                when(recommendationRequestRepository.save(eq(recommendationRequest1))).thenReturn(recommendationRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequest/post?requesterEmail=me@gmail.com&professorEmail=jacoco@compile.please&explanation=no_spaces_no_caps_you_like_it_huh&dateRequested=2022-02-04T01:01:01&dateNeeded=2022-03-05T11:59:59&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).save(recommendationRequest1);
                String expectedJson = mapper.writeValueAsString(recommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}