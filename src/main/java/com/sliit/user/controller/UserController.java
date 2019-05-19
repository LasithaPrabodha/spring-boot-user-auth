package com.sliit.user.controller;

import java.net.URI;

import javax.validation.Valid;

import com.sliit.user.exception.ResourceNotFoundException;
import com.sliit.user.model.User;
import com.sliit.user.payload.*;
import com.sliit.user.repository.UserRepository;
import com.sliit.user.security.UserPrincipal;
import com.sliit.user.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/user/me")
	@PreAuthorize("hasRole('USER')")
	public UserProfile getCurrentUser(@CurrentUser UserPrincipal currentUser) {
		User user = userRepository.findByUsername(currentUser.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUser.getUsername()));

		UserProfile userProfile = new UserProfile(user.getFirstName(), user.getLastName(), user.getEmail());

		return userProfile;
	}

	@DeleteMapping("/user/me")
	public ResponseEntity<?> deleteUserProfile(@CurrentUser UserPrincipal currentUser) {
		User user = userRepository.findByUsername(currentUser.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUser.getUsername()));

		userRepository.delete(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/me").build().toUri();

		return ResponseEntity.created(location)
				.body(new ApiResponse(true, "Your account has been deleted successfully"));
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

	@GetMapping("/users/{username}")
	public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

		UserProfile userProfile = new UserProfile(user.getFirstName(), user.getLastName(), user.getEmail());

		return userProfile;
	}

	@PutMapping("/users/{username}")
	public ResponseEntity<?> saveUserProfile(@Valid @RequestBody UserProfile userProfile,
			@CurrentUser UserPrincipal currentUser) {
		User user = userRepository.findByUsername(currentUser.getUsername())
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUser.getUsername()));

		user.setEmail(userProfile.getEmail());
		user.setFirstName(userProfile.getFirstName());
		user.setLastName(userProfile.getLastName());

		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
				.buildAndExpand(result.getUsername()).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true, "User details saved successfully"));
	}

}