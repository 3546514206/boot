/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web.servlet;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import java.util.Arrays;
import java.util.List;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the Spring
 * {@link DispatcherServlet}. Should work for a standalone application where an embedded
 * web server is already present and also for a deployable application using
 * {@link SpringBootServletInitializer}.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Brian Clozel
 */

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
public class DispatcherServletAutoConfiguration {

    /*
     * The bean name for a DispatcherServlet that will be mapped to the root URL "/"
     */
    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

    /*
     * The bean name for a ServletRegistrationBean for the DispatcherServlet "/"
     */
    public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = "dispatcherServletRegistration";


    /**
     * 可以看到Spring Boot内部已经自动为我们做了很多关于DispatcherServlet的配置，
     * 其中的@EnableConfigurationProperties还能够在读取配置内容的情况下自动生成SpringMVC所需的类，
     * 有关这些内容的讨论可以参考附录。到这里，应该明白为什么几乎在没有任何配置下就能用Spring Boot启动Spring MVC项目，
     * 这些都是SpringBoot通过Maven依赖找到对应的jar包和嵌入的服务器，然后使用默认自动配置类来创建默认的开发环境。
     * 但是有时候，我们需要对这些默认的环境进行修改以适应个性化的要求，这些在Spring Boot中也是非常简单的，
     * 正如@EnableConfigurationProperties注解那样，它允许读入配置文件的内容来自定义自动初始化所需的内容。这里扩展说明一下配置的加载顺序
     * 事实上，Spring Boot的参数配置除了使用properties文件之外，还可以使用yml文件等，它会以下列的优先级顺序进行加载：
     * 1）命令行参数；
     * 2）来自java:comp/env的JNDI属性；
     * 3）Java系统属性（System.getProperties()）；
     * 4）操作系统环境变量；
     * 5）RandomValuePropertySource配置的random.
     * 6）属性值；
     * 7）jar包外部的application-{profile}.properties或application.yml（带spring.profile）配置文件；
     * 8）jar包内部的application-{profile}.properties或application.ym（带spring.profile）配置文件；
     * 9）jar包外部的application.properties或application.yml（不带spring.profile）配置文件；
     * 10）jar包内部的application.properties或application.ym（不带spring.profile）配置文件；
     * 11）@Configuration注解类上的@PropertySource；
     * 12）通过SpringApplication.setDefaultProperties指定的默认属性。
     */
    @Configuration
    @Conditional(DefaultDispatcherServletCondition.class)
    //如果存在ServletRegistration类则进行配置
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties({HttpProperties.class, WebMvcProperties.class})
    protected static class DispatcherServletConfiguration {

        private final HttpProperties httpProperties;

        private final WebMvcProperties webMvcProperties;

        public DispatcherServletConfiguration(HttpProperties httpProperties,
                                              WebMvcProperties webMvcProperties) {
            this.httpProperties = httpProperties;
            this.webMvcProperties = webMvcProperties;
        }

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public DispatcherServlet dispatcherServlet() {
            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            dispatcherServlet.setDispatchOptionsRequest(
                    this.webMvcProperties.isDispatchOptionsRequest());
            dispatcherServlet.setDispatchTraceRequest(
                    this.webMvcProperties.isDispatchTraceRequest());
            dispatcherServlet.setThrowExceptionIfNoHandlerFound(
                    this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
            dispatcherServlet.setEnableLoggingRequestDetails(
                    this.httpProperties.isLogRequestDetails());
            return dispatcherServlet;
        }

        @Bean
        // 如果存在MultipartResolver则配置
        @ConditionalOnBean(MultipartResolver.class)
        // 如果不存在名为DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME的bean则配置
        @ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
        public MultipartResolver multipartResolver(MultipartResolver resolver) {
            // Detect if the user has created a MultipartResolver but named it incorrectly
            return resolver;
        }

    }

    @Configuration
    @Conditional(DispatcherServletRegistrationCondition.class)
    @ConditionalOnClass(ServletRegistration.class)
    @EnableConfigurationProperties(WebMvcProperties.class)
    @Import(DispatcherServletConfiguration.class)
    protected static class DispatcherServletRegistrationConfiguration {

