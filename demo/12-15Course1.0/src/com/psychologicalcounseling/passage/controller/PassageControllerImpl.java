package com.psychologicalcounseling.passage.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.psychologicalcounseling.entity.Article;
import com.psychologicalcounseling.entity.Evaluate;
import com.psychologicalcounseling.entity.Page;
import com.psychologicalcounseling.entity.User;
import com.psychologicalcounseling.passage.service.PassageServiceImpl;

@Controller
public class PassageControllerImpl {
	@Resource
	private PassageServiceImpl passageServiceImpl;
	
	@RequestMapping("/PassageControllerImpl")
	public String show(HttpServletRequest request) {
		String evaluateComment = request.getParameter("evaluateContent");     //获取评论内容
	//	System.out.println("evaluateComment"+evaluateComment);
		
		String id = request.getParameter("articleId");          //获取文章的articleId
		int articleId = Integer.parseInt(id);
		
		Article article = this.passageServiceImpl.findArticle(articleId);     //根据articleId获取文章
//		System.out.println(article.getArticleContent());
		List<Article> list = new ArrayList<Article>();
		list.add(article);
		
		/**
		 * 这里还差一个将文章正文转换的内容
		 */
		
		int articleLookNumber = article.getArticleLookNumber()+1;             //更新文章的浏览数量
//		System.out.println("articleLookNumber--------"+articleLookNumber);
		this.passageServiceImpl.updateLookNumber(articleLookNumber, articleId);
		
		String pageNum = request.getParameter("pageNum");        //获取pageNum的值
		int num = 0;
		if(pageNum==null || pageNum.equals("")) {
			num=1;
		}else {
			num = Integer.parseInt(pageNum);
		}
		
		
		if(evaluateComment==null) {               //如果评论为空，那么只显示他人的评论内容		
			
			int count = this.passageServiceImpl.findCount(articleId);  //得到评论的条数
			List<Evaluate> list1 = this.passageServiceImpl.findEvaluateByPage(num, 4, articleId);  //分页查询每页的评论
			Page<Evaluate> page = new Page<Evaluate>(num,4);
			page.setList(list1);
			page.setTotalCount(count);
//			System.out.println(count+"uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
			List<Integer> pageNums = new ArrayList();
			long totalNum = page.getTotalPageNum();
			for(int i=1;i<=totalNum;i++) {
				pageNums.add(i);
			}
			request.setAttribute("page", page);
			request.getServletContext().setAttribute("articleId", articleId);
			request.setAttribute("article", list);
			request.setAttribute("pages", pageNums);
			
			return "passage";
		}else {                     //如果评论内容不为空，那么就将评论内容插入数据库，然后重新转入passage页面（需要在评论区button按钮那里添加一个超链接，以便跳转）
			Evaluate evaluate = new Evaluate();
			evaluate.setEvaluateWorkType(5);
			evaluate.setEvaluateWorkId(articleId);
			evaluate.setEvaluateComment(evaluateComment);
			evaluate.setEvaluateTime(new Date());

			
//			String Id2 = request.getParameter("userId");
//			int userId = Integer.parseInt(Id2);
			User user = this.passageServiceImpl.findUserByUserId(2);
			evaluate.setUser(user);
			this.passageServiceImpl.insertEvaluate(evaluate);     //将用户的评论插入数据库
			
			int count = this.passageServiceImpl.findCount(articleId);  //得到评论的条数
//			System.out.println(count+"uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
			List<Evaluate> list1 = this.passageServiceImpl.findEvaluateByPage(num, 4, articleId);  //分页查询每页的评论
			Page<Evaluate> page = new Page<Evaluate>(num,4);
			page.setList(list1);
			page.setTotalCount(count);
			List<Integer> pageNums = new ArrayList();
			for(int i=1;i<=count;i++) {
				pageNums.add(i);
			}
			request.getServletContext().setAttribute("page", page);
			request.getServletContext().setAttribute("articleId", articleId);	
			request.getServletContext().setAttribute("article", list);
			request.setAttribute("pages", pageNums);
			return "passage";
		}	
	} 
	
	
	
