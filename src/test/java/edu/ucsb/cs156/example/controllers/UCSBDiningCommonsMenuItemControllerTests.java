package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

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


@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

        @MockBean
        UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;
        
        @MockBean
        UserRepository UserRepository;

        // Authorization tests for /api/ucsbdiningcommons/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().is(200)); //logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=7"))
                .andExpect(status().is(403)); // logged out users can't get by id
        }


        // Authorization tests for /api/ucsbdiningcommonsmenuitem/post
        // (Perhaps should also have these for put and delete)
        @Test
        public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                .andExpect(status().is(403));
        }

        

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
                .andExpect(status().is(403));
        }


      // Tests with mocks for database actions
        @WithMockUser(roles = {"USER"})
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
            //arrange
            UCSBDiningCommonsMenuItem commonsMenuItem = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Baked Pesto Pasta with Chicken")
                .station("Entree Specials")
                .build();

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(commonsMenuItem));
            //act
            MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=7"))
                .andExpect(status().isOk())
                .andReturn();
            //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
            String expectedJSON = mapper.writeValueAsString(commonsMenuItem);
            String responseJSON = response.getResponse().getContentAsString();
            assertEquals(expectedJSON, responseJSON);
        }


        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
            //arrange
            when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.empty());
            //act
            MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem?id=7"))
                .andExpect(status().isNotFound())
                .andReturn();
            //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBDiningCommonsMenuItem with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommonsmenuitems() throws Exception {
            //arrange
            //LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
            UCSBDiningCommonsMenuItem commonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Baked Pesto Pasta with Chicken")
                .station("Entree Specials")
                .build();

            UCSBDiningCommonsMenuItem commonsMenuItem2 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Chicken Caesar Salad")
                .station("Entrees")
                .build();
            
            ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
            expectedItems.addAll(Arrays.asList(commonsMenuItem1, commonsMenuItem2));

            when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

            //act
            MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
                .andExpect(status().isOk())
                .andReturn();

                //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
            String expectedJSON = mapper.writeValueAsString(expectedItems);
            String responseJSON = response.getResponse().getContentAsString();
            assertEquals(expectedJSON, responseJSON);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_item() throws Exception {

            //arrange
            UCSBDiningCommonsMenuItem commonsMenuItem = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Cheese")
                .station("Entrees")
                .build();
            
            when(ucsbDiningCommonsMenuItemRepository.save(eq(commonsMenuItem))).thenReturn(commonsMenuItem);
            //act
            MvcResult response = mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post?diningCommonsCode=ortega&name=Cheese&station=Entrees")
                                                    .with(csrf()))
                                                    .andExpect(status().isOk()).andReturn();


            //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(commonsMenuItem);
            String expectedJSON = mapper.writeValueAsString(commonsMenuItem);
            String responseJSON = response.getResponse().getContentAsString();
            assertEquals(expectedJSON, responseJSON);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_item() throws Exception{
            //arrange
            UCSBDiningCommonsMenuItem commonsMenuItem = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Cheese")
                .station("Entrees")
                .build();

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(commonsMenuItem));

            //act
            MvcResult response = mockMvc.perform(
                                delete("/api/UCSBDiningCommonsMenuItem?id=7")
                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();


            //aassert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDiningCommonsMenuItem with id 7 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_item_and_gets_right_error_message() throws Exception{
                //arrange
            when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());
            //ACT
            MvcResult response = mockMvc.perform(
                            delete("/api/UCSBDiningCommonsMenuItem?id=15")
                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();
            //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_item() throws Exception{
            //arrange
            UCSBDiningCommonsMenuItem commonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("ortega")
                .name("Cheese")
                .station("Entrees")
                .build();

            UCSBDiningCommonsMenuItem commonsMenuItem2 = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("portola")
                .name("Cake")
                .station("Dessert")
                .build();

            String requestBody = mapper.writeValueAsString(commonsMenuItem2);

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L))).thenReturn(Optional.of(commonsMenuItem1));
            
            //act
            MvcResult response = mockMvc.perform(
                put("/api/UCSBDiningCommonsMenuItem?id=67")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(requestBody)
                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();
            
            //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(67L));
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(commonsMenuItem2);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_item_that_does_not_exist() throws Exception {
            //arrange
            UCSBDiningCommonsMenuItem commonsMenuItem = UCSBDiningCommonsMenuItem.builder()
                .diningCommonsCode("portola")
                .name("Cake")
                .station("Dessert")
                .build();

            String requestBody = mapper.writeValueAsString(commonsMenuItem);

            when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L))).thenReturn(Optional.empty());
            
            //act
            MvcResult response = mockMvc.perform(
                put("/api/UCSBDiningCommonsMenuItem?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
                    .andExpect(status().isNotFound()).andReturn();


                    //assert
            verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(67L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
        }
        


}
