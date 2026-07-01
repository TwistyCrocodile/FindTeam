package com.findteam.findteam.controller;

import com.findteam.findteam.catalog.TechnologyCatalog;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technologies")
public class TechnologyController {

	@GetMapping
	public ResponseEntity<List<String>> getTechnologies() {
		return ResponseEntity.ok(TechnologyCatalog.TECHNOLOGIES);
	}
}
