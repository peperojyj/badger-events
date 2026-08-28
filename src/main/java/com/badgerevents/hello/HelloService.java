package com.badgerevents.hello;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public HelloResponse getHello() {
        return new HelloResponse("BadgerEvents server is running");
    }
}
