package rs.edu.raf.si.bank2.otc.controllers;

import io.micrometer.core.annotation.Timed;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.si.bank2.otc.models.mongodb.MarginBalance;
import rs.edu.raf.si.bank2.otc.services.MarginBalanceService;

@RestController
@CrossOrigin
@RequestMapping("/api/marginAccount")
@Timed
public class MarginBalanceController {

    private final MarginBalanceService marginBalanceService;

    @Autowired
    public MarginBalanceController(MarginBalanceService marginBalanceService) {
        this.marginBalanceService = marginBalanceService;
    }

    @Timed("controllers.marginBalance.getAllMarginBalances")
    @GetMapping
    public ResponseEntity<List<MarginBalance>> getAllMarginBalances() {
        List<MarginBalance> marginBalances = marginBalanceService.getAllMarginBalances();
        return new ResponseEntity<>(marginBalances, HttpStatus.OK);
    }

    @Timed("controllers.marginBalance.getMarginBalanceById")
    @GetMapping("/{id}")
    public ResponseEntity<MarginBalance> getMarginBalanceById(@PathVariable String id) {
        MarginBalance marginBalance = marginBalanceService.getMarginBalanceById(id);
        if (marginBalance != null) {
            return new ResponseEntity<>(marginBalance, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Timed("controllers.marginBalance.createMarginBalance")
    @PostMapping
    public ResponseEntity<MarginBalance> createMarginBalance(@RequestBody MarginBalance marginBalance) {
        MarginBalance createdMarginBalance = marginBalanceService.createMarginBalance(marginBalance);
        return new ResponseEntity<>(createdMarginBalance, HttpStatus.CREATED);
    }

    @Timed("controllers.marginBalance.updateMarginBalance")
    @PutMapping("/{id}")
    public ResponseEntity<MarginBalance> updateMarginBalance(
            @PathVariable String id, @RequestBody MarginBalance updatedMarginBalance) {
        MarginBalance existingMarginBalance = marginBalanceService.getMarginBalanceById(id);
        if (existingMarginBalance != null) {
            updatedMarginBalance.setId(existingMarginBalance.getId());
            MarginBalance savedMarginBalance = marginBalanceService.updateMarginBalance(updatedMarginBalance);
            return new ResponseEntity<>(savedMarginBalance, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Timed("controllers.marginBalance.deleteMarginBalance")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarginBalance(@PathVariable String id) {
        MarginBalance existingMarginBalance = marginBalanceService.getMarginBalanceById(id);
        if (existingMarginBalance != null) {
            marginBalanceService.deleteMarginBalance(existingMarginBalance);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
