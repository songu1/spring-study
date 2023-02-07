package hello.hellospring.controller;

import hello.hellospring.HelloSpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    // map application에서 hello라고 들어오면 이 메서드를 호출해줌 (spring)
    @GetMapping("hello")
    public String hello(Model model){
        model.addAttribute("data","spring!!");       // data를 hello!!로 넘김
        return "hello";     // templete 폴더안의 hello.html를 찾아서 실행해줌
    }

    @GetMapping("hello-mvc")        // parameter로 받아옴
    public String helloMVC(@RequestParam(name="name") String name, Model model ){
        model.addAttribute("name",name);
        return "hello-templete";
    }

    @GetMapping("hello-string")
    @ResponseBody
    public String helloString(@RequestParam("name") String name){
        return "hello " + name;
    }

    @GetMapping("hello-api")
    @ResponseBody
    public Hello helloApi(@RequestParam("name") String name){
        Hello hello=new Hello();
        hello.setName(name);
        return hello;
    }

    static class Hello{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
