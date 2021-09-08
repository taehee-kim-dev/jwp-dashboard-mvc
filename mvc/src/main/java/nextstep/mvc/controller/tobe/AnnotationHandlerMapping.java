package nextstep.mvc.controller.tobe;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.handler.mapping.HandlerMapping;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        final Set<Object> handlers = getHandlers();
        putHandlerExecutionsOfHandlers(handlers);
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private Set<Object> getHandlers() {
        final Reflections reflections = new Reflections(basePackage);
        final Set<Class<?>> handlerClasses = reflections.getTypesAnnotatedWith(Controller.class);
        return handlerClasses.stream()
                .map(this::createHandlerInstance)
                .collect(Collectors.toSet());
    }

    private void putHandlerExecutionsOfHandlers(Set<Object> handlers) {
        for (Object handler : handlers) {
            final Class<?> handlerClass = handler.getClass();
            log.info("Annotation Controller 등록 : {}", handlerClass.getName());
            final Method[] methods = handlerClass.getDeclaredMethods();
            putHandlerExecutionsOfHandlerMethods(handler, methods);
        }
    }

    private Object createHandlerInstance(Class<?> handlerClass) {
        try {
            return handlerClass.getConstructor().newInstance();
        } catch (Exception e) {
            log.error("핸들러 클래스 {} 의 인스턴스 생성 중 에러가 발생했습니다.", handlerClass.getName());
            throw new IllegalStateException(String.format("핸들러 클래스 %s 의 인스턴스 생성 중 에러가 발생했습니다.", handlerClass.getName()));
        }
    }

    private void putHandlerExecutionsOfHandlerMethods(Object handler, Method[] methods) {
        for (Method method : methods) {
            log.info("Annotation Controller {} 의 method {} 등록", handler.getClass().getName(), method.getName());
            final RequestMapping requestMappingAnnotation = method.getAnnotation(RequestMapping.class);

            final String requestUri = requestMappingAnnotation.value();
            final RequestMethod requestMethod = requestMappingAnnotation.method();

            final HandlerKey handlerKey = new HandlerKey(requestUri, requestMethod);
            final HandlerExecution handlerExecution = new HandlerExecution(handler, method);
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    public Object getHandler(HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        final RequestMethod requestMethod = RequestMethod.valueOf(method);
        final HandlerKey handlerKey = new HandlerKey(requestURI, requestMethod);

        return handlerExecutions.get(handlerKey);
    }
}
