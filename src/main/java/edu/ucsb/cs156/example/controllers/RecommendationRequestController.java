package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;

@Tag(name = "RecommendationRequest")
@RequestMapping("/api/RecommendationRequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    @Operation(summary= "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
        return requests;
    }

    @Operation(summary= "Get a single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        return recommendationRequest;
    }

    @Operation(summary= "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="professorEmail") @RequestParam String professorEmail,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="dateRequested", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)", example="2023-12-01T13:15") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name="dateNeeded", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)", example="2023-12-01T13:15") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
            @Parameter(name="done") @RequestParam Boolean done)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateRequested={}", dateRequested);
        log.info("dateNeeded={}", dateNeeded);

        RecommendationRequest recommendationRequest = new RecommendationRequest();
        recommendationRequest.setRequesterEmail(requesterEmail);
        recommendationRequest.setProfessorEmail(professorEmail);
        recommendationRequest.setExplanation(explanation);
        recommendationRequest.setDateRequested(dateRequested);
        recommendationRequest.setDateNeeded(dateNeeded);
        recommendationRequest.setDone(done);

        RecommendationRequest savedRecommendationRequest = recommendationRequestRepository.save(recommendationRequest);

        return savedRecommendationRequest;
    }

    // @Operation(summary= "Delete a UCSBDate")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteUCSBDate(
    //         @Parameter(name="id") @RequestParam Long id) {
    //     UCSBDate ucsbDate = ucsbDateRepository.findById(id)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

    //     ucsbDateRepository.delete(ucsbDate);
    //     return genericMessage("UCSBDate with id %s deleted".formatted(id));
    // }

    // @Operation(summary= "Update a single date")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @PutMapping("")
    // public UCSBDate updateUCSBDate(
    //         @Parameter(name="id") @RequestParam Long id,
    //         @RequestBody @Valid UCSBDate incoming) {

    //     UCSBDate ucsbDate = ucsbDateRepository.findById(id)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDate.class, id));

    //     ucsbDate.setQuarterYYYYQ(incoming.getQuarterYYYYQ());
    //     ucsbDate.setName(incoming.getName());
    //     ucsbDate.setLocalDateTime(incoming.getLocalDateTime());

    //     ucsbDateRepository.save(ucsbDate);

    //     return ucsbDate;
    // }
}
