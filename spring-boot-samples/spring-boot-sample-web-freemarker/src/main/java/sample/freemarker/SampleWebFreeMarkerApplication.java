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

package sample.freemarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SampleWebFreeMarkerApplication {

	public static void main(String[] args) {
		ApplicationContext context =  SpringApplication.run(SampleWebFreeMarkerApplication.class, args);
		// SpringBootApplication 也包含了 Component 注解的功能
		SampleWebFreeMarkerApplication application = context.getBean(SampleWebFreeMarkerApplication.class);
		System.out.println(application.hashCode());
	}

}
