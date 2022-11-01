package d3e.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController extends VerificationControllerBase {
  @GetMapping("/verify")
  public String onVerificationLinkClick(@RequestParam String code) {
    return handleVerificationClick(code);
  }

  public void handleVerificationClick(String method, String context) {}
}
