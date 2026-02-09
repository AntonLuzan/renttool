package com.example.renttool.controller;

import com.example.renttool.model.Rental;
import com.example.renttool.model.Tool;
import com.example.renttool.model.User;
import com.example.renttool.repository.RentalRepository;
import com.example.renttool.repository.ToolRepository;
import com.example.renttool.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("/rental")
public class RentalController {

    private final RentalRepository rentalRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;

    public RentalController(
            RentalRepository rentalRepository,
            ToolRepository toolRepository,
            UserRepository userRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
    }


    @GetMapping("/my")
    public String myRentals(
            @RequestParam(required = false, defaultValue = "all") String filter,
            Authentication auth,
            Model model
    ) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();

        List<Rental> rentals;

        LocalDate today = LocalDate.now();

        switch (filter) {
            case "active" ->
                    rentals = rentalRepository.findByUserAndEndDateGreaterThanEqualAndCancelledFalseOrderByStartDateDesc(
                            user, today
                    );
            case "ended" ->
                    rentals = rentalRepository.findByUserAndEndDateLessThanOrCancelledTrueOrderByStartDateDesc(
                            user, today
                    );
            default ->
                    rentals = rentalRepository.findByUserOrderByStartDateDesc(user);
        }

        model.addAttribute("rentals", rentals);
        model.addAttribute("filter", filter);

        return "my-rentals";
    }


    @PostMapping("/cancel/{id}")
    public String cancelRental(
            @PathVariable Long id,
            Authentication auth
    ) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        if (!rental.getUser().getId().equals(user.getId())) {
            return "redirect:/rental/my?error=forbidden";
        }

        rentalRepository.cancelRental(id);

        return "redirect:/rental/my?filter=active";
    }


    @GetMapping("/new/{toolId}")
    public String newRentalForm(@PathVariable Long toolId, Model model) {

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        LocalDate today = LocalDate.now();

        List<Rental> busyRentals =
                rentalRepository.findByToolAndEndDateGreaterThanEqual(tool, today);

        model.addAttribute("tool", tool);
        model.addAttribute("today", today);
        model.addAttribute("busyRentals", busyRentals);

        return "rental-form";
    }

    @PostMapping("/new")
    public String saveRental(
            @RequestParam Long toolId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication
    ) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        if (!end.isAfter(start)) {
            return "redirect:/rental/new/" + toolId + "?error=invalid_dates";
        }

        boolean isAvailable = rentalRepository
                .findByToolAndStartDateLessThanEqualAndEndDateGreaterThanEqual(tool, end, start)
                .isEmpty();

        if (!isAvailable) {
            return "redirect:/rental/new/" + toolId + "?error=tool_unavailable";
        }

        long days = ChronoUnit.DAYS.between(start, end);
        BigDecimal totalPrice = tool.getPricePerDay()
                .multiply(BigDecimal.valueOf(days));

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setTool(tool);
        rental.setStartDate(start);
        rental.setEndDate(end);
        rental.setTotalPrice(totalPrice);
        rental.setCancelled(false);

        rentalRepository.save(rental);

        return "redirect:/rental/success?rentalId=" + rental.getId();
    }
}
