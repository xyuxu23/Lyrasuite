package com.xyuxu.javasec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/gadget";
    }

    @GetMapping("/gadget")
    public String gadget(Model model) {
        model.addAttribute("activeModule", "gadget");
        model.addAttribute("pageTitle", "Gadget Generator");
        return "pages/gadget";
    }

    @GetMapping("/jndi")
    public String jndi(Model model) {
        model.addAttribute("activeModule", "jndi");
        model.addAttribute("pageTitle", "JNDI Service Control");
        return "pages/jndi";
    }

    @GetMapping("/fastjson")
    public String fastjson(Model model) {
        model.addAttribute("activeModule", "fastjson");
        model.addAttribute("pageTitle", "Fastjson Exploitation");
        return "pages/fastjson";
    }

    @GetMapping("/shiro")
    public String shiro(Model model) {
        model.addAttribute("activeModule", "shiro");
        model.addAttribute("pageTitle", "Shiro Exploitation");
        return "pages/shiro";
    }

    @GetMapping("/memshell")
    public String memshell(Model model) {
        model.addAttribute("activeModule", "memshell");
        model.addAttribute("pageTitle", "Memory Shell");
        return "pages/memshell";
    }

    @GetMapping("/codec")
    public String codec(Model model) {
        model.addAttribute("activeModule", "codec");
        model.addAttribute("pageTitle", "Codec Tools");
        return "pages/codec";
    }

    @GetMapping("/snakeyaml")
    public String snakeyaml(Model model) {
        model.addAttribute("activeModule", "snakeyaml");
        model.addAttribute("pageTitle", "SnakeYAML Exploitation ");
        return "pages/snakeyaml";
    }
}