package com.example.renttool.controller;

import com.example.renttool.model.Tool;
import com.example.renttool.repository.ToolRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class ToolController {

    private final ToolRepository toolRepository;

    public ToolController(ToolRepository toolRepository) {
        this.toolRepository = toolRepository;
    }

    @GetMapping("/")
    public String listTools(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        List<Tool> tools = toolRepository.findAll();


        if (search != null && !search.isEmpty()) {
            tools = tools.stream()
                    .filter(t -> t.getName() != null &&
                            t.getName().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        if ("priceAsc".equals(sort)) {
            tools = tools.stream()
                    .sorted(Comparator.comparing(Tool::getPricePerDay))
                    .toList();
        } else if ("priceDesc".equals(sort)) {
            tools = tools.stream()
                    .sorted(Comparator.comparing(Tool::getPricePerDay).reversed())
                    .toList();
        }

        model.addAttribute("tools", tools);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "tools";
    }


    @GetMapping("/admin/tools/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tool", new Tool());
        return "add-tool";
    }

    @GetMapping("/tools/image/{id}")
    public ResponseEntity<byte[]> getToolImage(@PathVariable Long id) {
        Optional<Tool> toolOpt = toolRepository.findById(id);

        if (toolOpt.isEmpty() || toolOpt.get().getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        Tool tool = toolOpt.get();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        tool.getImageType() != null ? tool.getImageType() : MediaType.IMAGE_JPEG_VALUE)
                .body(tool.getImageData());
    }

    @GetMapping("/tool/{id}")
    public String toolDetails(@PathVariable Long id, Model model) {
        return toolRepository.findById(id)
                .map(tool -> {
                    model.addAttribute("tool", tool);
                    return "tool-details";
                })
                .orElse("redirect:/");
    }
    @GetMapping("/about")
    public String about() {
        return "about";
    }

}
