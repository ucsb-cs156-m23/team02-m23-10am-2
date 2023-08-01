package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)

public class UCSBOrganizationControllerTests extends ControllerTestCase{

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;
    // Authorization tests for /api/UCSBOrganization/admin/all
    // GET_ALL tests
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().is(200));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

        // arrange
        
        UCSBOrganization zpr = UCSBOrganization.builder()
                .orgCode("ZPR")
                .orgTranslationShort("ZETA PHI RHO")
                .orgTranslation("ZETA PHI RHO")
                .inactive(false)
                .build();
                
        UCSBOrganization sky = UCSBOrganization.builder()
                .orgCode("SKY")
                .orgTranslationShort("SKYDIVING CLUB")
                .orgTranslation("SKYDIVING CLUB AT UCSB")
                .inactive(false)
                .build();


        ArrayList<UCSBOrganization> expectedOrg = new ArrayList<>();
        expectedOrg.addAll(Arrays.asList(zpr, sky));

        when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrg);

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrg);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


    // Authorization tests for /api/UCSBOrganization/post
    // (Perhaps should also have these for put and delete)
    // POST tests

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBOrganization/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBOrganization/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_org() throws Exception {
        // arrange

        UCSBOrganization tasa = UCSBOrganization.builder()        
                .orgCode("5")
                .orgTranslationShort("TASA")
                .orgTranslation("TASA Club")
                .inactive(true)
                .build();


        when(ucsbOrganizationRepository.save(eq(tasa))).thenReturn(tasa);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/UCSBOrganization/post?orgCode=5&orgTranslationShort=TASA&orgTranslation=TASA Club&inactive=true")
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(tasa);
        String expectedJson = mapper.writeValueAsString(tasa);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


    // GET_SINGLE TESTS

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization?orgCode=1"))
                .andExpect(status().is(403));
    } 

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        UCSBOrganization sky = UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

        when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=SKY"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq("SKY"));
        String expectedJson = mapper.writeValueAsString(sky);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange
        when(ucsbOrganizationRepository.findById(eq("smash-bros"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=smash-bros"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq("smash-bros"));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBOrganization with id smash-bros not found", json.get("message"));
    }


    // PUT TESTS

    @Test
    public void logged_out_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBOrganization"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBOrganization"))
                .andExpect(status().is(403)); // only admins can post
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_org() throws Exception {
        // arrange

        UCSBOrganization org1 = UCSBOrganization.builder()
                .orgCode("01")
                .orgTranslationShort("A")
                .orgTranslation("Apples")
                .inactive(true)
                .build();

        UCSBOrganization org2 = UCSBOrganization.builder()
                .orgCode("01")
                .orgTranslationShort("B")
                .orgTranslation("Bananas")
                .inactive(false)
                .build();

        String requestBody = mapper.writeValueAsString(org2);

        when(ucsbOrganizationRepository.findById(eq("01"))).thenReturn(Optional.of(org1));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/UCSBOrganization?orgCode=01")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("01");
        verify(ucsbOrganizationRepository, times(1)).save(org2); // should be saved with updated info
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_orgs_that_does_not_exist() throws Exception {
        // arrange

        UCSBOrganization obj = UCSBOrganization.builder()
                .orgCode("USA")
                .orgTranslationShort("Uncle Sam")
                .orgTranslation("Uncle Same")
                .inactive(false)
                .build();


        String requestBody = mapper.writeValueAsString(obj);

        when(ucsbOrganizationRepository.findById(eq("USA"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/UCSBOrganization?orgCode=USA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("USA");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id USA not found", json.get("message"));

    }


    // DELETE TESTS

    @Test
    public void logged_out_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBOrganization?orgCode=255"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBOrganization?orgCode=5"))
                .andExpect(status().is(403)); 
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_org() throws Exception {
        // arrange

        UCSBOrganization obj1 = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("CSU")
                .orgTranslation("Chinese Student Union")
                .inactive(false)
                .build();

        when(ucsbOrganizationRepository.findById(eq("1"))).thenReturn(Optional.of(obj1));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/UCSBOrganization?orgCode=1")
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("1");
        verify(ucsbOrganizationRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("record 1 deleted", json.get("message"));
    }



    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_orgs_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(ucsbOrganizationRepository.findById(eq("pickleball"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/UCSBOrganization?orgCode=pickleball")
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("pickleball");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id pickleball not found", json.get("message"));
    }

}   