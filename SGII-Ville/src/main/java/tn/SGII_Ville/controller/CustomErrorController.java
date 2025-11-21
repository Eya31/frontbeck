package tn.SGII_Ville.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController {

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        String msg = "Application error";
        HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            msg = "Error " + statusCode + " returned by the server";
            returnStatus = HttpStatus.resolve(statusCode);
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                msg += " - Forbidden (403). Check security configuration and CORS.";
            }
        }
        return ResponseEntity.status(returnStatus != null ? returnStatus : HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
