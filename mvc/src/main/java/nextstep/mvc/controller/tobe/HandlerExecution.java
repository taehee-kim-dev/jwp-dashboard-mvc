package nextstep.mvc.controller.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.ModelAndView;

import java.lang.reflect.Method;

public class HandlerExecution {

    private final Method method;

    public HandlerExecution(Method method) {
        this.method = method;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Class<?> declaringClass = method.getDeclaringClass();
        final Object handler = declaringClass.getConstructor().newInstance();
        return (ModelAndView) method.invoke(handler, request, response);
    }
}
