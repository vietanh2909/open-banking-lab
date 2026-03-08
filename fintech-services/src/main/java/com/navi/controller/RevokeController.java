package com.navi.controller;

import com.navi.dto.RevokeRequest;
import com.navi.dto.RevokeResponse;
import com.navi.service.RevokeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RevokeController {

    private final RevokeService revokeService;

    public RevokeController(RevokeService revokeService) {
        this.revokeService = revokeService;
    }

    @PostMapping("/revoke")
    public  ResponseEntity<RevokeResponse> revoke(@RequestBody RevokeRequest req) {
        RevokeResponse response =
                revokeService.revokeEverywhere(req.getSubject());

        return ResponseEntity.ok(response);
    }
}