package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBOrganization")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {
    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allUCSBOrganization() {
        Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
        return orgs;
    }

    @Operation(summary= "Create a new UCSB organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postOrg(
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        throws JsonProcessingException {

        log.info("Detail: orgCode={}, orgTranslationShort={}, orgTranslation={}, inactive={}", orgCode, orgTranslationShort, orgTranslation, inactive);

        UCSBOrganization ucsborg = new UCSBOrganization();
        ucsborg.setOrgCode(orgCode);
        ucsborg.setOrgTranslationShort(orgTranslationShort);
        ucsborg.setOrgTranslation(orgTranslation);
        ucsborg.setInactive(inactive);

        UCSBOrganization savedOrg = ucsbOrganizationRepository.save(ucsborg);

        return savedOrg;
    }

    @Operation(summary= "Get UCSB organization by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization org = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return org;
    }

    @Operation(summary= "Update an existing UCSB organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateOrg(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {
        // log.info("incoming={}", incoming);
        // log.info("orgCode={}", orgCode);
        UCSBOrganization org = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        org.setOrgTranslationShort(incoming.getOrgTranslationShort());
        org.setOrgTranslation(incoming.getOrgTranslation());
        org.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(org);

        return org;
    }

    @Operation(summary= "Delete a UCSB organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrg(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization org = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        ucsbOrganizationRepository.delete(org);
        return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
    }
}