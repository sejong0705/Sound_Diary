package com.sj.sound_diary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
	@GetMapping("/app")
	public String appShell() {
	    return "app-shell";
	}
}
