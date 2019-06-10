package com.videotranscripts.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.videotranscripts.exception.AuthException;
import com.videotranscripts.exception.GenericException;

/**
 * @author amitb
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(GenericException.class)
	public String handleException(GenericException e, Model model){
		model.addAttribute("error", e.getFailureResponse().getErrorMessage());
		return "error";
	}
	
	@ExceptionHandler(AuthException.class)
	public String handleAuthException(AuthException e, RedirectAttributes attr){
		attr.addFlashAttribute("error", e.getFailureResponse().getErrorMessage());
		return "redirect:/";
	}

}
