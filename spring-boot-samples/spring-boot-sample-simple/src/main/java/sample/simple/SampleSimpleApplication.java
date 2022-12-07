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

package sample.simple;

import sample.simple.service.HelloWorldService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 每一个 Spring Boot 程序都有一个主入口，这个主入口就是 main 方法，而 main 方法中都会
// 调用 SpringBootApplication.run 方法，一个快速了解 SpringBootApplication 启动过程的好方法
// 就是在 run 方法中打一个断点，然后通过 Debug 的模式启动工程，逐步跟踪了解 SpringBoot 源码是如何完
// 成环境准备和启动加载 bean 的。
@SpringBootApplication
public class SampleSimpleApplication implements CommandLineRunner {

	// Simple example shows how a command line spring application can execute an
	// injected bean service. Also demonstrates how you can use @Value to inject
	// command line args ('--name=whatever') or application properties

	@Autowired
	private HelloWorldService helloWorldService;

	@Override
	public void run(String... args) {
		System.out.println("........................................................");
		System.out.println(this.helloWorldService.getHelloMessage());
		if (args.length > 0 && args[0].equals("exitcode")) {
			throw new ExitException();
		}
	}

	public static void main(String[] args) {
//		查看SpringBootApplication.run方法的源码就可以发现SpringBoot启动的流程主要分为两大阶段：
//		1）初始化 SpringApplication ；2）运行 SpringApplication 的过程。
//		其中运行SpringApplication的过程又可以细分为以下几个部分：
//		1）SpringApplicationRunListeners 引用启动监控模块
//		2）ConfigrableEnvironment配置环境模块和监听：包括创建配置环境、加载属性配置文件 和 发布配置环境已发布的事件
//		3）ConfigrableApplicationContext配置应用上下文：包括配置应用上下文对象、配置基本属性和刷新应用上下文
		SpringApplication.run(SampleSimpleApplication.class, args);
	}

}
