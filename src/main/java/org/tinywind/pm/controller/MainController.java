package org.tinywind.pm.controller;

import org.tinywind.pm.model.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired
	private PersonRepository personRepository;

	@RequestMapping("/hello")
	public String index(Model model) {
		model.addAttribute("name", "tinywind");
		return "hello";
	}
}