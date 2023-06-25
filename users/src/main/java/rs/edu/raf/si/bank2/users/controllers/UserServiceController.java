package rs.edu.raf.si.bank2.users.controllers;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import io.micrometer.core.annotation.Timed;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.si.bank2.users.dto.ChangePassDto;
import rs.edu.raf.si.bank2.users.models.mariadb.User;
import rs.edu.raf.si.bank2.users.services.UserService;

@RestController
@CrossOrigin
@RequestMapping("/api/userService")
@Timed
public class UserServiceController {

    private final UserService userService;

    @Autowired
    public UserServiceController(UserService userService) {
        this.userService = userService;
    }

    @Timed("controllers.user.loadByUsername")
    @GetMapping(value = "/loadUserByUsername/{username}")
    public ResponseEntity<?> loadUserByUsername(@PathVariable(name = "username") String username) {
        return ResponseEntity.ok().body(userService.loadUserByUsername(username));
    }

    @Timed("controllers.user.findByEmail")
    @GetMapping(value = "/findByEmail")
    public ResponseEntity<?> findByEmail() {
        String userEmail = getContext().getAuthentication().getName();

        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) return ResponseEntity.ok().body(user);
        else return ResponseEntity.status(404).body("Korisnik nije pronadjen.");
    }

    @Timed("controllers.user.findAll")
    @GetMapping(value = "/findAll")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok().body(userService.findAll());
    }

    @Timed("controllers.user.save")
    @PostMapping(value = "/save")
    public ResponseEntity<?> save(@RequestBody User user) {
        return ResponseEntity.ok().body(userService.save(user));
    }

    @Timed("controllers.user.getUserPermissions")
    @GetMapping(value = "/getUserPermissions")
    public ResponseEntity<?> getUserPermissions() {
        String userEmail = getContext().getAuthentication().getName();
        return ResponseEntity.ok().body(userService.getUserPermissions(userEmail));
    }

    @Timed("controllers.user.findById")
    @GetMapping(value = "/findById/{id}")
    public ResponseEntity<?> findById(@PathVariable(name = "id") Long id) {
        //        return ResponseEntity.ok().body(userService.findById(id));

        Optional<User> user = userService.findById(id);
        if (user.isPresent()) return ResponseEntity.ok().body(user);
        else return ResponseEntity.status(404).body("Korisnik sa email-om: asdf@raf.rs nije pronadjen.");
    }

    @Timed("controllers.user.deleteById")
    @DeleteMapping(value = "/deleteById/{id}")
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok().body("User deleted");
    }

    @Timed("controllers.user.getUserByPasswordResetToken")
    @GetMapping(value = "/getUserByPasswordResetToken/{token}")
    public ResponseEntity<?> getUserByPasswordResetToken(@PathVariable(name = "token") String token) {
        return ResponseEntity.ok().body(userService.getUserByPasswordResetToken(token));
    }

    @Timed("controllers.user.changePassword")
    @PatchMapping(value = "/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassDto dto) {
        userService.changePassword(dto.getUser(), dto.getNewPass(), dto.getPassResetToken());
        return ResponseEntity.ok().body("Password changed");
    }

    @Timed("controllers.user.changeUsersDailyLimit")
    @PatchMapping(value = "/changeUsersDailyLimit/{limit}")
    public ResponseEntity<?> changeUsersDailyLimit(@PathVariable(name = "limit") Double limit) {
        String userEmail = getContext().getAuthentication().getName();
        return ResponseEntity.ok().body(userService.changeUsersDailyLimit(userEmail, limit));
    }

    @Timed("controllers.user.getUsersDailyLimit")
    @GetMapping(value = "/limit")
    public ResponseEntity<?> getUsersDailyLimit() {
        String userEmail = getContext().getAuthentication().getName();
        return ResponseEntity.ok().body(userService.getUsersDailyLimit(userEmail));
    }
}
