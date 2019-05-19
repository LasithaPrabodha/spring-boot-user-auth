package com.sliit.user.controller;

import com.sliit.user.exception.ResourceNotFoundException;
import com.sliit.user.model.User;
import com.sliit.user.payload.*;
import com.sliit.user.repository.UserRepository;
import com.sliit.user.security.UserPrincipal;
import com.sliit.user.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/user/me")
	@PreAuthorize("hasRole('USER')")
	public UserProfile getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		User user = userRepository.findByUsername(currentUser.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUser.getUsername()));

		UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(),
				user.getCreatedAt());

		return userProfile;
	}

	@GetMapping("/user/checkUsernameAvailability")
	public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
		Boolean isAvailable = !userRepository.existsByUsername(username);
		return new UserIdentityAvailability(isAvailable);
	}

	@GetMapping("/user/checkEmailAvailability")
	public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
		Boolean isAvailable = !userRepository.existsByEmail(email);
		return new UserIdentityAvailability(isAvailable);
	}

}