        private final WebMvcProperties webMvcProperties;

        private final MultipartConfigElement multipartConfig;

        public DispatcherServletRegistrationConfiguration(
                WebMvcProperties webMvcProperties,
                ObjectProvider<MultipartConfigElement> multipartConfigProvider) {
            this.webMvcProperties = webMvcProperties;
            this.multipartConfig = multipartConfigProvider.getIfAvailable();
        }

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
        @ConditionalOnBean(value = DispatcherServlet.class, name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public DispatcherServletRegistrationBean dispatcherServletRegistration(
                DispatcherServlet dispatcherServlet) {
            DispatcherServletRegistrationBean registration = new DispatcherServletRegistrationBean(
                    dispatcherServlet, this.webMvcProperties.getServlet().getPath());
            registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            registration.setLoadOnStartup(
                    this.webMvcProperties.getServlet().getLoadOnStartup());
            if (this.multipartConfig != null) {
                registration.setMultipartConfig(this.multipartConfig);
            }
            return registration;
        }

    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    private static class DefaultDispatcherServletCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context,
                                                AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage
                    .forCondition("Default DispatcherServlet");
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            List<String> dispatchServletBeans = Arrays.asList(beanFactory
                    .getBeanNamesForType(DispatcherServlet.class, false, false));
            if (dispatchServletBeans.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("dispatcher servlet bean")
                        .items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            if (beanFactory.containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome
                        .noMatch(message.found("non dispatcher servlet bean")
                                .items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            if (dispatchServletBeans.isEmpty()) {
                return ConditionOutcome
                        .match(message.didNotFind("dispatcher servlet beans").atAll());
            }
            return ConditionOutcome.match(message
                    .found("dispatcher servlet bean", "dispatcher servlet beans")
                    .items(Style.QUOTE, dispatchServletBeans)
                    .append("and none is named " + DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
        }

    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    private static class DispatcherServletRegistrationCondition
            extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context,
                                                AnnotatedTypeMetadata metadata) {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            ConditionOutcome outcome = checkDefaultDispatcherName(beanFactory);
            if (!outcome.isMatch()) {
                return outcome;
            }
            return checkServletRegistration(beanFactory);
        }

        private ConditionOutcome checkDefaultDispatcherName(
                ConfigurableListableBeanFactory beanFactory) {
            List<String> servlets = Arrays.asList(beanFactory
                    .getBeanNamesForType(DispatcherServlet.class, false, false));
            boolean containsDispatcherBean = beanFactory
                    .containsBean(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
            if (containsDispatcherBean
                    && !servlets.contains(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)) {
                return ConditionOutcome
                        .noMatch(startMessage().found("non dispatcher servlet")
                                .items(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
            }
            return ConditionOutcome.match();
        }

        private ConditionOutcome checkServletRegistration(
                ConfigurableListableBeanFactory beanFactory) {
            ConditionMessage.Builder message = startMessage();
            List<String> registrations = Arrays.asList(beanFactory
                    .getBeanNamesForType(ServletRegistrationBean.class, false, false));
            boolean containsDispatcherRegistrationBean = beanFactory
                    .containsBean(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
            if (registrations.isEmpty()) {
                if (containsDispatcherRegistrationBean) {
                    return ConditionOutcome
                            .noMatch(message.found("non servlet registration bean").items(
                                    DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
                }
                return ConditionOutcome
                        .match(message.didNotFind("servlet registration bean").atAll());
            }
            if (registrations
                    .contains(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)) {
                return ConditionOutcome.noMatch(message.found("servlet registration bean")
                        .items(DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
            }
            if (containsDispatcherRegistrationBean) {
                return ConditionOutcome
                        .noMatch(message.found("non servlet registration bean").items(
                                DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
            }
            return ConditionOutcome.match(message.found("servlet registration beans")
                    .items(Style.QUOTE, registrations).append("and none is named "
                            + DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME));
        }

        private ConditionMessage.Builder startMessage() {
            return ConditionMessage.forCondition("DispatcherServlet Registration");
        }

    }

}
