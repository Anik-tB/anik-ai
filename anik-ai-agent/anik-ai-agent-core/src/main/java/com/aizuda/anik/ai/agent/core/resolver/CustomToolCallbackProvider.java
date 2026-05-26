package com.aizuda.anik.ai.agent.core.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Automatically discover all beans containing @Tool methods in the Spring container, convert them to ToolCallback and cache them.
 * <p>
 * The user only needs to declare the @Component + @Tool method, and it will be automatically discovered and injected into Agent ChatClient at startup.
 * <p>
 * Example:
 * <pre>
 * &#064;Component
 * public class OrderTool {
 *     &#064;Tool(name = "query_order", description = "Query order information")
 *     public String queryOrder(&#064;ToolParam(description = "Order ID") String orderId) {
 *         return orderService.query(orderId);
 *     }
 * }
 * </pre>
 *
 * @author openanik
 * @date 2026-05-24
 */
@Slf4j
public class CustomToolCallbackProvider implements ToolCallbackProvider, InitializingBean {

    private static final String INTERNAL_TOOL_PACKAGE = "com.aizuda.anik.ai.agent.core.tool";

    private final ApplicationContext applicationContext;
    private ToolCallback[] cachedCallbacks = new ToolCallback[0];

    public CustomToolCallbackProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        List<ToolCallback> allCallbacks = new ArrayList<>();

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);

                // Exclude framework internal toolkit
                if (bean.getClass().getPackageName().startsWith(INTERNAL_TOOL_PACKAGE)) {
                    continue;
                }

                if (hasToolMethods(bean)) {
                    ToolCallback[] callbacks = ToolCallbacks.from(bean);
                    allCallbacks.addAll(Arrays.asList(callbacks));
                    log.info("Discovered @Tool bean: [{}] with {} tool(s)", beanName, callbacks.length);
                }
            } catch (Exception e) {
                // Bean may be lazy or prototype, ignored
                log.debug("Skip bean [{}] during tool scanning: {}", beanName, e.getMessage());
            }
        }

        this.cachedCallbacks = allCallbacks.toArray(new ToolCallback[0]);
        if (cachedCallbacks.length > 0) {
            log.info("Custom tool discovery complete: {} tool(s) cached", cachedCallbacks.length);
        }
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return cachedCallbacks;
    }

    private boolean hasToolMethods(Object bean) {
        return Arrays.stream(bean.getClass().getMethods())
                .anyMatch(m -> m.isAnnotationPresent(Tool.class));
    }
}
