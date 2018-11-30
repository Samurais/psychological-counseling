/**
 * 
 */
package com.psychologicalcounseling.login.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.psychologicalcounseling.login.service.IsNewPhoneDaoImpl;
import com.psychologicalcounseling.login.service.LoginWeiboService;
import com.psychologicalcounseling.login.service.RegistServiceImpl;
import com.psychologicalcounseling.login.service.VerifyPwdServiceImpl;

import com.psychologicalcounseling.login.weibo4j.Oauth;
import com.psychologicalcounseling.login.weibo4j.http.AccessToken;
import com.psychologicalcounseling.login.weibo4j.Log;
import com.psychologicalcounseling.login.weibo4j.model.WeiboException;
import com.psychologicalcounseling.login.weibo4j.util.BareBonesBrowserLaunch;

/**
 *@desc:微博第三方登录
 *@author 邓旸
 *@date:2018年11月28日下午3:39:06
 */
@Controller
public class LoginWeiboController {
	@Resource
	private LoginWeiboService loginWeiboService;
	
	/**
	 * 
	 */
	public LoginWeiboController() {
	}

	@RequestMapping(value="/loginWeiboRequest.do",method=RequestMethod.GET)
	public void loginRequest(HttpServletResponse resp) throws IOException, WeiboException {
		Oauth oauth = new Oauth();
		resp.sendRedirect(oauth.authorize("code"));
	}
	
	@RequestMapping(value="/loginWeiboAuth2.do",method=RequestMethod.GET)
	public String loginWeiboAuth(@RequestParam("code")String code,HttpSession session) {
		Oauth oauth = new Oauth();
		Log.logInfo("code: " + code);
		try{
			AccessToken at = oauth.getAccessTokenByCode(code);
			System.out.println(at);
			String accessToken = at.getAccessToken();
			String weiboUid = at.getUid();
			//调用服务进行登录，若登录成功，将用户id存入session。
			//若登录失败，uid值为null
			if(accessToken!=null && weiboUid!=null) {
				int uid = loginWeiboService.login(accessToken,weiboUid);
				if(uid > 0) session.setAttribute("uid", uid);
				else session.setAttribute("uid", null);
			} else {
				session.setAttribute("uid", null);
				return "login";
			}
		} catch (WeiboException e) {
			if(401 == e.getStatusCode()){
				Log.logInfo("Unable to get the access token.");
				return "login";
			}else{
				e.printStackTrace();
			}
		}
		return "index";
	}
	
	
}
