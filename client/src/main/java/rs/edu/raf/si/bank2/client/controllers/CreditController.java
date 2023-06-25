package rs.edu.raf.si.bank2.client.controllers;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.si.bank2.client.dto.*;
import rs.edu.raf.si.bank2.client.models.mongodb.enums.CreditApproval;
import rs.edu.raf.si.bank2.client.repositories.mongodb.CreditRequestRepository;
import rs.edu.raf.si.bank2.client.repositories.mongodb.PayedInterestRepository;
import rs.edu.raf.si.bank2.client.services.CreditService;

@RestController
@CrossOrigin
@RequestMapping("/api/credit")
@Timed
public class CreditController {

    private final CreditService creditService;
    private final CreditRequestRepository creditRequestRepository;
    private final PayedInterestRepository payedInterestRepository;

    @Autowired
    public CreditController(
            CreditService creditService,
            CreditRequestRepository creditRequestRepository,
            PayedInterestRepository payedInterestRepository) {
        this.creditService = creditService;
        this.creditRequestRepository = creditRequestRepository;
        this.payedInterestRepository = payedInterestRepository;
    }

    // credit

    @Timed("controllers.credit.getCreditsForClient")
    @GetMapping("/{email}")
    public ResponseEntity<?> getCreditsForClient(@PathVariable String email) {
        return ResponseEntity.ok(creditService.getCreditsAllForClient(email));
    }

    @Timed("controllers.credit.payThisMonthsInterest")
    @PostMapping("/pay/{creditId}")
    public ResponseEntity<?> payThisMonthsInterest(@PathVariable String creditId) {
        CommunicationDto communicationDto = creditService.payOffOneMonthsInterest(creditId);
        return ResponseEntity.status(communicationDto.getResponseCode()).body(communicationDto.getResponseMsg());
    }

    @Timed("controllers.credit.getAllPayedInterests")
    @GetMapping("/interests/{creditId}")
    public ResponseEntity<?> getAllPayedInterests(@PathVariable String creditId) {
        return ResponseEntity.ok(payedInterestRepository.findAll());
    }

    // requests
    @Timed("controllers.credit.requestCredit")
    @PostMapping("/request")
    public ResponseEntity<?> requestCredit(@RequestBody CreditRequestDto dto) {
        return ResponseEntity.ok(creditService.saveRequest(dto));
    }

    @Timed("controllers.credit.getAllWaitingRequests")
    @GetMapping
    public ResponseEntity<?> getAllWaitingRequests() {
        return ResponseEntity.ok(creditRequestRepository.findAllByCreditApproval(CreditApproval.WAITING));
    }

    @Timed("controllers.credit.approveRequest")
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable String id, @RequestBody CreditDto creditDto) {
        CommunicationDto communicationDto = creditService.approveCredit(id, creditDto);
        return ResponseEntity.status(communicationDto.getResponseCode()).body(communicationDto.getResponseMsg());
    }

    @Timed("controllers.credit.denyRequest")
    @PatchMapping("/deny/{id}")
    public ResponseEntity<?> denyRequest(@PathVariable String id) {
        CommunicationDto communicationDto = creditService.denyCredit(id);
        return ResponseEntity.status(communicationDto.getResponseCode()).body(communicationDto.getResponseMsg());
    }
}
