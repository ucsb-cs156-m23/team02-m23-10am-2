package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

        // Authorization tests for /api/RecommendationRequest/admin/all

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
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest?id=123"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/RecommendationRequest/post

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

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

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

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.of(recommendationRequest1));

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=123"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findById(eq(123L));
                String expectedJson = mapper.writeValueAsString(recommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=123"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findById(eq(123L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendation_requests() throws Exception {

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
        public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
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

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
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

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.of(recommendationRequest1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=123")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(123L);
                verify(recommendationRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 123 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendation_request_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/RecommendationRequest?id=123")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 123 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recommendation_request() throws Exception {
                // arrange

                LocalDateTime dateRequested1 = LocalDateTime.parse("2022-02-04T01:01:01");
                LocalDateTime dateNeeded1 = LocalDateTime.parse("2022-03-05T11:59:59");
                LocalDateTime dateRequested2 = LocalDateTime.parse("2023-02-04T01:01:01");
                LocalDateTime dateNeeded2 = LocalDateTime.parse("2023-03-05T11:59:59");

                RecommendationRequest recommendationRequestOrig = RecommendationRequest.builder()
                                .requesterEmail("me@gmail.com")
                                .professorEmail("jacoco@compile.please")
                                .explanation("no_spaces_no_caps_you_like_it_huh")
                                .dateRequested(dateRequested1)
                                .dateNeeded(dateNeeded1)
                                .done(false)
                                .build();

                RecommendationRequest recommendationRequestEdited = RecommendationRequest.builder()
                                .requesterEmail("someoneelse@gmail.com")
                                .professorEmail("anotheroneelse@gmail.com")
                                .explanation("hello_world")
                                .dateRequested(dateRequested2)
                                .dateNeeded(dateNeeded2)
                                .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.of(recommendationRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(123L);
                verify(recommendationRequestRepository, times(1)).save(recommendationRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendation_request_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime dateRequested1 = LocalDateTime.parse("2022-02-04T01:01:01");
                LocalDateTime dateNeeded1 = LocalDateTime.parse("2022-03-05T11:59:59");

                RecommendationRequest recommendationEditedRequest = RecommendationRequest.builder()
                                .requesterEmail("me@gmail.com")
                                .professorEmail("jacoco@compile.please")
                                .explanation("no_spaces_no_caps_you_like_it_huh")
                                .dateRequested(dateRequested1)
                                .dateNeeded(dateNeeded1)
                                .done(false)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationEditedRequest);

                when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/RecommendationRequest?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRequestRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("RecommendationRequest with id 123 not found", json.get("message"));
        }
}
