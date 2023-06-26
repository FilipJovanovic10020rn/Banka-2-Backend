package rs.edu.raf.si.bank2.main.controllers;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.si.bank2.main.services.AuthorisationService;
import rs.edu.raf.si.bank2.main.services.ExchangeService;
import rs.edu.raf.si.bank2.main.services.UserCommunicationService;
import rs.edu.raf.si.bank2.main.services.UserService;
import rs.edu.raf.si.bank2.main.services.interfaces.UserCommunicationInterface;

@RestController
@CrossOrigin
@RequestMapping("/api/exchange")
@Timed
public class ExchangeController {

    private final AuthorisationService authorisationService;
    private final ExchangeService exchangeService;
    private final UserService userService;
    private final UserCommunicationInterface userCommunicationInterface;

    @Autowired
    public ExchangeController(
            AuthorisationService authorisationService,
            ExchangeService exchangeService,
            UserService userService,
            UserCommunicationService communicationService) {
        this.userCommunicationInterface = communicationService;
        this.authorisationService = authorisationService;
        this.exchangeService = exchangeService;
        this.userService = userService;
    }

    @Timed("controllers.exchange.findAll")
    @GetMapping()
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok().body(exchangeService.findAll());
    }

    @Timed("controllers.exchange.findById")
    @GetMapping(value = "/id/{id}")
    public ResponseEntity<?> findById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(exchangeService.findById(id));
    }

    @Timed("controllers.exchange.isExchangeActive")
    @GetMapping(value = "/status/{micCode}")
    public ResponseEntity<?> isExchangeActive(@PathVariable(name = "micCode") String micCode) {
        if (exchangeService.findByMicCode(micCode) != null) {
            return ResponseEntity.ok().body(exchangeService.isExchangeActive(micCode));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Timed("controllers.exchange.findByAcronym")
    @GetMapping(value = "/acronym/{acronym}")
    public ResponseEntity<?> findByAcronym(@PathVariable(name = "acronym") String acronym) {
        return ResponseEntity.ok().body(exchangeService.findByAcronym(acronym));
    }
}
