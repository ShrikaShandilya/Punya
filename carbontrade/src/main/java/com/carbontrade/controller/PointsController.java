package com.carbontrade.controller;

import com.carbontrade.controller.dto.BalanceResponse;
import com.carbontrade.controller.dto.EarnPointsRequest;
import com.carbontrade.controller.dto.RulesResponse;
import com.carbontrade.model.PointsWallet;
import com.carbontrade.service.PointsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/rules")
    public RulesResponse rules() {
        return new RulesResponse(PointsService.POINTS_PER_COIN, true);
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@RequestParam Long userId) {
        PointsWallet w = pointsService.getBalance(userId);
        return new BalanceResponse(w.getUserId(), w.getPoints(), w.getCoins());
    }

    @PostMapping("/earn")
    public BalanceResponse earn(@RequestBody EarnPointsRequest req) {
        String reason = (req.reason == null || req.reason.isBlank()) ? "api_earn" : req.reason;
        PointsWallet w = pointsService.earnPoints(req.userId, req.amount, reason);
        return new BalanceResponse(w.getUserId(), w.getPoints(), w.getCoins());
    }
}
