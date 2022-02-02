package kh.spring.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kh.spring.dto.BoardDTO;
import kh.spring.dto.BoardReplyDTO;
import kh.spring.dto.CertiDTO;
import kh.spring.dto.ChalDTO;
import kh.spring.dto.JoinChalDTO;
import kh.spring.dto.MemberDTO;
import kh.spring.service.BoardReplyService;
import kh.spring.service.BoardService;
import kh.spring.service.ChalService;
import kh.spring.service.MypageService;
import kh.spring.service.RefundService;

@RequestMapping("/mypage/")
@Controller
public class MypageController {

	@Autowired
	private HttpSession session;
	@Autowired
	private MypageService memberService;
	@Autowired
	private BoardReplyService brService;
	@Autowired
	private MypageService mService;
	@Autowired
	private BoardService bService;
	@Autowired
	private ChalService cservice;
	@Autowired
	private RefundService rservice;

	@RequestMapping("updateUserInfo")
	public String  updateUesrInfoPage(Model model, int seq) {
		MemberDTO memberDTO = memberService.selectBySeq(seq);
		model.addAttribute("user",memberDTO);
		return "user/updateUserInfo";
	}

	@RequestMapping("update")
	public String update(HttpServletRequest httpServletRequest,Model model) {
		int seq = Integer.parseInt(httpServletRequest.getParameter("seq"));
		String phone = httpServletRequest.getParameter("phone");
		String email = httpServletRequest.getParameter("email");
		String address1 = httpServletRequest.getParameter("address1");
		String address2 = httpServletRequest.getParameter("address2");
		String pw = httpServletRequest.getParameter("pw");
		MemberDTO memberDTO = memberService.selectBySeq(seq);
		memberDTO.setPhone(phone);
		memberDTO.setEmail(email);
		memberDTO.setAddress1(address1);
		memberDTO.setAddress2(address2);
		memberDTO.setPw(pw);
		model.addAttribute("user",memberDTO);
		memberService.update(memberDTO);
		return "user/mypage";
	}

	@RequestMapping("updateUserProfile")
	public String updateUserProfile(Model model, int seq) {
		MemberDTO memberDTO = memberService.selectBySeq(seq);
		model.addAttribute("user",memberDTO);
		return "user/updateProfile";
	}

	@RequestMapping("updateUserProfileAction")
	public String updateUserProfileAction(Model model,HttpServletRequest httpServletRequest, @RequestParam("file") MultipartFile file) throws IOException {
		int seq = Integer.parseInt(httpServletRequest.getParameter("seq"));
		String realPath = session.getServletContext().getRealPath("files");
		System.out.print(realPath);
		String filename ="ssh.png";
		//		FileCopyUtils.copy(file.getBytes(), new File(realPath+"/"+filename));
		File f = new File(realPath+"/"+filename);
		MemberDTO memberDTO = memberService.selectBySeq(seq);
		model.addAttribute("user",memberDTO);
		return "user/updateProfile";
	}

	//회원 마이페이지 홈
	@RequestMapping("info")
	public String mypage(Model model ,int seq, HttpServletRequest request) {
		MemberDTO memberDTO = memberService.selectBySeq(seq);
		model.addAttribute("user",memberDTO);
		System.out.print(memberDTO.toString());
		return "/user/mypage";

	}
	//참여중인 목록
	@RequestMapping("myChalList")
	public String myChalList(Model model) {
		String nickname = (String)session.getAttribute("writerNickname");
		List<JoinChalDTO> blist =  cservice.myChalListB(nickname);
		List<JoinChalDTO> plist =  cservice.myChalListP(nickname);
		List<JoinChalDTO> flist =  cservice.myChalListF(nickname);
		model.addAttribute("nickname",nickname);
		model.addAttribute("blist",blist);
		model.addAttribute("plist",plist);
		model.addAttribute("flist",flist);
		return "/user/myChalList";
	}
	
	//환급 신청되는지 확인
	@ResponseBody
	@RequestMapping(value ="refundOk", produces = "text/html;charset=utf8")
	public String chalCancel(int chalSeq) {
		String nickname = (String)session.getAttribute("writerNickname");
		System.out.println(chalSeq);
		//중복확인 메서드
		int num = rservice.refundOk(nickname, chalSeq);
		System.out.println("결과값" + num);
		if(num == 1) {
			return "true";
		}
		return "false";
	}
	//내가 작성한 글과 댓글
	@RequestMapping("myBoardAndReply")
	public String myBoardPage(Model model) {
		String nickname = (String)session.getAttribute("writerNickname");
		List<BoardDTO> boardList = memberService.getUserBoard(nickname);
		List<BoardReplyDTO> boardReplyList = memberService.getUserBoardReply(nickname);
		model.addAttribute("nickname",nickname);
		model.addAttribute("blist", boardList);
		model.addAttribute("rlist", boardReplyList);
		return "/user/mypageBoard";
	}
	@RequestMapping("myBARSearch")
	public String myBoardReply(Model model, String option, String keyword) {
		String nickname = (String)session.getAttribute("writerNickname");
		System.out.println(option + ":" + keyword);
		if(option.equals("title")) {
			List<BoardDTO> boardList = bService.mySearch(nickname, option, keyword);
			List<BoardReplyDTO> boardReplyList = memberService.getUserBoardReply(nickname);
			model.addAttribute("nickname",nickname);
			model.addAttribute("blist", boardList);
			model.addAttribute("rlist", boardReplyList);
		}else if(option.equals("contents")) {
			List<BoardDTO> boardList = bService.mySearch(nickname, option, keyword);
			List<BoardReplyDTO> boardReplyList = brService.mySearch(nickname, "repContents", keyword);
			model.addAttribute("nickname",nickname);
			model.addAttribute("blist", boardList);
			model.addAttribute("rlist", boardReplyList);
		}else if(option.equals("seq")) {
			List<BoardDTO> boardList = bService.mySearch(nickname, option, keyword);
			List<BoardReplyDTO> boardReplyList = brService.mySearch(nickname, "refBoardSeq", keyword);
			model.addAttribute("nickname",nickname);
			model.addAttribute("blist", boardList);
			model.addAttribute("rlist", boardReplyList);
		}
		return "/user/mypageBoard";
	}

	@RequestMapping("delete")
	public String deleteUser(int seq) {
		memberService.delete(seq);
		return "home";
	}

	@RequestMapping("certi") // 인증 상세목록으로 이동.
	public String certi(Model model, String chalName, int refChalSeq) {
		int chalSeq = refChalSeq;
		String nickname = (String)session.getAttribute("writerNickname");
		// 인증한 목록 출력.
		List<CertiDTO> list = mService.findCertiList(chalSeq, chalName, nickname);
		CertiDTO info = new CertiDTO();
		info.setChalSeq(chalSeq);
		info.setChalName(chalName);
		info.setRefNickname(nickname);
		model.addAttribute("list",list);
		model.addAttribute("info",info);
		return "/user/certi";
	}

	@RequestMapping("certiWriteForm") // 인증 작성폼으로 이동.
	public String certiWriteForm(Model model, CertiDTO dto) {
		model.addAttribute("list",dto);
		return "/user/certiwriteform";
	}

	// 인증 작성은 ImageController에서 구현.

	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:/";
	}
	
	@RequestMapping("like") // 인증 작성폼으로 이동.
	public String like() {
		return "/user/like";
	}
	
	
}