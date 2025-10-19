package com.chattingbox.controller;

import com.chattingbox.model.User;
import com.chattingbox.repository.UserRepository;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/testdb")
public class TestDbController {
    private final UserRepository repo;

    public TestDbController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/add")
    public User addUser(@RequestBody User user) {
        return repo.save(user);
    }

    @GetMapping("/all")
    public Iterable<User> getUsers() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable String id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE (PUT) – partial update allowed (username/password jo bhejo)
    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User incoming) {
        Optional<User> opt = repo.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();

        User u = opt.get();
        if (incoming.getUsername() != null && !incoming.getUsername().isBlank()) {
            u.setUsername(incoming.getUsername());
        }
        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            u.setPassword(incoming.getPassword());
        }
        return ResponseEntity.ok(repo.save(u));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!repo.existsById(id))
            return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
