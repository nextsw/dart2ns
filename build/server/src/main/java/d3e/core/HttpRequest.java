package d3e.core;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.common.io.CharStreams;

public class HttpRequest {
  public static String getBodyAsString(HttpServletRequest request) {
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      try {
        return CharStreams.toString(request.getReader());
      } catch (IOException e) {}
    }
    return "";
  }
}
