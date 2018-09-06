package com.rayfay.bizcloud.uaa.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author maxiang
 *
 */
@Controller
public class AppErrorController implements ErrorController
{
  
  private static final Logger logger = LoggerFactory.getLogger(AppErrorController.class);
  
  private static AppErrorController appErrorController;
  
  /**
   * Error Attributes in the Application
   */
  @Autowired
  private ErrorAttributes errorAttributes;
  
  private final static String ERROR_PATH = "/error";
  
  /**
   * Controller for the Error Controller
   * 
   * @param errorAttributes
   * @return
   */
  
  public AppErrorController(ErrorAttributes errorAttributes)
  {
    this.errorAttributes = errorAttributes;
  }
  
  public AppErrorController()
  {
    if (appErrorController == null)
    {
      appErrorController = new AppErrorController(errorAttributes);
    }
  }
  
  /**
   * Supports the HTML Error View
   * 
   * @param request
   * @return
   */
  @RequestMapping(value = {ERROR_PATH, "/oauth/error"}, produces = "text/html")
  public ModelAndView errorHtml(HttpServletRequest request)
  {
    Map<String, Object> attr = getErrorAttributes(request, true);

    attr.put("contextPath", request.getContextPath());
    return new ModelAndView("error", attr);
  }
  
  /**
   * Supports other formats like JSON, XML
   * 
   * @param request
   * @return
   */
  @RequestMapping(value = ERROR_PATH)
  @ResponseBody
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request)
  {
    Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
    HttpStatus status = getStatus(request);
    return new ResponseEntity<Map<String, Object>>(body, status);
  }
  
  /**
   * Returns the path of the error page.
   *
   * @return the error path
   */
  @Override
  public String getErrorPath()
  {
    return ERROR_PATH;
  }
  
  private boolean getTraceParameter(HttpServletRequest request)
  {
    String parameter = request.getParameter("trace");
    if (parameter == null)
    {
      return false;
    }
    return !"false".equals(parameter.toLowerCase());
  }
  
  private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace)
  {
    RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    Map<String, Object> map = this.errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    String errorMessage = this.errorAttributes.getError(requestAttributes)==null?""
            : this.errorAttributes.getError(requestAttributes).getMessage();
    map.put("message", errorMessage);
    String URL = request.getRequestURL().toString();
    map.put("URL", URL);
    if (new Integer(403).equals(map.get("status")) && map.get("exception")==null)
      map.put("errortitle", "无权限或会话超时，请重新登录");
      
    logger.error("AppErrorController [error info]: timestamp-" + map.get("timestamp"));
    logger.error("AppErrorController [error info]: URL-" + map.get("URL"));
    logger.error("AppErrorController [error info]: path-" + map.get("path"));
    logger.error("AppErrorController [error info]: status-" + map.get("status"));
    logger.error("AppErrorController [error info]: error-" + map.get("error"));
    logger.error("AppErrorController [error info]: exception-" + map.get("exception"));
    logger.error("AppErrorController [error info]: trace-" + map.get("trace"));
    return map;
  }
  
  private HttpStatus getStatus(HttpServletRequest request)
  {
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    if (statusCode != null)
    {
      try
      {
        return HttpStatus.valueOf(statusCode);
      }
      catch (Exception ex)
      {
      }
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
  
}