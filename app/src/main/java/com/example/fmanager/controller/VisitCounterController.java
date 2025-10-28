package com.example.fmanager.controller;

import com.example.fmanager.service.VisitCounterService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    @Autowired
    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getVisitCount(@RequestParam String url) {
        int count = visitCounterService.getVisitCount(url);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Integer>> getTotalVisitCount() {
        Map<String, Integer> totalCount = new HashMap<>();
        totalCount.put("total", visitCounterService.getTotalVisitCount());
        return ResponseEntity.ok(totalCount);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Integer>> getAllVisitCounts() {
        Map<String, Integer> allCounts = visitCounterService.getAllVisitCounts();
        return ResponseEntity.ok(allCounts);
    }
}