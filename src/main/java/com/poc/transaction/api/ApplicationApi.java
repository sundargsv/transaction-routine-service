package com.poc.transaction.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/app") // this may be redundant if the application is already configured with a base path or in ingress
public class ApplicationApi {
    // This endpoint is used to check the health of the service
    @GetMapping(path = {"/version"})
    public String getVersion() {
        log.info("Checking Health point...{}", "txn-processing-svc");
        // can return a version of the app in future from maven build info
        return "Healthy";
    }
}
