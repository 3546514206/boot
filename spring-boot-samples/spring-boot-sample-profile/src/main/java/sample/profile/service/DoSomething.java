package sample.profile.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @description: DoSomething
 * @author: 杨海波
 * @date: 2022-05-14 16:41
 **/
@Component
public class DoSomething implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("do something");
    }
}
