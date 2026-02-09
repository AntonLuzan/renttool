package com.example.renttool.controller;

import com.example.renttool.model.Role;
import com.example.renttool.model.Rental;
import com.example.renttool.model.Tool;
import com.example.renttool.model.User;
import com.example.renttool.repository.RentalRepository;
import com.example.renttool.repository.ToolRepository;
import com.example.renttool.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    public AdminController(
            ToolRepository toolRepository,
            UserRepository userRepository,
            RentalRepository rentalRepository
    ) {
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
    }


    @InitBinder("tool")
    protected void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("imageData", "imageType");
    }


    @GetMapping("/rentals")
    public String rentalsList(
            @RequestParam(required = false) String filter,
            Model model
    ) {
        LocalDate today = LocalDate.now();
        List<Rental> rentals;

        if ("active".equals(filter)) {
            rentals = rentalRepository.findAll().stream()
                    .filter(r -> !r.isCancelled() && !r.getEndDate().isBefore(today))
                    .toList();
        }
        else if ("ended".equals(filter)) {
            rentals = rentalRepository.findAll().stream()
                    .filter(r ->
                            !r.isCancelled()
                                    && r.getEndDate().isBefore(today)
                    )
                    .toList();
        }

        else if ("cancelled".equals(filter)) {
            rentals = rentalRepository.findAll().stream()
                    .filter(Rental::isCancelled)
                    .toList();
        }
        else {
            rentals = rentalRepository.findAllWithDetails();
        }

        model.addAttribute("rentals", rentals);
        model.addAttribute("filter", filter);

        return "admin/rentals";
    }



    @PostMapping("/rentals/cancel/{id}")
    public String cancelRental(@PathVariable Long id) {
        rentalRepository.cancelRental(id);
        return "redirect:/admin/rentals";
    }

    @GetMapping("/tools")
    public String listTools(Model model) {
        model.addAttribute("tools", toolRepository.findAll());
        return "admin/tools";
    }

    @GetMapping("/tools/add")
    public String addToolForm(Model model) {
        model.addAttribute("tool", new Tool());
        return "admin/add-tool";
    }

    @GetMapping("/tools/edit/{id}")
    public String editToolForm(@PathVariable Long id, Model model) {
        return toolRepository.findById(id)
                .map(tool -> {
                    model.addAttribute("tool", tool);
                    return "admin/add-tool";
                })
                .orElse("redirect:/admin/tools");
    }

    @PostMapping("/tools/save")
    public String saveTool(
            @ModelAttribute("tool") Tool tool,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {

        if (imageFile != null && !imageFile.isEmpty()) {
            tool.setImageData(imageFile.getBytes());
            tool.setImageType(imageFile.getContentType());
        } else if (tool.getId() != null) {
            toolRepository.findById(tool.getId()).ifPresent(existing -> {
                tool.setImageData(existing.getImageData());
                tool.setImageType(existing.getImageType());
            });
        }

        if (tool.getAvailable() == null) tool.setAvailable(true);
        if (tool.getPricePerDay() == null) tool.setPricePerDay(BigDecimal.ZERO);

        toolRepository.save(tool);
        return "redirect:/admin/tools";
    }

    @GetMapping("/tools/delete/{id}")
    public String deleteTool(@PathVariable Long id) {
        toolRepository.findById(id).ifPresent(toolRepository::delete);
        return "redirect:/admin/tools";
    }

    @GetMapping("/tools/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id) {
        toolRepository.findById(id).ifPresent(tool -> {
            tool.setAvailable(!Boolean.TRUE.equals(tool.getAvailable()));
            toolRepository.save(tool);
        });
        return "redirect:/admin/tools";
    }

    @GetMapping("/tools/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getToolImage(@PathVariable Long id) {
        return toolRepository.findById(id)
                .filter(t -> t.getImageData() != null)
                .map(t -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,
                                t.getImageType() != null ? t.getImageType() : MediaType.IMAGE_JPEG_VALUE)
                        .body(t.getImageData()))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        return userRepository.findById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("roles", Role.values());
                    return "admin/edit-user";
                })
                .orElse("redirect:/admin/users");
    }

    @PostMapping("/users/save")
    public String saveUser(
            @Valid @ModelAttribute("user") User updatedUser,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/edit-user";
        }

        userRepository.findById(updatedUser.getId()).ifPresent(existing -> {
            existing.setUsername(updatedUser.getUsername());
            existing.setEmail(updatedUser.getEmail());
            existing.setPhone(updatedUser.getPhone());
            existing.setRole(updatedUser.getRole());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                existing.setPassword(updatedUser.getPassword());
            }

            userRepository.save(existing);
        });

        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return "redirect:/admin/users";
    }

}
