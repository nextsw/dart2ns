package d3e.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpContext {
  @Autowired private HttpServletRequest request;
  @Autowired private HttpServletResponse response;
  
  public HttpServletResponse getResponse() {
    return response;
  }
  
  public HttpServletRequest getRequest() {
    return request;
  }
}
