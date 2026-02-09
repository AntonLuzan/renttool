package com.example.renttool.controller;

import com.example.renttool.model.*;
import com.example.renttool.repository.CartItemRepository;
import com.example.renttool.repository.RentalRepository;
import com.example.renttool.repository.ToolRepository;
import com.example.renttool.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ToolRepository toolRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public CartController(
            CartItemRepository cartItemRepository,
            ToolRepository toolRepository,
            RentalRepository rentalRepository,
            UserRepository userRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.toolRepository = toolRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<CartItem> cart = cartItemRepository.findByUser(user);

        BigDecimal total = cart.stream()
                .map(item -> {
                    long days = ChronoUnit.DAYS.between(
                            item.getStartDate(), item.getEndDate());
                    return item.getTool()
                            .getPricePerDay()
                            .multiply(BigDecimal.valueOf(days));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long toolId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication auth
    ) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Tool tool = toolRepository.findById(toolId).orElseThrow();

        cartItemRepository.findByUserAndTool(user, tool)
                .ifPresentOrElse(
                        item -> {
                            item.setStartDate(java.time.LocalDate.parse(startDate));
                            item.setEndDate(java.time.LocalDate.parse(endDate));
                            cartItemRepository.save(item);
                        },
                        () -> {
                            CartItem item = new CartItem(
                                    user,
                                    tool,
                                    java.time.LocalDate.parse(startDate),
                                    java.time.LocalDate.parse(endDate)
                            );
                            cartItemRepository.save(item);
                        }
                );

        return "redirect:/cart";
    }

    @Transactional
    @GetMapping("/remove/{toolId}")
    public String remove(@PathVariable Long toolId, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Tool tool = toolRepository.findById(toolId).orElseThrow();
        cartItemRepository.deleteByUserAndTool(user, tool);
        return "redirect:/cart";
    }

    @Transactional
    @GetMapping("/clear")
    public String clear(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        cartItemRepository.deleteByUser(user);
        return "redirect:/cart";
    }

    @Transactional
    @PostMapping("/checkout")
    public String checkout(Authentication auth, Model model) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<CartItem> cart = cartItemRepository.findByUser(user);

        if (cart.isEmpty()) {
            model.addAttribute("message", "Корзина пуста");
            model.addAttribute("rentals", List.of());
            model.addAttribute("total", BigDecimal.ZERO);
            return "rentals-success";
        }

        BigDecimal total = BigDecimal.ZERO;
        List<Rental> rentals = new ArrayList<>();

        for (CartItem item : cart) {
            long days = ChronoUnit.DAYS.between(
                    item.getStartDate(), item.getEndDate());

            BigDecimal price = item.getTool()
                    .getPricePerDay()
                    .multiply(BigDecimal.valueOf(days));

            Rental rental = new Rental();
            rental.setUser(user);
            rental.setTool(item.getTool());
            rental.setStartDate(item.getStartDate());
            rental.setEndDate(item.getEndDate());
            rental.setTotalPrice(price);

            rentalRepository.save(rental);
            rentals.add(rental);
            total = total.add(price);
        }

        cartItemRepository.deleteByUser(user);

        model.addAttribute("message", "✅ Аренда успешно оформлена!");
        model.addAttribute("rentals", rentals);
        model.addAttribute("total", total);

        return "rentals-success";
    }
}
