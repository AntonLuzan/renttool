package com.example.renttool.controller;

import com.example.renttool.model.Rental;
import com.example.renttool.model.User;
import com.example.renttool.repository.RentalRepository;
import com.example.renttool.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MyRentalsController {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public MyRentalsController(
            RentalRepository rentalRepository,
            UserRepository userRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-rentals")
    public String myRentals(Authentication authentication, Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow();

        List<Rental> rentals =
                rentalRepository.findByUserOrderByStartDateDesc(user);


        model.addAttribute("rentals", rentals);

        return "my-rentals";
    }
}
