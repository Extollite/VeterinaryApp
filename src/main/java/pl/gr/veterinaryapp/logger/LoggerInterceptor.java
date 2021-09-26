package pl.gr.veterinaryapp.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static pl.gr.veterinaryapp.logger.util.LoggerUtils.logRequest;

@Slf4j
@Component
public class LoggerInterceptor implements HandlerInterceptor {

    private final String TRACE_ID = "trace-id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String traceId = response.getHeader(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);
            response.setHeader(TRACE_ID, traceId);
        }

        if (request.getMethod().equals(HttpMethod.GET.name())) {
            log.info(logRequest(request, null));
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        MDC.remove(TRACE_ID);
    }
}
