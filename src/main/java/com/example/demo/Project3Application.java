package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class Project3Application {

    public static void main(String[] args) {
        SpringApplication.run(Project3Application.class, args);
    }

}

@Entity
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    // Constructors, Getters, and Setters
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

interface UserRepository extends JpaRepository<User, Long> {
    // NEW: Custom query methods
    Optional<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
}

@Service
class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // NEW: Find by exact email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // NEW: Search by name (partial match)
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    // NEW: Bulk create users
    public List<User> createBulkUsers(List<User> users) {
        return userRepository.saveAll(users);
    }

    // NEW: Partial update (PATCH)
    public User patchUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Only update fields that are provided (not null)
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        return userRepository.save(user);
    }

    // NEW: Delete all users
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Original Endpoints ---

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // --- NEW Endpoints ---

    // 1. Get user by email
    // Example: GET /api/users/email/john@example.com
    @GetMapping("/email/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    // 2. Search users by partial name
    // Example: GET /api/users/search?name=john
    @GetMapping("/search")
    public List<User> searchUsersByName(@RequestParam String name) {
        return userService.searchUsersByName(name);
    }

    // 3. Create multiple users at once
    // Example: POST /api/users/bulk (Pass an array of User objects)
    @PostMapping("/bulk")
    public List<User> createBulkUsers(@RequestBody List<User> users) {
        return userService.createBulkUsers(users);
    }

    // 4. Partially update a user
    // Example: PATCH /api/users/1 (Pass just { "email": "newemail@example.com" })
    @PatchMapping("/{id}")
    public User patchUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.patchUser(id, userDetails);
    }

    // 5. Delete all users
    // Example: DELETE /api/users/all
    @DeleteMapping("/all")
    public String deleteAllUsers() {
        userService.deleteAllUsers();
        return "All users have been deleted successfully.";
    }
}
