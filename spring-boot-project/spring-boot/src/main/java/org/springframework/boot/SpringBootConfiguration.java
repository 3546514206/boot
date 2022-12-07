/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;

/**
 * Indicates that a class provides Spring Boot application
 * {@link Configuration @Configuration}. Can be used as an alternative to the Spring's
 * standard {@code @Configuration} annotation so that configuration can be found
 * automatically (for example in tests).
 * <p>
 * Application should only ever include <em>one</em> {@code @SpringBootConfiguration} and
 * most idiomatic Spring Boot applications will inherit it from
 * {@code @SpringBootApplication}.
 *
 * @author Phillip Webb
 * @since 1.4.0
 */

/**
 * SpringBootConfiguration 相当于 Configuration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@SpringBootConfiguration 实际上就是一个 @Configuration 注解，这个注解大家应该很熟悉了，加上这个注
// 解就是为了让当前类作为一个配置类交由 Spring 的 IOC 容器进行管理，因为 Spring Boot 本质上还是 Spring，所
// 以原属于 Spring 的注解 @Configuration 在 Spring Boot 中也可以直接应用。
//由此可见，@SpringBootConfiguration 注解的作用与 @Configuration 注解相同，都是标识一个可以被组件扫描器扫描的配置类，
// 只不过 @SpringBootConfiguration 是被 Spring Boot 进行了重新封装命名而已。
@Configuration
public @interface SpringBootConfiguration {

}
