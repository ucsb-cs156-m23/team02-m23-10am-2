package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import javax.validation.Valid;


import java.time.LocalDateTime;

@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @Operation(summary= "List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allReviews() {
        Iterable<MenuItemReview> reviews = menuItemReviewRepository.findAll();
        return reviews;
    }
    
    @Operation(summary= "Get a single review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @Parameter(name="id") @RequestParam Long id) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    @Operation(summary= "Create a new item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMenuItemReview(
        @Parameter(name="itemId") @RequestParam Long itemId,
        @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
        @Parameter(name="stars") @RequestParam int stars,
        @Parameter(name="dateReviewed") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
        @Parameter(name="comments") @RequestParam String comments
    ) throws JsonProcessingException {
        log.info("localDateTime={}", dateReviewed);

        MenuItemReview menuItemReview = new MenuItemReview();
        menuItemReview.setItemId(itemId);
        menuItemReview.setReviewerEmail(reviewerEmail);
        menuItemReview.setStars(stars);
        menuItemReview.setComments(comments);
        menuItemReview.setDateReviewed(dateReviewed);

       MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);
       return savedMenuItemReview;
    }
    @Operation(summary= "Delete a review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMenuItemReview(
            @Parameter(name="id") @RequestParam Long id) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReviewRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateMenuItemReview(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid MenuItemReview incoming) {

        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReview.setItemId(incoming.getItemId());
        menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
        menuItemReview.setStars(incoming.getStars());
        menuItemReview.setDateReviewed(incoming.getDateReviewed());
        menuItemReview.setComments(incoming.getComments());

        menuItemReviewRepository.save(menuItemReview);

        return menuItemReview;
    }
}