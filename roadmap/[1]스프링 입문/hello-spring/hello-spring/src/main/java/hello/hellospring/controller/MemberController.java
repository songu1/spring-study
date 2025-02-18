package hello.hellospring.controller;

import hello.hellospring.domain.Member;
import hello.hellospring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members/new")         //get 방식으로 들어옴
    public String createdForm() {
        return "members/createMemberForm";      //createMemberForm으로 이동
    }

    @PostMapping("/members/new")
    public String create(MemberForm form){
        Member member = new Member();
        member.setName(form.getName());

        memberService.join(member);     // 회원가입할 때 join함수

        return "redirect:/";            // 회원가입 했으므로 home화면으로 돌리기
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();         //findMembers : 멤버를 다 끄집어 올 수 있음
        model.addAttribute("members",members);          // 멤버 리스트 자체를 모델에 담아 화면에 넘김
        return "members/memberList";
    }
}