	@RequestMapping(value="/insertEvaluate",method=RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> insertEvaluate(@RequestParam(value="articleId") String articleId,@RequestParam(value="evaluateContent") String evaluateContent,HttpServletRequest request) {
		System.out.println(articleId);
		System.out.println(evaluateContent);
		int articleid = Integer.parseInt(articleId);
//		String id = request.getParameter("userId");
//		int userId = Integer.parseInt(id);
//		User user = new User();
		User user = this.passageServiceImpl.findUserByUserId(2);
		Evaluate evaluate = new Evaluate();
		evaluate.setEvaluateWorkId(articleid);
		evaluate.setEvaluateWorkType(5);
		evaluate.setEvaluateComment(evaluateContent);
		evaluate.setEvaluateTime(new Date());
		evaluate.setUser(user);
		this.passageServiceImpl.insertEvaluate(evaluate);
		Map<String, Object> map = new HashMap<>();
		map.put("evaluateContent",evaluateContent);
		map.put("evaluateTime",evaluate.getEvaluateTime());
		map.put("userRealName",user.getUserRealName());
		map.put("userHeadPath",user.getUserHeadPath());
		
		
		
		return map;
	}
	
	@RequestMapping(value="showcomment",produces = "application/json; charset=utf-8")
	@ResponseBody
	public String showComment(HttpServletRequest request) {
		String articleId = request.getParameter("articleid");
		String pageNum = request.getParameter("pagenum"); 
		int num = 1;
		if(pageNum != null) {
			num = Integer.parseInt(pageNum);
		}
		int id = Integer.parseInt(articleId);
		List<Evaluate> comment = this.passageServiceImpl.findEvaluateByPage(num, 10, id);
		int count = this.passageServiceImpl.findCount(id);
		List<User> users = new ArrayList();
		Page pageComment = new Page(num,10);
		pageComment.setTotalCount(count);
		for(Evaluate temp:comment) {
			User a = new User();
			a.setUserHeadPath(temp.getUser().getUserHeadPath());
			a.setUserId(temp.getUser().getUserId());
			a.setUserNickName(temp.getUser().getUserNickName());
			System.out.println(temp.getEvaluateComment());
			users.add(a);
			temp.setUser(null);
		}
		for(User temp:users) {
			System.out.println(temp.toString());
		}
		pageComment.setList(comment);
		Gson gson = new Gson();
		String pageComments = gson.toJson(pageComment);
//		System.out.println(pageComments);
		String user = gson.toJson(users);
//		System.out.println(user);
		Map<String,String> map = new HashMap();
		map.put(user, pageComments);
//		System.out.println(map);
		String maps = gson.toJson(map);
//		System.out.println(maps);
		
		//System.out.println(pageComments);
		return maps;
	}
	
	
	@RequestMapping(value="logincomment",produces = "application/json; charset=utf-8")
	@ResponseBody
	public String loginComment(HttpServletRequest request) {
		String articleId = request.getParameter("articleid");
		int id = Integer.parseInt(articleId);
		HttpSession session = request.getSession();
		List<User> users = new ArrayList();
		User user = new User();
		user.setUserId(7);
		user.setUserRealName("孙明伟");
		user.setUserNickName("克罗夫");
		user.setUserHeadPath("consultant.png");
		session.setAttribute("user", user);
		String a = request.getParameter("evaluateContent");
	    Date time = new Date();
	    String localtime = new String();
	    SimpleDateFormat format1 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分");
        localtime = format1.format(time.getTime());
        Evaluate comment = new Evaluate();
        comment.setEvaluateTime(time);
        comment.setEvaluateWorkType(3);
        comment.setEvaluateWorkId(id);
        comment.setEvaluateStar(0);
        comment.setEvaluateComment(a);
        comment.setUser(user);
        
        this.passageServiceImpl.insertEvaluate(comment);
        Page pageComment = new Page<Evaluate>(1,4);
        int count = this.passageServiceImpl.findCount(id);
        pageComment.setTotalCount(count);
        List<Evaluate> list = this.passageServiceImpl.findEvaluateByPage(1, 4, id);
        for(Evaluate temp:list) {
			User  b= new User();
			b.setUserHeadPath(temp.getUser().getUserHeadPath());
			b.setUserId(temp.getUser().getUserId());
			b.setUserNickName(temp.getUser().getUserNickName());
//			System.out.println(temp.getEvaluateComment());
			users.add(b);
			temp.setUser(null);
		}
		for(User temp:users) {
			System.out.println(temp.toString());
		}
		pageComment.setList(list);
        Gson gson = new Gson();
		String pageComments = gson.toJson(pageComment);
//		System.out.println(pageComments);
		String usert = gson.toJson(users);
//		System.out.println(user);
		Map<String,String> map = new HashMap();
		map.put(usert, pageComments);
//		System.out.println(map);
		String maps = gson.toJson(map);
//		System.out.println(maps);
		
		//System.out.println(pageComments);
		return maps;
        
	}



}
